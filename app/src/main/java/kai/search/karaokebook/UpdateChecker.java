package kai.search.karaokebook;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kai.search.karaokebook.db.DbAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateChecker {

    private static SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private final Context context;
    private DbAdapter dbAdapter;
    private OkHttpClient client = new OkHttpClient();

    public UpdateChecker(Context context) {
        this.context = context;
        this.dbAdapter = new DbAdapter(context);
    }

    public void checkUpdate() {
        CheckUpdate checkUpdate = new CheckUpdate();
        checkUpdate.execute();
    }

    private String getHttpContent(String urlStr) throws IOException {
        Request request = new Request.Builder()
                .url(urlStr)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private class CheckUpdate extends AsyncTask<Void, Void, String> {
        String updatedDate;
        String toastMsg = null;

        @Override
        protected String doInBackground(Void... params) {
            try {
                String content = getHttpContent("http://karaoke.kjwon15.net/info");
                JSONObject json = new JSONObject(content);
                updatedDate = json.getString("last_updated");
            } catch (JSONException e) {
                toastMsg = context.getString(R.string.msg_update_unavailable);
                e.printStackTrace();
            } catch (IOException e) {
                toastMsg = context.getString(R.string.msg_update_unavailable);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (updatedDate != null) {
                try {
                    Date localUpdated = null;
                    localUpdated = datetimeFormat.parse(dbAdapter.getLastUpdated());
                    Date remoteUpdated = datetimeFormat.parse(updatedDate);

                    if (localUpdated.before(remoteUpdated)) {
                        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        new DoUpdate(context).execute();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(MessageFormat.format(
                                context.getString(R.string.msg_do_update),
                                remoteUpdated));
                        builder.setPositiveButton(android.R.string.yes, clickListener);
                        builder.setNegativeButton(android.R.string.no, clickListener);
                        builder.show();
                    } else {
                        Toast.makeText(context, R.string.msg_up_to_date, Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (toastMsg != null) {
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class DoUpdate extends AsyncTask<Object, Object, Void> {
        private static final int ACTION_SET_MAX = 0;
        private static final int ACTION_SET_PROGRESS = 1;
        private final Context context;
        private ProgressDialog dialog;

        public DoUpdate(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            dialog.setMessage(context.getString(R.string.msg_update_db));
            dialog.show();

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            int action = (Integer) values[0];
            switch (action) {
                case ACTION_SET_MAX:
                    int length = (Integer) values[1];
                    dialog.setMax(length);
                    break;
                case ACTION_SET_PROGRESS:
                    int currentPosition = (Integer) values[1];
                    String currentSong = (String) values[2];
                    dialog.setProgress(currentPosition);
                    dialog.setMessage(currentSong);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Object... params) {
            try {
                String localUpdated = dbAdapter.getLastUpdated();
                String content = getHttpContent(
                        String.format("http://karaoke.kjwon15.net/get_update/%s/",
                                localUpdated.replace(" ", "%20")));
                JSONObject json = new JSONObject(content);

                String updated = json.getString("updated");
                JSONArray songs = json.getJSONArray("songs");

                publishProgress(ACTION_SET_MAX, songs.length());

                dbAdapter.createSongs(songs, updated, this);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void publishProgress(int position, String message) {
            publishProgress(ACTION_SET_PROGRESS, position, message);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }
}
