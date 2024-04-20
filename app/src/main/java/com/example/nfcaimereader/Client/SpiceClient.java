package com.example.nfcaimereader.Client;

import android.util.Log;

import com.example.nfcaimereader.Client.Utils.RC4;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.nio.ByteBuffer;

public class SpiceClient {
    private static SpiceClient instance = null;
    private WebSocketClient webSocketClient;

    // 公开的获取单例实例的方法
    public static synchronized SpiceClient getInstance() {
        if (instance == null) {
            instance = new SpiceClient();
        }
        return instance;
    }

    // 回调
    private ConnectionStatusCallback callback;

    public interface ConnectionStatusCallback {
        void onConnectionStatusChanged(boolean isConnected);
    }

    public void setConnectionStatusCallback(ConnectionStatusCallback callback) {
        this.callback = callback;
    }

    public void connectWebSocket(String serverUri, String password) {
        URI uri;
        try {
            uri = new URI(serverUri);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketCustomClient(uri, callback, password);
        webSocketClient.connect();
    }

    public void sendCardId(String idmValue, String password) {
        try {
            String jsonRequest = "{\"id\": 1, \"module\": \"card\", \"function\": \"insert\", \"params\": [0, \"" + idmValue + "\"]}";
            byte[] encrypted;
            if (password == null || password.isEmpty()) {
                encrypted = jsonRequest.getBytes("UTF-8");
            } else {
                RC4 rc4 = new RC4(password.getBytes());
                encrypted = rc4.encrypt(jsonRequest.getBytes("UTF-8"));
            }
            ByteBuffer buffer = ByteBuffer.wrap(encrypted);
            webSocketClient.send(buffer);
        } catch (Exception e) {
            Log.e("WebSocket", "加密发生错误", e);
        }
    }

    public void closeWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
    }
}
