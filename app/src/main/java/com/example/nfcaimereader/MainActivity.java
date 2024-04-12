package com.example.nfcaimereader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nfcaimereader.Connect.SpiceWebSocket;
import com.example.nfcaimereader.Controllers.EditableHostnameAndPort;
import com.example.nfcaimereader.Services.NfcHandler;
import com.example.nfcaimereader.Services.NfcTagListener;

public class MainActivity extends AppCompatActivity implements NfcTagListener {
    NfcHandler nfcHandler;

    // UI
    private TextView textViewCardType, textViewCardNumber;

    // WebSocket
    private SpiceWebSocket spiceWebSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI
        textViewCardType = findViewById(R.id.textview_card_type);
        textViewCardNumber = findViewById(R.id.textview_card_number);

        nfcHandler = new NfcHandler(this, this);

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
    public void onTagDetected(String cardType, String cardNumber) {
        textViewCardType.setText(cardType);
        textViewCardNumber.setText(cardNumber);
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