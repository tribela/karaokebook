package kai.search.karaokebook;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
}