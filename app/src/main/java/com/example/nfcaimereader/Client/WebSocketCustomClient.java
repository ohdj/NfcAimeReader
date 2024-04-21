package com.example.nfcaimereader.Client;

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
    }

    @Override
    public void onMessage(String message) {
        // 接收到消息时触发
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        // 接收到二进制消息时触发
        try {
            byte[] message;
            // if (password == null || password.isEmpty()) {
                message = bytes.array();
            // } else {
            //     RC4 rc4 = new RC4(password.getBytes());
            //     message = rc4.encrypt(bytes.array());
            // }
            String json = new String(message, "UTF-8");
            SpiceClient.getInstance().updateWebSocketResponse("WebSocket接收到二进制消息: " + json);
        } catch (Exception e) {
            SpiceClient.getInstance().updateWebSocketResponse("WebSocket解密出错" + e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // 连接关闭时触发
        callback.onConnectionStatusChanged(false);
    }

    @Override
    public void onError(Exception ex) {
        // 连接错误时触发
        callback.onConnectionStatusChanged(false);
        SpiceClient.getInstance().updateWebSocketResponse("WebSocket连接错误" + ex);
    }
}
