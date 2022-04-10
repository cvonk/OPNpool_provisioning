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

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ContentLoadingProgressBar;

import com.coertvonk.opnpool.AppConstants;
import com.coertvonk.provisioning.DeviceConnectionEvent;
import com.coertvonk.pool.R;
import com.coertvonk.provisioning.ESPConstants;
import com.coertvonk.provisioning.ESPProvisionManager;
import com.coertvonk.provisioning.listeners.ProvisionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ProvisionActivity extends AppCompatActivity {

    private static final String TAG = ProvisionActivity.class.getSimpleName();

    private TextView tvTitle, tvBack, tvCancel;
    private ImageView tick0, tick1, tick2, tick3, tick4, tick5, tick6;
    private ContentLoadingProgressBar progress0, progress1, progress2, progress3, progress4, progress5, progress6;
    private ProgressBar progressbar2, progressbar3, progressbar4, progressbar5, progressbar6;
    private TextView tvErrAtStep0, tvErrAtStep1, tvErrAtStep2, tvErrAtStep3, tvProvError, tvErrAtStep4, tvErrAtStep5;

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
            goForMain();
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
        tick4 = findViewById(R.id.iv_tick_4);
        tick5 = findViewById(R.id.iv_tick_5);
        tick6 = findViewById(R.id.iv_tick_6);
        progress0 = findViewById(R.id.prov_progress_0);
        progress1 = findViewById(R.id.prov_progress_1);
        progress2 = findViewById(R.id.prov_progress_2);
        progress3 = findViewById(R.id.prov_progress_3);
        progress4 = findViewById(R.id.prov_progress_4);
        progress5 = findViewById(R.id.prov_progress_5);
        progress6 = findViewById(R.id.prov_progress_6);
        progressbar2 = findViewById(R.id.prov_progressbar_2);
        progressbar3 = findViewById(R.id.prov_progressbar_3);
        progressbar4 = findViewById(R.id.prov_progressbar_4);
        progressbar5 = findViewById(R.id.prov_progressbar_5);
        progressbar6 = findViewById(R.id.prov_progressbar_6);

        tvErrAtStep0 = findViewById(R.id.tv_prov_error_0);
        tvErrAtStep1 = findViewById(R.id.tv_prov_error_1);
        tvErrAtStep2 = findViewById(R.id.tv_prov_error_2);
        tvErrAtStep3 = findViewById(R.id.tv_prov_error_3);
        tvErrAtStep4 = findViewById(R.id.tv_prov_error_4);
        tvErrAtStep5 = findViewById(R.id.tv_prov_error_5);
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
        progress0.setVisibility(VISIBLE);

        provisionManager.getEspDevice().provision(ssidValue, passphraseValue, mqttUrlValue,
                                                  new ProvisionListener() {

            @Override
            public void createSessionFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick0.setImageResource(R.drawable.ic_error);
                        tick0.setVisibility(VISIBLE);
                        progress0.setVisibility(View.GONE);
                        tvErrAtStep0.setVisibility(VISIBLE);
                        tvErrAtStep0.setText(R.string.error_session_creation);
                        tvProvError.setVisibility(VISIBLE);
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
                        tick0.setVisibility(VISIBLE);
                        progress0.setVisibility(View.GONE);
                        tick1.setVisibility(View.GONE);
                        progress1.setVisibility(VISIBLE);
                    }
                });
            }

            @Override
            public void mqttConfigFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick0.setImageResource(R.drawable.ic_error);
                        tick0.setVisibility(VISIBLE);
                        progress0.setVisibility(View.GONE);
                        tvErrAtStep0.setVisibility(VISIBLE);
                        tvErrAtStep0.setText(R.string.error_prov_step_1);
                        tvProvError.setVisibility(VISIBLE);
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
                        tick1.setVisibility(VISIBLE);
                        progress1.setVisibility(View.GONE);
                        tick2.setVisibility(View.GONE);
                        progress2.setVisibility(VISIBLE);
                        progressbar2.setVisibility(VISIBLE);
                    }
                });
            }

            @Override
            public void wifiConfigFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick1.setImageResource(R.drawable.ic_error);
                        tick1.setVisibility(VISIBLE);
                        progress1.setVisibility(View.GONE);
                        tvErrAtStep1.setVisibility(VISIBLE);
                        tvErrAtStep1.setText(R.string.error_prov_step_1);
                        tvProvError.setVisibility(VISIBLE);
                        hideLoading();
                    }
                });
            }

            public void moveProgressBar(ProgressBar progressbar) {

                new Thread(new Runnable() {
                    @Override public void run() {
                        int ii = 0;
                        int ii_max = progressbar.getMax();
                        while (ii++ < ii_max && progressbar.getVisibility() == VISIBLE) {
                            int finalIi = ii;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressbar.setProgress(finalIi);
                                }
                            });

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void wifiConfigApplied() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick2.setImageResource(R.drawable.ic_checkbox_on);
                        tick2.setVisibility(VISIBLE);
                        progress2.setVisibility(View.GONE);
                        progressbar2.setVisibility(INVISIBLE);
                        tick3.setVisibility(View.GONE);
                        progress3.setVisibility(VISIBLE);
                        progressbar3.setVisibility(VISIBLE);
                        int ii_max = progressbar3.getMax();
                        moveProgressBar(progressbar3);
                    }
                });

                new Thread(new Runnable() {
                    public void run() {
                    }
                }).start();
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick2.setImageResource(R.drawable.ic_error);
                        tick2.setVisibility(VISIBLE);
                        progress2.setVisibility(View.GONE);
                        progressbar2.setVisibility(INVISIBLE);
                        tvErrAtStep2.setVisibility(VISIBLE);
                        tvErrAtStep2.setText(R.string.error_prov_step_2);
                        tvProvError.setVisibility(VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void wifiConfigApplyFailedFromDevice(final ESPConstants.ProvisionFailureReason failureReason) {

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
                        tick3.setVisibility(VISIBLE);
                        progress3.setVisibility(View.GONE);
                        progressbar3.setVisibility(INVISIBLE);
                        tvErrAtStep3.setVisibility(VISIBLE);
                        tvProvError.setVisibility(VISIBLE);
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
                        tick3.setVisibility(VISIBLE);
                        progress3.setVisibility(View.GONE);
                        progressbar3.setVisibility(INVISIBLE);
                        tick4.setVisibility(View.GONE);
                        progress4.setVisibility(VISIBLE);
                        progressbar4.setVisibility(VISIBLE);
                        moveProgressBar(progressbar4);
                    }
                });
            }

            @Override
            public void mqttConfigApplied() {

                runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    tick4.setImageResource(R.drawable.ic_checkbox_on);
                    tick4.setVisibility(VISIBLE);
                    progress4.setVisibility(View.GONE);
                    progressbar4.setVisibility(INVISIBLE);
                    tick5.setVisibility(View.GONE);
                    progress5.setVisibility(VISIBLE);
                    progressbar5.setVisibility(VISIBLE);
                    moveProgressBar(progressbar5);
                }
                });
            }

            @Override
            public void mqttConfigApplyFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick4.setImageResource(R.drawable.ic_error);
                        tick4.setVisibility(VISIBLE);
                        progress4.setVisibility(View.GONE);
                        progressbar4.setVisibility(INVISIBLE);
                        tvErrAtStep4.setVisibility(VISIBLE);
                        tvErrAtStep4.setText(R.string.error_prov_step_2);
                        tvProvError.setVisibility(VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void otaUpdateApplied() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick5.setImageResource(R.drawable.ic_checkbox_on);
                        tick5.setVisibility(VISIBLE);
                        progress5.setVisibility(View.GONE);
                        progressbar5.setVisibility(INVISIBLE);
                        tick6.setVisibility(View.GONE);
                        progress6.setVisibility(VISIBLE);
                        progressbar6.setVisibility(VISIBLE);
                        moveProgressBar(progressbar6);
                    }
                });
            }

            @Override
            public void otaUpdateApplyFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick5.setImageResource(R.drawable.ic_error);
                        tick5.setVisibility(VISIBLE);
                        progress5.setVisibility(View.GONE);
                        progressbar5.setVisibility(INVISIBLE);
                        tvErrAtStep5.setVisibility(VISIBLE);
                        tvErrAtStep5.setText(R.string.error_prov_step_2);
                        tvProvError.setVisibility(VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void rebootApplied() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick6.setImageResource(R.drawable.ic_checkbox_on);
                        tick6.setVisibility(VISIBLE);
                        progress6.setVisibility(View.GONE);
                        progressbar6.setVisibility(INVISIBLE);
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

    private void goForMain() {

        finish();
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainIntent);
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
}
