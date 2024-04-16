package com.example.nfcaimereader.Connect;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class SpiceWebSocket {
    private static SpiceWebSocket instance = null;
    private WebSocketClient webSocketClient;

    private SpiceWebSocket() {
    }

    // 公开的获取单例实例的方法
    public static synchronized SpiceWebSocket getInstance() {
        if (instance == null) {
            instance = new SpiceWebSocket();
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

    // 当WebSocket的连接状态改变时，调用这个方法
    private void changeConnectionStatus(boolean isConnected) {
        if (callback != null) {
            callback.onConnectionStatusChanged(isConnected);
        }
    }

    public void connectWebSocket(String serverUri) {
        URI uri;
        try {
            uri = new URI(serverUri);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                // 连接开启时触发
                changeConnectionStatus(true);
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
                Log.d("WebSocket", "接收到二进制消息");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                // 连接关闭时触发
                changeConnectionStatus(false);
                Log.d("WebSocket", "连接关闭");
            }

            @Override
            public void onError(Exception ex) {
                // 连接错误时触发
                changeConnectionStatus(false);
                Log.d("WebSocket", "连接错误");
            }
        };

        webSocketClient.connect();
    }

    public void sendCardId(String idmValue) {
        // 构建请求数据
        String jsonRequest = "{\"id\": 1, \"module\": \"card\", \"function\": \"insert\", \"params\": [0, \"" + idmValue + "\"]}";

        // 将字符串消息转换为二进制数据
        ByteBuffer buffer = ByteBuffer.wrap(jsonRequest.getBytes());

        // 通过WebSocket客户端发送
        webSocketClient.send(buffer);
    }

    public void closeWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
    }
}
