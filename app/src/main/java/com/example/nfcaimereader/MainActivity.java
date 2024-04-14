package com.example.nfcaimereader;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nfcaimereader.Connect.SpiceWebSocket;
import com.example.nfcaimereader.Controllers.EditableHostnameAndPort;
import com.example.nfcaimereader.Services.NfcHandler;

public class MainActivity extends AppCompatActivity {
    NfcHandler nfcHandler;

    // WebSocket
    private SpiceWebSocket spiceWebSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcHandler = new NfcHandler(this);

        // 编辑服务器
        new EditableHostnameAndPort(this);

        spiceWebSocket = new SpiceWebSocket();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        nfcHandler.handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView textviewNfcStatus = findViewById(R.id.textview_nfc_status);
        ProgressBar progressBarNfcDelay = findViewById(R.id.progressBar_nfc_delay);
        Button buttonNfcSetting = findViewById(R.id.button_nfc_setting);

        progressBarNfcDelay.setVisibility(View.VISIBLE);
        buttonNfcSetting.setVisibility(View.GONE);

        if (nfcHandler != null) {
            nfcHandler.enableForegroundDispatch();

            new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                    // 更新文本视图的倒计时
                    textviewNfcStatus.setText("NFC状态检测中..." + secondsLeft);
                }

                public void onFinish() {
                    // 隐藏进度条
                    progressBarNfcDelay.setVisibility(View.GONE);
                    nfcHandler.updateNfcStatus();
                }
            }.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcHandler != null) {
            nfcHandler.disableForegroundDispatch();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭WebSocket连接
        if (spiceWebSocket != null) {
            spiceWebSocket.closeWebSocket();
        }
    }
}
