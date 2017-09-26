package com.realm.moviesmazak.UI;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.realm.moviesmazak.Manager.SessionManager;
import com.realm.moviesmazak.R;

/**
 * Created by Rajesh Kumar on 25-09-2017.
 */

public class PlayingVideo extends AppCompatActivity {
    WebView webview;
    ProgressBar progressBar;
    String frames;
    String url;
    SessionManager sessionManager;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.playvideo);
        sessionManager = new SessionManager(PlayingVideo.this);
        url = getIntent().getExtras().getString("url");
//        url ="http://123freemovies.net/watch-mothers-milk-2013-free-123movies.html?play=1";

        webview = (WebView) findViewById(R.id.webplayvideo);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webview.getSettings().setSaveFormData(true);
        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webview.setVerticalScrollBarEnabled(false);
        webview.setHorizontalScrollBarEnabled(false);
        webview.getSettings().setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= 16) {
            webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        webview.getSettings().setLightTouchEnabled(true);
        webview.getSettings().setSupportMultipleWindows(true);
        webview.getSettings().setLoadsImagesAutomatically(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        url = "<iframe width=\"100%\" frameborder=\"0\" height=\"100%\" scrolling=\"no\" style=\"background: #000000; overflow:hidden\" src=\"" + url + "\" allowfullscreen></iframe>";
        frames = "<html width=\""+getWindowManager().getDefaultDisplay().getWidth()+"\" height=\""+getWindowManager().getDefaultDisplay().getWidth()+"\"><body style=\"margin:0; padding:0\">" + url + "</body></html>";
        webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                progressBar.setVisibility(View.VISIBLE);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                try {
                    Toast.makeText(PlayingVideo.this, "Please Check Internet", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.loadData(frames, "text/html", "utf-8");
    }

    @Override
    protected void onPause() {
        super.onPause();
        webview.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webview.onResume();
    }
}
