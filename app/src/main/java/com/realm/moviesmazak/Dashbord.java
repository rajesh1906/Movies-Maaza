package com.realm.moviesmazak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.realm.moviesmazak.UI.Films;
import com.realm.moviesmazak.UI.PlayingVideo;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashbord extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashbord);

    }
    /*private class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                int fileLength = connection.getContentLength();
                input = connection.getInputStream();
                output = new FileOutputStream("/sdcard/movie_mazak.txt");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new retrievedata().execute();


        }
    }


    class retrievedata extends AsyncTask<String, String, String> {
        StringBuilder text = new StringBuilder();

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub

            try {
                File sdcard = Environment.getExternalStorageDirectory();
                File file = new File(sdcard, "movie_mazak.txt");
                Log.e("file exists ", "<><" + file.exists());
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String ab) {
            progressDialog.dismiss();
            Log.e(" text value is ", "<><>" + text.toString());
            try {
                JSONObject obj = new JSONObject(text.toString());
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("status",obj.getInt("status"));
                if (obj.getInt("status") == 1) {
                    Log.e("url is ", "<><>" + obj.getString("url"));
                   *//* BASE_URL = obj.getString("url");
                    editor.putString("URL", BASE_URL);*//*
                    initRetrofit(obj.getString("url"),true);
                    getResponse(generateValue());
                } else {
                    Toast.makeText(Films.this, "Server Under Maintaince", Toast.LENGTH_LONG).show();
                }
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            tv.setText(ab);
        }

    }*/


   /* void getResponse(final int coming_from) {
        progressDialog.show();
        Log.e("page number is ","<><>"+coming_from);
        if (!apicalled) {

            Log.e("value is ", "<><>" + generateValue());
            apicalled = true;
            serviceData = service.getUrlData("/short-movies/page/" +pageno+ "/");
            serviceData.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {

                        Document document = Jsoup.parse(response.body().source().readUtf8());
                        div = new Elements();
                        div = document.select("div.herald-main-content").select("article").select("div.herald-post-thumbnail").select("a");
                        count = 0;
                        for (final Element e : div) {
                            Log.e("Image", "" + e.attr("href").replace(BASE_URL, "").replace("/", ""));
//                            if(e.attr("href").contains("Movie")) {
                            urlsArray.add(e.attr("href"));
                            imagesArray.add(e.select("img").attr("src"));
//                            }
                            count++;
                            if (div.size() == count) {
                                if (pageno == 1) {
                                    progressDialog.setMessage("Buffering ...");
                                    mAdapter = new Films.MyAdapter(imagesArray, urlsArray);
                                    mRecyclerView.setAdapter(mAdapter);
                                } else {
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    void playVideo(String url) {
        Call<ResponseBody> fetchdata = service.getUrlData(url.replace(BASE_URL, ""));
        fetchdata.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        String html_resp = response.body().source().readUtf8();
                        Document document = Jsoup.parse(html_resp);
                        div = new Elements();
                        div = document.select("iframe");
                        String url = div.attr("src");
                        Log.e("frames" + pageno, "" + div);
                        Intent intent = new Intent(Films.this, PlayingVideo.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    } else {
                        if (pageno == 1) {
                            if (null != progressDialog && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                    progressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }
*/

}
