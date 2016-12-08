package kai.search.karaokebook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import kai.search.karaokebook.UpdateChecker;

/**
 * Created by kjwon15 on 2014. 7. 17..
 */
public class DbAdapter {
    private static final String COL_ROWID = "rowid";

    private static final String TABLE_SONG = "songs";
    private static final String COL_VENDOR = "vendor";
    private static final String COL_NUMBER = "number";
    private static final String COL_TITLE = "title";
    private static final String COL_SINGER = "singer";

    private static final String TABLE_FAVORITES = "favourites";
    private static final String COL_CATEGORY_ID = "category_id";
    private static final String COL_SONG_ID = "song_id";

    private static final String TABLE_FAVCATEGORY = "favourite_categories";
    private static final String COL_CATEGORY_NAME = "category_name";

    private static final String TABLE_INFO = "information";
    private static final String COL_UPDATED = "updated";
    private static final String DATE_INITIAL = "1970-01-01 00:00:00";

    private Context context;
    private DbHelper dbHelper;

    public DbAdapter(Context context) {
        this.context = context;
        this.dbHelper = new DbHelper(context);
    }

    public long createSong(Song song) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_VENDOR, song.getVendor());
        values.put(COL_NUMBER, song.getNumber());
        values.put(COL_TITLE, song.getTitle());
        values.put(COL_SINGER, song.getSinger());

        long id = db.insert(TABLE_SONG, null, values);
        db.close();
        return id;
    }

    public boolean createSongs(JSONArray songs, String updated, UpdateChecker.DoUpdate task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean succeed = false;
        db.beginTransaction();
        try {
            for (int i = 0; i < songs.length(); i++) {
                try {
                    ContentValues values = new ContentValues();
                    JSONObject song = songs.getJSONObject(i);

                    String vendor = song.getString("vendor");
                    String number = song.getString("number");
                    String title = song.getString("title");
                    String singer = song.getString("singer");

                    values.put(COL_VENDOR, vendor);
                    values.put(COL_NUMBER, number);
                    values.put(COL_TITLE, title);
                    values.put(COL_SINGER, singer);
                    db.insert(TABLE_SONG, null, values);

                    task.publishProgress(i + 1, title);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ContentValues values = new ContentValues();
            values.put(COL_UPDATED, updated);
            db.update(TABLE_INFO, values, null, null);

            db.setTransactionSuccessful();
            succeed = true;
        } finally {
            db.endTransaction();
            db.close();
        }

        return succeed;
    }

    public List<Song> getSongs(String _vendor, String _title, String _number, String _singer,
                               boolean searchFromMiddle) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<String> whereClauses = new ArrayList<String>();
        ArrayList<String> whereArgs = new ArrayList<String>();
        ArrayList<Song> results = new ArrayList<Song>();

        String likeFormat;
        if (searchFromMiddle) {
            likeFormat = "%%%s%%";
        } else {
            likeFormat = "%s%%";
        }

        if (_vendor != null) {
            whereClauses.add("vendor like ?");
            whereArgs.add(String.format(likeFormat, _vendor));
        }

        if (_title != null) {
            whereClauses.add("title like ?");
            whereArgs.add(String.format(likeFormat, _title));
        }

        if (_number != null) {
            whereClauses.add("number like ?");
            whereArgs.add(String.format(likeFormat, _number));
        }

        if (_singer != null) {
            whereClauses.add("singer like ?");
            whereArgs.add(String.format(likeFormat, _singer));
        }

        String[] args = whereArgs.toArray(new String[whereArgs.size()]);
        Cursor cursor = db.query(TABLE_SONG,
                new String[]{COL_ROWID, COL_VENDOR, COL_NUMBER, COL_TITLE, COL_SINGER},
                TextUtils.join(" and ", whereClauses.toArray()), args,
                null, null, COL_TITLE + " asc", "100");

        int indexRowid = cursor.getColumnIndex(COL_ROWID);
        int indexVendor = cursor.getColumnIndex(COL_VENDOR);
        int indexTitle = cursor.getColumnIndex(COL_TITLE);
        int indexNumber = cursor.getColumnIndex(COL_NUMBER);
        int indexSinger = cursor.getColumnIndex(COL_SINGER);
        while (cursor.moveToNext()) {
            long rowid = cursor.getLong(indexRowid);
            String vendor = cursor.getString(indexVendor);
            String title = cursor.getString(indexTitle);
            String number = cursor.getString(indexNumber);
            String singer = cursor.getString(indexSinger);
            results.add(new Song(rowid, vendor, number, title, singer));
        }

        db.close();
        return results;
    }

    public List<FavouriteCategory> getFavoriteCategories() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<FavouriteCategory> results = new ArrayList<>();

        Cursor cursor = db.query(TABLE_FAVCATEGORY,
                new String[]{COL_ROWID, COL_CATEGORY_NAME},
                null, null, null, null, COL_ROWID, null);
        int indexRowId = cursor.getColumnIndex(COL_ROWID);
        int indexCategoryName = cursor.getColumnIndex(COL_CATEGORY_NAME);
        while (cursor.moveToNext()) {
            long rowId = cursor.getLong(indexRowId);
            String name = cursor.getString(indexCategoryName);
            results.add(new FavouriteCategory(rowId, name));
        }

        db.close();
        return results;
    }

    public boolean removeFavoriteCategory(FavouriteCategory category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.delete(TABLE_FAVCATEGORY, "rowid = ?",
                new String[]{String.valueOf(category.getRowId())});
        db.close();
        return result > 0;
    }

    public boolean addFavouriteSong(Song song) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        long rowid = song.getRowid();

        values.put(COL_SONG_ID, rowid);
        long result = db.insert(TABLE_FAVORITES, null, values);

        db.close();
        return result > 0;
    }

    public boolean removeFavouriteSong(long categoryId, Song song) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.delete(TABLE_FAVORITES,
                COL_CATEGORY_ID + " = ? and " + COL_SONG_ID + " = ?",
                new String[]{String.valueOf(categoryId), String.valueOf(song.getRowid())});

        db.close();
        return result > 0;
    }

    public List<Song> getFavouriteSongs(long categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Song> results = new ArrayList<Song>();

        Cursor cursor = db.rawQuery(MessageFormat.format(
                "select {1}, {2}, {3}, {4}, {5} from {0} where rowid in (" +
                        "select {7} from {6} where {8} = {9})",
                TABLE_SONG, COL_ROWID, COL_VENDOR, COL_TITLE, COL_NUMBER, COL_SINGER,
                TABLE_FAVORITES, COL_SONG_ID, COL_CATEGORY_ID, categoryId
        ), null);

        int indexRowid = cursor.getColumnIndex(COL_ROWID);
        int indexVendor = cursor.getColumnIndex(COL_VENDOR);
        int indexTitle = cursor.getColumnIndex(COL_TITLE);
        int indexNumber = cursor.getColumnIndex(COL_NUMBER);
        int indexSinger = cursor.getColumnIndex(COL_SINGER);
        while (cursor.moveToNext()) {
            long rowid = cursor.getLong(indexRowid);
            String vendor = cursor.getString(indexVendor);
            String title = cursor.getString(indexTitle);
            String number = cursor.getString(indexNumber);
            String singer = cursor.getString(indexSinger);
            results.add(new Song(rowid, vendor, number, title, singer));
        }

        db.close();
        return results;
    }

    public boolean setLastUpdated(String updated) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_UPDATED, updated);
        long result = db.update(TABLE_INFO, values, null, null);

        db.close();
        return result > 0;
    }

    public String getLastUpdated() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INFO,
                new String[]{COL_UPDATED},
                null,
                null, null, null, COL_UPDATED + " desc");

        cursor.moveToFirst();
        String lastUpdated = cursor.getString(0);

        db.close();
        return lastUpdated;
    }

    public boolean isFirstTime() {
        return getLastUpdated().equals(DATE_INITIAL);
    }


    private class DbHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "karaoke";
        private static final int DB_VERSION = 3;
        private final Context context;

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.context = context;

            if (checkDbExists() == false) {
                try {
                    copyDatabase();
                    Log.i("DB", "Copy initial database succeeded.");
                } catch (IOException e) {
                    Log.e("DB", "Failed to copy database");
                }
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String query;
            query = MessageFormat.format(
                    "create table if not exists {0}(" +
                            "{1} text not null," +
                            "{2} text not null," +
                            "{3} text not null," +
                            "{4} text not null," +
                            "unique ({1}, {2}) on conflict replace" +
                            ");",
                    TABLE_SONG, COL_VENDOR, COL_NUMBER, COL_TITLE, COL_SINGER
            );
            db.execSQL(query);

            query = MessageFormat.format(
                    "create table if not exists {0}(" +
                            "{1} datetime not null" +
                            ");",
                    TABLE_INFO, COL_UPDATED
            );
            db.execSQL(query);

            query = MessageFormat.format(
                    "create table if not exists {0}(" +
                            "{1} text not null," +
                            "unique ({1})" +
                            ");",
                    TABLE_FAVCATEGORY, COL_CATEGORY_NAME
            );
            db.execSQL(query);

            query = (
                    "create table if not exists " + TABLE_FAVORITES + "(" +
                            COL_CATEGORY_ID + " integer not null," +
                            COL_SONG_ID + " integer not null," +
                            "foreign key("+ COL_CATEGORY_ID + ") references " + TABLE_FAVCATEGORY + "(rowid)," +
                            "foreign key("+ COL_SONG_ID + ") references " + TABLE_SONG + "(rowid)," +
                            "unique (" + COL_CATEGORY_ID + ", " + COL_SONG_ID + ")" +
                            ");"
            );
            db.execSQL(query);

            // Insert zero last updated.
            Cursor cursor = db.query(TABLE_INFO,
                    new String[]{COL_UPDATED},
                    null,
                    null, null, null, null);
            if (cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(COL_UPDATED, DATE_INITIAL);
                db.insert(TABLE_INFO, null, values);
            }

            Log.i("DB", "Database created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("DB", "Database upgraded from " + oldVersion);

            switch(oldVersion) {
                case 1:
                    db.execSQL(MessageFormat.format(
                    "update {0} set {1} = datetime({1});", TABLE_INFO, COL_UPDATED));
                case 2:
                    final String MIGRATED_CATEGORY = "MIGRATED";
                    // Create new favorite categories table.
                    db.execSQL(MessageFormat.format(
                            "create table if not exists {0}(" +
                                    "{1} text not null," +
                                    "unique ({1})" +
                                    ");",
                            TABLE_FAVCATEGORY, COL_CATEGORY_NAME
                    ));

                    // Insert one category for migrate.
                    db.execSQL(MessageFormat.format(
                            "insert into {0} values (\"{1}\");",
                            TABLE_FAVCATEGORY, MIGRATED_CATEGORY
                    ));

                    // Create new favorites table.
                    db.execSQL(
                            "create table if not exists " + TABLE_FAVORITES + "(" +
                                    COL_CATEGORY_ID + " integer not null," +
                                    COL_SONG_ID + " integer not null," +
                                    "foreign key("+ COL_CATEGORY_ID + ") references " + TABLE_FAVCATEGORY + "(rowid)," +
                                    "foreign key("+ COL_SONG_ID + ") references " + TABLE_SONG + "(rowid)," +
                                    "unique (" + COL_CATEGORY_ID + ", " + COL_SONG_ID + ")" +
                                    ");"
                    );

                    // Migrate all stars to newer table.
                    db.execSQL(MessageFormat.format(
                            "insert into {0} ({1}, {2}) select " +
                                    "(select rowid " +
                                    "from {3} " +
                                    "where {4} = \"{5}\"" +
                                    "), {2} from stars",
                            TABLE_FAVORITES, COL_CATEGORY_ID, COL_SONG_ID,
                            TABLE_FAVCATEGORY,
                            COL_CATEGORY_NAME, MIGRATED_CATEGORY
                    ));

                    // Drop old table.
                    db.execSQL("drop table stars");
            }
        }

        private String getDbPath(boolean includeFilename) {
            if (includeFilename) {
                return MessageFormat.format("/data/data/{0}/databases/{1}",
                        context.getPackageName(),
                        DB_NAME);
            } else {
                return MessageFormat.format("/data/data/{0}/databases",
                        context.getPackageName()
                );
            }
        }

        private boolean checkDbExists() {
            String path = getDbPath(true);
            boolean exist = false;

            File dbFile = new File(path);
            exist = dbFile.exists();

            return exist;
        }

        private void copyDatabase() throws IOException {
            InputStream input = context.getAssets().open(DB_NAME);
            String dbPath = getDbPath(true);
            OutputStream output;
            try {
                output = new FileOutputStream(dbPath);
            } catch (FileNotFoundException e) {
                new File(getDbPath(false)).mkdirs();
                output = new FileOutputStream(dbPath);
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            input.close();
        }
    }
}
