package com.example.nfcaimereader;

import android.content.Intent;
import android.os.Bundle;

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
        if (nfcHandler != null) {
            nfcHandler.enableForegroundDispatch();
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