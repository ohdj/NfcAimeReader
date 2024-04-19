package com.example.nfcaimereader;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nfcaimereader.Connect.SpiceWebSocket;
import com.example.nfcaimereader.Controllers.EditableHostnameAndPort;
import com.example.nfcaimereader.Services.NfcEventListener;
import com.example.nfcaimereader.Services.NfcHandler;

public class MainActivity extends AppCompatActivity implements SpiceWebSocket.ConnectionStatusCallback, NfcEventListener {
    // NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcHandler nfcHandler;

    private EditText editTextPassword;
    private Button buttonConnectServer;
    private TextView textViewServerConnectionStatus, textviewNfcStatus, textViewCardType, textViewCardNumber;
    private ProgressBar progressBarNfcDelay;
    private Button buttonNfcSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查设备是否支持NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
        );
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        intentFiltersArray = new IntentFilter[]{tech};

        techListsArray = new String[][]{
                new String[]{"android.nfc.tech.NfcF"},
                new String[]{"android.nfc.tech.MifareClassic"}
        };

        nfcHandler = new NfcHandler(this);

        // UI
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

        // 编辑服务器
        new EditableHostnameAndPort(this);

        SpiceWebSocket.getInstance().setConnectionStatusCallback(this);
    }

    @Override
    public void onTagDiscovered(String cardType, String cardNumber) {
        // 显示卡片类型和卡号
        textViewCardType.setText("卡片类型: " + cardType);
        textViewCardNumber.setText("卡号: " + cardNumber);
        SpiceWebSocket.getInstance().sendCardId(cardNumber, String.valueOf(editTextPassword.getText()));
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> buttonConnectServer.setText(isConnected ? "断开连接" : "连接服务器"));
        runOnUiThread(() -> textViewServerConnectionStatus.setText(isConnected ? "已连接" : "已断开"));
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
        if (nfcHandler != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
            checkNfcStatus();
        }
    }

    private CountDownTimer countDownTimer;

    public void checkNfcStatus() {
        // NFC不可用
        if (nfcAdapter == null) return;

        buttonNfcSetting.setVisibility(View.GONE);

        // NFC已启用
        if (nfcAdapter.isEnabled()) {
            textviewNfcStatus.setText("NFC已启用");
            return;
        }

        progressBarNfcDelay.setVisibility(View.VISIBLE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                // 更新文本视图的倒计时
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                textviewNfcStatus.setText("NFC状态检测中..." + secondsLeft);
            }

            public void onFinish() {
                // 隐藏进度条
                progressBarNfcDelay.setVisibility(View.GONE);

                if (nfcAdapter.isEnabled()) {
                    // NFC已启用
                    buttonNfcSetting.setVisibility(View.GONE);
                    textviewNfcStatus.setText("NFC已启用");
                } else {
                    // 提示用户在设置中启用NFC
                    buttonNfcSetting.setVisibility(View.VISIBLE);
                    textviewNfcStatus.setText("NFC功能未启用，请在设置中开启");
                }
            }
        }.start();

        // Thread statusCheckThread = new Thread(() -> {
        //     try {
        //         boolean initialStatus = nfcAdapter.isEnabled();
        //         boolean currentStatus;
        //         int retries = 3; // 最多检查次数
        //         do {
        //             Thread.sleep(1000); // 1秒暂停时间，减少轮询频率
        //             currentStatus = nfcAdapter.isEnabled();
        //             retries--;
        //         } while (initialStatus == currentStatus && retries > 0);
        //         activity.runOnUiThread(this::updateNfcStatus);
        //     } catch (InterruptedException e) {
        //         Thread.currentThread().interrupt();
        //     }
        // });
        // statusCheckThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcHandler != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭WebSocket连接
        SpiceWebSocket.getInstance().closeWebSocket();
    }
}
