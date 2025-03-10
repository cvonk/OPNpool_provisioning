// Copyright 2022 Coert Vonk
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

package com.coertvonk.opnpool.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.coertvonk.pool.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private static final String urlValue = "http://opnpool.local/";
    private static final String TAG = MainActivity.class.getSimpleName();

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
                webView.loadUrl("file:///android_asset/index.html");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
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
        getMenuInflater().inflate(R.menu.menu_overflow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_main_provision: {
                Intent intent = new Intent(this, EspMainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_main_open_in_browser: {
                Uri uri = Uri.parse(urlValue);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(Intent.createChooser(intent, "Browse with"));
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }



/*
    private TextView tvTitle, tvBack, tvCancel;
    private ImageView tick0, tick1, tick2, tick3;
    private ContentLoadingProgressBar progress0, progress1, progress2, progress3;
    private TextView tvErrAtStep0, tvErrAtStep1, tvErrAtStep2, tvErrAtStep3, tvProvError;

    private CardView btnOk;
    private TextView txtOkBtn;

    private String ssidValue, passphraseValue, mqttUrlValue = "";
    private ESPProvisionManager provisionManager;
    private boolean isProvisioningCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provision);

        Intent intent = getIntent();
        ssidValue = intent.getStringExtra(AppConstants.KEY_WIFI_SSID);
        passphraseValue = intent.getStringExtra(AppConstants.KEY_WIFI_PASSWORD);
        mqttUrlValue = intent.getStringExtra(AppConstants.KEY_MQTT_URL);

        provisionManager = ESPProvisionManager.getInstance(getApplicationContext());
        initViews();
        EventBus.getDefault().register(this);

        Log.d(TAG, "Selected AP -" + ssidValue);
        Log.d(TAG, "Selected MQTT -" + mqttUrlValue);
        showLoading();
        doProvisioning();
    }

    @Override
    public void onBackPressed() {
        provisionManager.getEspDevice().disconnectDevice();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceConnectionEvent event) {

        Log.d(TAG, "On Device Connection Event RECEIVED : " + event.getEventType());

        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
                if (!isFinishing() && !isProvisioningCompleted) {
                    showAlertForDeviceDisconnected();
                }
                break;
        }
    }

    private View.OnClickListener okBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            provisionManager.getEspDevice().disconnectDevice();
            finish();
        }
    };

    private void initViews() {

        tvTitle = findViewById(R.id.main_toolbar_title);
        tvBack = findViewById(R.id.btn_back);
        tvCancel = findViewById(R.id.btn_cancel);

        tick0 = findViewById(R.id.iv_tick_0);
        tick1 = findViewById(R.id.iv_tick_1);
        tick2 = findViewById(R.id.iv_tick_2);
        tick3 = findViewById(R.id.iv_tick_3);

        progress0 = findViewById(R.id.prov_progress_0);
        progress1 = findViewById(R.id.prov_progress_1);
        progress2 = findViewById(R.id.prov_progress_2);
        progress3 = findViewById(R.id.prov_progress_3);

        tvErrAtStep0 = findViewById(R.id.tv_prov_error_0);
        tvErrAtStep1 = findViewById(R.id.tv_prov_error_1);
        tvErrAtStep2 = findViewById(R.id.tv_prov_error_2);
        tvErrAtStep3 = findViewById(R.id.tv_prov_error_3);
        tvProvError = findViewById(R.id.tv_prov_error);

        tvTitle.setText(R.string.title_activity_provisioning);
        tvBack.setVisibility(View.GONE);
        tvCancel.setVisibility(View.GONE);

        btnOk = findViewById(R.id.btn_ok);
        txtOkBtn = findViewById(R.id.text_btn);
        btnOk.findViewById(R.id.iv_arrow).setVisibility(View.GONE);

        txtOkBtn.setText(R.string.btn_ok);
        btnOk.setOnClickListener(okBtnClickListener);
    }

    private void doProvisioning() {

        tick0.setVisibility(View.GONE);
        progress0.setVisibility(View.VISIBLE);

        provisionManager.getEspDevice().provision(ssidValue, passphraseValue, mqttUrlValue,
                                                  new ProvisionListener() {

            @Override
            public void createSessionFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick0.setImageResource(R.drawable.ic_error);
                        tick0.setVisibility(View.VISIBLE);
                        progress0.setVisibility(View.GONE);
                        tvErrAtStep0.setVisibility(View.VISIBLE);
                        tvErrAtStep0.setText(R.string.error_session_creation);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void mqttConfigSent() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick0.setImageResource(R.drawable.ic_checkbox_on);
                        tick0.setVisibility(View.VISIBLE);
                        progress0.setVisibility(View.GONE);
                        tick1.setVisibility(View.GONE);
                        progress1.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void mqttConfigFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick0.setImageResource(R.drawable.ic_error);
                        tick0.setVisibility(View.VISIBLE);
                        progress0.setVisibility(View.GONE);
                        tvErrAtStep0.setVisibility(View.VISIBLE);
                        tvErrAtStep0.setText(R.string.error_prov_step_1);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void wifiConfigSent() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick1.setImageResource(R.drawable.ic_checkbox_on);
                        tick1.setVisibility(View.VISIBLE);
                        progress1.setVisibility(View.GONE);
                        tick2.setVisibility(View.GONE);
                        progress2.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void wifiConfigFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick1.setImageResource(R.drawable.ic_error);
                        tick1.setVisibility(View.VISIBLE);
                        progress1.setVisibility(View.GONE);
                        tvErrAtStep1.setVisibility(View.VISIBLE);
                        tvErrAtStep1.setText(R.string.error_prov_step_1);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void wifiConfigApplied() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick2.setImageResource(R.drawable.ic_checkbox_on);
                        tick2.setVisibility(View.VISIBLE);
                        progress2.setVisibility(View.GONE);
                        tick3.setVisibility(View.GONE);
                        progress3.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick2.setImageResource(R.drawable.ic_error);
                        tick2.setVisibility(View.VISIBLE);
                        progress2.setVisibility(View.GONE);
                        tvErrAtStep2.setVisibility(View.VISIBLE);
                        tvErrAtStep2.setText(R.string.error_prov_step_2);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void provisioningFailedFromDevice(final ESPConstants.ProvisionFailureReason failureReason) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        switch (failureReason) {
                            case AUTH_FAILED:
                                tvErrAtStep3.setText(R.string.error_authentication_failed);
                                break;
                            case NETWORK_NOT_FOUND:
                                tvErrAtStep3.setText(R.string.error_network_not_found);
                                break;
                            case DEVICE_DISCONNECTED:
                            case UNKNOWN:
                                tvErrAtStep3.setText(R.string.error_prov_step_3);
                                break;
                        }
                        tick3.setImageResource(R.drawable.ic_error);
                        tick3.setVisibility(View.VISIBLE);
                        progress3.setVisibility(View.GONE);
                        tvErrAtStep3.setVisibility(View.VISIBLE);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void deviceProvisioningSuccess() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        isProvisioningCompleted = true;
                        tick3.setImageResource(R.drawable.ic_checkbox_on);
                        tick3.setVisibility(View.VISIBLE);
                        progress3.setVisibility(View.GONE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void onProvisioningFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick3.setImageResource(R.drawable.ic_error);
                        tick3.setVisibility(View.VISIBLE);
                        progress3.setVisibility(View.GONE);
                        tvErrAtStep3.setVisibility(View.VISIBLE);
                        tvErrAtStep3.setText(R.string.error_prov_step_3);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }
        });
    }

    private void showLoading() {

        btnOk.setEnabled(false);
        btnOk.setAlpha(0.5f);
    }

    public void hideLoading() {

        btnOk.setEnabled(true);
        btnOk.setAlpha(1f);
    }

    private void showAlertForDeviceDisconnected() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.error_title);
        builder.setMessage(R.string.dialog_msg_ble_device_disconnection);

        // Set up the buttons
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }
    */
}
