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

    private static final String TABLE_STAR = "stars";
    private static final String COL_SONG_ID = "song_id";

    private static final String TABLE_INFO = "information";
    private static final String COL_UPDATED = "updated";
    private static final String DATE_INITIAL = "1970-01-01";

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

    public boolean addFavouriteSong(Song song) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        long rowid = song.getRowid();

        values.put(COL_SONG_ID, rowid);
        long result = db.insert(TABLE_STAR, null, values);

        db.close();
        return result > 0;
    }

    public List<Song> getFavouriteSongs() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Song> results = new ArrayList<Song>();

        Cursor cursor = db.rawQuery(MessageFormat.format(
                "select {1}, {2}, {3}, {4}, {5} from {0} where rowid in (" +
                        "select {7} from {6})",
                TABLE_SONG, COL_ROWID, COL_VENDOR, COL_TITLE, COL_NUMBER, COL_SINGER,
                TABLE_STAR, COL_SONG_ID
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
        private static final int DB_VERSION = 1;
        private final Context context;

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            try {
                copyDatabase();
                Log.i("DB", "Copy initial database succeeded.");
            } catch (IOException e) {
                Log.e("DB", "Failed to copy database");
                createDatabase(db);
            }

            Log.i("DB", "Database created");
        }

        private void createDatabase(SQLiteDatabase db) {
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
                            "{1} date not null" +
                            ");",
                    TABLE_INFO, COL_UPDATED
            );
            db.execSQL(query);

            query = MessageFormat.format(
                    "create table if not exists {0}(" +
                            "{1} integer primary key," +
                            "foreign key({1}) references {2}(rowid)" +
                            ");",
                    TABLE_STAR, COL_SONG_ID, TABLE_SONG
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
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("DB", "Database upgraded");
        }

        private void copyDatabase() throws IOException {
            InputStream input = context.getAssets().open(DB_NAME);
            String dbPath = MessageFormat.format("/data/data/{0}/databases/{1}",
                    context.getPackageName(),
                    DB_NAME);
            OutputStream output = new FileOutputStream(dbPath);

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
