package com.arrg.android.app.geoda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadPublicity {

    private Context context;
    private TextView tvLoad;

    private ArrayList<String> filesData = new ArrayList<>();

    public DownloadPublicity(Context context, TextView tvLoad) {
        this.context = context;
        this.tvLoad = tvLoad;
    }

    public void downloadFiles() throws IOException {

        InputStream in = new FileInputStream(Constants.APP_DATA_SDCARD + "/PublicityData.json");
        readJSon(in);

        String filesDataToDownload[] = new String[filesData.size()];

        for (int i = 0; i < filesData.size(); i++) {
            filesDataToDownload[i] = filesData.get(i);
        }

        new DownloadPublicityFiles().execute(filesDataToDownload);
    }

    public void readJSon(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        reader.beginArray();
        while (reader.hasNext()) {
            String folder = null;
            String names = null;
            String link = null;

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                final boolean isNull = reader.peek() == JsonToken.NULL;

                if (name.equals("folder") && !isNull) {
                    folder = reader.nextString();
                } else if (name.equals("files") && !isNull) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String innerName = reader.nextName();
                            final boolean isInnerNull = reader.peek() == JsonToken.NULL;

                            if (innerName.equals("name") && !isInnerNull) {
                                names = reader.nextString();
                            } else if (innerName.equals("link") && !isInnerNull) {
                                link = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }

                        File file = new File(Constants.APP_DATA_SDCARD + "/" + folder);

                        if (!file.exists()) {
                            String TAG = "DownloadPublicity";
                            try {
                                if (file.mkdir()) {
                                    Log.d(TAG, "Folder " + file.getPath() + " fully created.");
                                } else {
                                    Log.d(TAG, "Folder " + file.getPath() + " already created.");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }

                        filesData.add(link);
                        filesData.add(folder);
                        filesData.add(names);

                        reader.endObject();
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }
        reader.endArray();
    }

    public Context getContext() {
        return context;
    }

    class DownloadPublicityFiles extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvLoad.setText(getContext().getString(R.string.downloading_publicity));
        }

        @Override
        protected String doInBackground(String... params) {
            for (int i = 0; i < params.length; i += 3) {
                int count;

                try {
                    URL url = new URL(params[i]);
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    int lengthOfFile = connection.getContentLength();

                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    OutputStream output = new FileOutputStream(Constants.APP_DATA_SDCARD + "/" + params[i + 1] + "/" + params[i + 2]);

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

            tvLoad.setText(getContext().getString(R.string.download_complete));

            SharedPreferences preferences = getContext().getSharedPreferences(Constants.PACKAGE_NAME + ".STARTUP_SETTINGS_PREFERENCES", Context.MODE_PRIVATE);
            String type = preferences.getString("type_of_app", "Real");

            if (type.toUpperCase().equals("DEMO")) {
                Intent i = new Intent(getContext(), FlipperViewDemoActivity.class);
                getContext().startActivity(i);
            } else {
                Intent i = new Intent(getContext(), FlipperViewActivity.class);
                getContext().startActivity(i);
            }

            ((AppCompatActivity)getContext()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            ((AppCompatActivity)getContext()).finish();
        }
    }
}
