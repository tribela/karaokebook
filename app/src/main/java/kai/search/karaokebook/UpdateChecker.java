package kai.search.karaokebook;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import kai.search.karaokebook.db.DbAdapter;

public class UpdateChecker {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
    private DbAdapter dbAdapter;

    public UpdateChecker(Context context) {
        this.dbAdapter = new DbAdapter(context);
    }

    public boolean updateAvailable() {
        CheckUpdate checkUpdate = new CheckUpdate();
        checkUpdate.execute();
        try {
            String updatedDate = checkUpdate.get();
            if (updatedDate == null) {
                return false;
            }

            Date localUpdated = dateFormat.parse(dbAdapter.getLastUpdated());
            Date remoteUpdated = dateFormat.parse(updatedDate);

            if (localUpdated.before(remoteUpdated)) {
                return true;
            }

            return false;
        } catch (ParseException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public void doUpdate(Context context) {
        new DoUpdate(context).execute();
    }

    private String getHttpContent(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);

        InputStream content = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
        StringBuilder builder = new StringBuilder();
        String buffer;

        while ((buffer = reader.readLine()) != null) {
            builder.append(buffer);
        }

        return builder.toString();
    }

    private class CheckUpdate extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            try {
                String content = getHttpContent("http://karaoke.kjwon15.net/info");
                JSONObject json = new JSONObject(content);
                return json.getString("last_updated");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
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
                        String.format("http://karaoke.kjwon15.net/get_update/%s/", localUpdated));
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
