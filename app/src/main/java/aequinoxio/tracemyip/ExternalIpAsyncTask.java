package aequinoxio.tracemyip;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by utente on 10/02/2018.
 */

public class ExternalIpAsyncTask extends AsyncTask <Void, Void, String> {

    private NetworkUpdateCallback networkUpdateCallback;

    @Override
    protected String doInBackground(Void... voids) {
        return getPublicIPAddress();
    }

    public ExternalIpAsyncTask(NetworkUpdateCallback callback) {
        networkUpdateCallback=callback;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
//        EditText edt = (EditText)((Activity)context).findViewById(R.id.edtLog);
//        edt.append(String.format("%s%n",s));
        networkUpdateCallback.callback(s);
    }

    @NonNull
    private String getPublicIPAddress() {
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            URL url = new URL("https://wtfismyip.com/text");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            if (httpURLConnection.getResponseCode()!=-1) {
                InputStream is = httpURLConnection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
            }
            httpURLConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            sb.append("External connection error");
        }

        return sb.toString();
    }
}
