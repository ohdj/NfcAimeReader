package com.example.nfcaimereader.Client;

import android.util.Log;

import com.example.nfcaimereader.Client.Utils.RC4;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class WebSocketCustomClient extends WebSocketClient {
    private final SpiceClient.ConnectionStatusCallback callback;
    private final String password;

    public WebSocketCustomClient(URI serverUri, SpiceClient.ConnectionStatusCallback callback, String password) {
        super(serverUri);
        this.callback = callback;
        this.password = password;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        // 连接开启时触发
        callback.onConnectionStatusChanged(true);
        Log.d("WebSocket", "连接开启");
    }

    @Override
    public void onMessage(String message) {
        // 接收到消息时触发
        Log.d("WebSocket", "接收到消息");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        // 接收到二进制消息时触发
        try {
            byte[] decrypted;
            if (password == null || password.isEmpty()) {
                decrypted = bytes.array();
            } else {
                RC4 rc4 = new RC4(password.getBytes());
                decrypted = rc4.encrypt(bytes.array());
            }
            String json = new String(decrypted, "UTF-8");
            Log.d("WebSocket", "接收到二进制消息: " + json);
        } catch (Exception e) {
            Log.e("WebSocket", "解密出错", e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // 连接关闭时触发
        callback.onConnectionStatusChanged(false);
        Log.d("WebSocket", "连接关闭");
    }

    @Override
    public void onError(Exception ex) {
        // 连接错误时触发
        callback.onConnectionStatusChanged(false);
        Log.d("WebSocket", "连接错误" + ex);
    }
}
