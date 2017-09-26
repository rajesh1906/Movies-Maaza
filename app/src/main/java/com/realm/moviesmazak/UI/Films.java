package com.realm.moviesmazak.UI;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.realm.moviesmazak.Manager.SessionManager;
import com.realm.moviesmazak.Network.RestAPI;
import com.realm.moviesmazak.R;

import org.json.JSONException;
import org.json.JSONObject;

import org.json.simple.parser.ParseException;
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
import java.util.ArrayList;
import java.util.Random;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Films extends Activity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private
    ArrayList<String> imagesArray;
    ArrayList<String> urlsArray;
    private SessionManager sessionManager;
    int pageno = 1;
    int count = 0;
    Elements div;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    Retrofit retrofit;
    RestAPI service;
    boolean apicalled = false;
    Call<ResponseBody> serviceData;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private AdRequest inter_adRequest;
    private AdRequest banner_adRequest;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Context con;
    //    private static String BASE_URL = "http://www.andhrawatch.com/";
    private static String BASE_URL = "";
    String url = "https://dl.dropboxusercontent.com/s/f1joqpquzedmmpy/moviesurl.txt";
    SharedPreferences sharedpreferences;
    OkHttpClient defaultHttpClient;
    //    String url="https://drive.google.com/open?id=10y2JM3it3-l3a3MEjryp70iFfu4OcWGgWZ3hFUO_wv8/";
//    https://www.dropbox.com/s/f1joqpquzedmmpy/moviesurl.txt?dl=0
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        con = this.getBaseContext();
        imagesArray = new ArrayList<>();
        urlsArray = new ArrayList<>();
        sessionManager = new SessionManager(Films.this);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        progressDialog = new ProgressDialog(Films.this);
        progressDialog.setMessage("Loading Movies...");
        try{
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "movie_mazak.txt");
            if(!file.exists()||sharedpreferences.getInt("status",0)==2){
                new DownloadTask().execute(url);
                Log.e("file not existed","<><>");
            }else{
                initRetrofit(sharedpreferences.getString("URL", ""),false);
                getResponse();
                Log.e("file existed","<><>");
            }
        }catch (Exception e){
            e.printStackTrace();
        }



    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

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
                    BASE_URL = obj.getString("url");
                    editor.putString("URL", BASE_URL);
                    initRetrofit(BASE_URL,true);
                    getResponse();
                } else {
                    Toast.makeText(Films.this, "Server Under Maintaince", Toast.LENGTH_LONG).show();
                }
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            tv.setText(ab);
        }

    }

    protected void initializeInterstitialAdds() {
        mInterstitialAd = new InterstitialAd(con);
        mInterstitialAd.setAdUnitId("ca-app-pub-1671845713125648/1100797419");
        inter_adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(inter_adRequest);
    }

    public void my_adds() {
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1671845713125648~4193864617");
        mAdView = (AdView) findViewById(R.id.adView);
        banner_adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(banner_adRequest);
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        my_adds();
        initializeInterstitialAdds();
        if (mAdView != null) {
            mAdView.resume();
        }

    }

    public void initRetrofit(String url,boolean initial) {
        if(initial){
              HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
         defaultHttpClient = new OkHttpClient.Builder().addInterceptor(interceptor)
                .addInterceptor(
                        new Interceptor() {
                            @Override
                            public okhttp3.Response intercept(Chain chain) throws IOException {
                                Request request = chain.request().newBuilder()
                                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                                        .build();
                                return chain.proceed(request);
                            }
                        }).build();
            retrofit = new Retrofit.Builder()
                    .client(defaultHttpClient)
                    .baseUrl(url).build();
            service = retrofit.create(RestAPI.class);
        }else{
            retrofit = new Retrofit.Builder()
                    .baseUrl(url).build();
            service = retrofit.create(RestAPI.class);
        }


    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<String> imagesArray;
        private ArrayList<String> urlsArray;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;

            ViewHolder(View v) {
                super(v);
                icon = (ImageView) v.findViewById(R.id.imageview);
                title = (TextView) v.findViewById(R.id.title);
            }
        }

        MyAdapter(ArrayList<String> imagesArray, ArrayList<String> urlsArray) {
            this.imagesArray = imagesArray;
            this.urlsArray = urlsArray;
            mRecyclerView.setOnScrollListener(new ScrolledDataLoader());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Glide.with(Films.this)
                    .load(imagesArray.get(position))
                    .into(holder.icon);
            holder.title.setTag(urlsArray.get(position));
            holder.title.setText(urlsArray.get(position).replace(BASE_URL, "").replace("/", ""));
            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        progressDialog.show();
                        playVideo(urlsArray.get(holder.getAdapterPosition()));
                    }
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            initializeInterstitialAdds();
                            progressDialog.show();
                            playVideo(urlsArray.get(position));
                        }
                    });
                }
            });
            if (pageno == 1) {
                if (null != progressDialog && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return imagesArray.size();
        }

    }

    void getResponse() {
        progressDialog.show();
        if (!apicalled) {

            Log.e("value is ", "<><>" + generateValue());
            apicalled = true;
            serviceData = service.getUrlData("/telugu-movies/page/" + pageno + "/");
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
                            urlsArray.add(e.attr("href"));
                            imagesArray.add(e.select("img").attr("src"));
                            count++;
                            if (div.size() == count) {
                                if (pageno == 1) {
                                    progressDialog.setMessage("Buffering ...");
                                    mAdapter = new MyAdapter(imagesArray, urlsArray);
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

    public class ScrolledDataLoader extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            int lastvisibleposition = gridLayoutManager.findLastVisibleItemPosition();
            if (newState == 0 && lastvisibleposition == urlsArray.size() - 1) {
                apicalled = false;
                pageno++;
                progressBar.setVisibility(View.VISIBLE);
                getResponse();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!sessionManager.isRated()) {
            rateusPopUp();
        } else {
            super.onBackPressed();
        }
    }

    private void rateusPopUp() {
        AlertDialog.Builder builder;
        AlertDialog dialog;
        builder = new AlertDialog.Builder(Films.this);
        builder.setMessage("Your Appreciation is our pleasure. Please RATE US");
        builder.setNegativeButton("LATER",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setPositiveButton("RATE",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sessionManager.setRated();
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                });
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }


    private int generateValue() {
        Random r = new Random();
        int Low = 1;
        int High = 81;
        return r.nextInt(High - Low) + Low;
    }
}
