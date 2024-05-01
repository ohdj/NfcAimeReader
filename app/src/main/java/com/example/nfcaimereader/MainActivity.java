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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nfcaimereader.Client.SpiceClient;
import com.example.nfcaimereader.Controllers.EditableHostnameAndPort;
import com.example.nfcaimereader.Services.NfcEventListener;
import com.example.nfcaimereader.Services.NfcHandler;
import com.example.nfcaimereader.Services.NfcHelper;
import com.example.nfcaimereader.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SpiceClient.ConnectionStatusCallback, NfcEventListener {
    // NFC初始化
    private NfcHelper nfcHelper;
    private NfcHandler nfcHandler;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 检测NFC状态
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null && adapter.isEnabled()) {
            binding.progressbarNfcDelay.setVisibility(View.GONE);
            binding.buttonNfcSetting.setVisibility(View.GONE);
            binding.textviewNfcStatus.setText("NFC已开启");
        } else if (adapter != null && !adapter.isEnabled()) {
            binding.progressbarNfcDelay.setVisibility(View.GONE);
            binding.buttonNfcSetting.setVisibility(View.VISIBLE);
            binding.textviewNfcStatus.setText("NFC已关闭");
        }

        // NFC初始化
        nfcHelper = new NfcHelper(this);
        nfcHandler = new NfcHandler(this);

        // 编辑服务器按钮
        new EditableHostnameAndPort(this);

        // WebSocket回调
        SpiceClient.getInstance().setConnectionStatusCallback(this);

        // 设置NFC设置按钮的点击事件
        binding.buttonNfcSetting.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> {
            binding.buttonConnectServer.setText(isConnected ? "断开连接" : "连接服务器");
            binding.buttonConnectServer.setIcon(ContextCompat.getDrawable(this, isConnected ? R.drawable.ic_connect_server_close : R.drawable.ic_connect_server));
        });
        runOnUiThread(() -> binding.textviewServerConnectionStatus.setText(isConnected ? "已连接" : "已断开"));
    }

    @Override
    public void onMessageReceived(final String message) {
        // 确保在主线程上运行
        runOnUiThread(() -> binding.textviewServerResponse.setText(message));
    }

    @Override
    public void onTagDiscovered(String cardType, String cardNumber) {
        // 显示卡片类型和卡号
        binding.textviewCardType.setText("卡片类型: " + cardType);
        binding.textviewCardNumber.setText("卡号: " + cardNumber);
        SpiceClient.getInstance().sendCardId(cardNumber, String.valueOf(binding.edittextPassword.getText()));
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
                        binding.progressbarNfcDelay.setVisibility(View.GONE);
                        binding.buttonNfcSetting.setVisibility(View.VISIBLE);
                        binding.textviewNfcStatus.setText("NFC已关闭");
                        break;
                    case NfcAdapter.STATE_TURNING_OFF:
                        // NFC正在关闭
                        binding.progressbarNfcDelay.setVisibility(View.VISIBLE);
                        binding.buttonNfcSetting.setVisibility(View.GONE);
                        binding.textviewNfcStatus.setText("NFC正在关闭");
                        break;
                    case NfcAdapter.STATE_ON:
                        // NFC已开启
                        binding.progressbarNfcDelay.setVisibility(View.GONE);
                        binding.buttonNfcSetting.setVisibility(View.GONE);
                        binding.textviewNfcStatus.setText("NFC已开启");
                        break;
                    case NfcAdapter.STATE_TURNING_ON:
                        // NFC正在开启
                        binding.progressbarNfcDelay.setVisibility(View.VISIBLE);
                        binding.buttonNfcSetting.setVisibility(View.GONE);
                        binding.textviewNfcStatus.setText("NFC正在开启");
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
