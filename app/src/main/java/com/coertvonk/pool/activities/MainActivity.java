// Copyright 2020 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.coertvonk.pool.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.espressif.wifi_provisioning.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private class CheckUrlTask implements Callable {

        String urlValue;

        public CheckUrlTask(String urlValue) {
            this.urlValue = urlValue;
        }

        public Boolean call() {

            HttpURLConnection urlConnection = null;
            Boolean ret = false;
            try {
                URL url = new URL(this.urlValue);
                urlConnection = (HttpURLConnection) url.openConnection();
                int status = urlConnection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    ret = true;
                }
            } catch (UnknownHostException e) {
                return false;
            } catch (Exception e) {
                return false;
            }
            return ret;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String urlValue = "http://pool.local/";

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new MyBrowser());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        CheckUrlTask checkUrlTask = new CheckUrlTask(urlValue);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Boolean> futureTask = executor.submit(checkUrlTask);

        try {
            Boolean ret = futureTask.get();
            if (ret) {
                webView.loadUrl(urlValue);
            } else {
                webView.loadUrl("file:///android_asset/offline.html");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        /*


        FutureTask[] checkUrlTasks = new FutureTask[0];
        Callable callable = new CheckUrlTask(urlValue);
        checkUrlTasks[0] = new FutureTask(callable);
        Thread t = new Thread(checkUrlTasks[0]);
        t.start();
        Boolean b = false;
        try {
            b = checkUrlTasks[0].get();
        } catch(Exception e) {
            e.printStackTrace();
        }
         */

/*
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            }
        });
        thread.start();
 */

    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_webview_provision) {
            Intent intent = new Intent(this, EspMainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}