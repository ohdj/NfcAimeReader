package com.example.nfcaimereader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nfcaimereader.Connect.SpiceWebSocket;
import com.example.nfcaimereader.Controllers.EditableHostnameAndPort;
import com.example.nfcaimereader.Services.NfcHandler;

public class MainActivity extends AppCompatActivity implements SpiceWebSocket.ConnectionStatusCallback {
    // NFC
    NfcHandler nfcHandler;

    Button buttonConnectServer;
    TextView textViewServerConnectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 编辑服务器
        new EditableHostnameAndPort(this);

        // NFC
        nfcHandler = new NfcHandler(this);

        buttonConnectServer = findViewById(R.id.button_connect_server);
        textViewServerConnectionStatus = findViewById(R.id.textview_server_connection_status);

        SpiceWebSocket.getInstance().setConnectionStatusCallback(this);
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> buttonConnectServer.setText(isConnected ? "断开连接" : "连接服务器"));
        runOnUiThread(() -> textViewServerConnectionStatus.setText(isConnected ? "已连接" : "已断开"));
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
            nfcHandler.checkNfcStatus();
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
        SpiceWebSocket.getInstance().closeWebSocket();
    }
}
