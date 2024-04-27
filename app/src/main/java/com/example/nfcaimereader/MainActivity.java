package com.example.nfcaimereader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nfcaimereader.Client.SpiceClient;
import com.example.nfcaimereader.Controllers.EditableHostnameAndPort;
import com.example.nfcaimereader.Services.NfcEventListener;
import com.example.nfcaimereader.Services.NfcHandler;
import com.example.nfcaimereader.Services.NfcHelper;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity implements SpiceClient.ConnectionStatusCallback, NfcEventListener {
    // NFC初始化
    private NfcHelper nfcHelper;
    private NfcHandler nfcHandler;

    // UI
    private EditText editTextPassword;
    private MaterialButton buttonConnectServer;
    private TextView textViewServerConnectionStatus, textviewNfcStatus, textViewCardType, textViewCardNumber;
    private ProgressBar progressBarNfcDelay;
    private Button buttonNfcSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化UI控件
        initUI();

        // 检测NFC状态
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null && adapter.isEnabled()) {
            progressBarNfcDelay.setVisibility(View.GONE);
            buttonNfcSetting.setVisibility(View.GONE);
            textviewNfcStatus.setText("NFC已开启");
        } else if (adapter != null && !adapter.isEnabled()) {
            progressBarNfcDelay.setVisibility(View.GONE);
            buttonNfcSetting.setVisibility(View.VISIBLE);
            textviewNfcStatus.setText("NFC已关闭");
        }

        // NFC初始化
        nfcHelper = new NfcHelper(this);
        nfcHandler = new NfcHandler(this);

        // 编辑服务器按钮
        new EditableHostnameAndPort(this);

        // WebSocket回调
        SpiceClient.getInstance().setConnectionStatusCallback(this);
    }

    private void initUI() {
        editTextPassword = findViewById(R.id.edittext_password);

        buttonConnectServer = findViewById(R.id.button_connect_server);

        textViewServerConnectionStatus = findViewById(R.id.textview_server_connection_status);
        textviewNfcStatus = findViewById(R.id.textview_nfc_status);
        textViewCardType = findViewById(R.id.textview_card_type);
        textViewCardNumber = findViewById(R.id.textview_card_number);

        progressBarNfcDelay = findViewById(R.id.progressBar_nfc_delay);

        buttonNfcSetting = findViewById(R.id.button_nfc_setting);

        // 设置NFC设置按钮的点击事件
        buttonNfcSetting.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> {
            buttonConnectServer.setText(isConnected ? "断开连接" : "连接服务器");
            buttonConnectServer.setIcon(ContextCompat.getDrawable(this, isConnected ? R.drawable.ic_connect_server_close : R.drawable.ic_connect_server));
        });
        runOnUiThread(() -> textViewServerConnectionStatus.setText(isConnected ? "已连接" : "已断开"));
    }

    @Override
    public void onMessageReceived(final String message) {
        // 确保在主线程上运行
        runOnUiThread(() -> {
            TextView textViewServerResponse = findViewById(R.id.textview_server_response);
            textViewServerResponse.setText(message);
        });
    }

    @Override
    public void onTagDiscovered(String cardType, String cardNumber) {
        // 显示卡片类型和卡号
        textViewCardType.setText("卡片类型: " + cardType);
        textViewCardNumber.setText("卡号: " + cardNumber);
        SpiceClient.getInstance().sendCardId(cardNumber, String.valueOf(editTextPassword.getText()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                nfcHandler.processTag(tag);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcHelper.enableNfcScan(this);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);
                switch (state) {
                    case NfcAdapter.STATE_OFF:
                        // NFC已关闭
                        progressBarNfcDelay.setVisibility(View.GONE);
                        buttonNfcSetting.setVisibility(View.VISIBLE);
                        textviewNfcStatus.setText("NFC已关闭");
                        break;
                    case NfcAdapter.STATE_TURNING_OFF:
                        // NFC正在关闭
                        progressBarNfcDelay.setVisibility(View.VISIBLE);
                        buttonNfcSetting.setVisibility(View.GONE);
                        textviewNfcStatus.setText("NFC正在关闭");
                        break;
                    case NfcAdapter.STATE_ON:
                        // NFC已开启
                        progressBarNfcDelay.setVisibility(View.GONE);
                        buttonNfcSetting.setVisibility(View.GONE);
                        textviewNfcStatus.setText("NFC已开启");
                        break;
                    case NfcAdapter.STATE_TURNING_ON:
                        // NFC正在开启
                        progressBarNfcDelay.setVisibility(View.VISIBLE);
                        buttonNfcSetting.setVisibility(View.GONE);
                        textviewNfcStatus.setText("NFC正在开启");
                        break;
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        nfcHelper.disableNfcScan(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
        // 关闭WebSocket连接
        SpiceClient.getInstance().closeWebSocket();
    }
}
