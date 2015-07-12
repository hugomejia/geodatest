package com.arrg.android.app.geoda;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadJSonFiles extends AsyncTask<String, String, String> {

    private Context context;
    private TextView tvLoading;

    public DownloadJSonFiles(Context context, TextView tvLoading) {
        this.context = context;
        this.tvLoading = tvLoading;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public TextView getTvLoading() {
        return tvLoading;
    }

    public void setTvLoading(TextView tvLoading) {
        this.tvLoading = tvLoading;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        for (int i = 0; i < params.length; i += 2) {

            int count;

            try {
                URL url = new URL(params[i]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                OutputStream output = new FileOutputStream(Constants.APP_DATA_SDCARD + "/" + params[i + 1]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate();
    }

    @Override
    protected void onPostExecute(String params) {
        super.onPostExecute(params);

        DownloadPublicity downloadPublicity = new DownloadPublicity(getContext(), getTvLoading());
        try {
            downloadPublicity.downloadFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
