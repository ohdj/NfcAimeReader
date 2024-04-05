package com.example.nfcaimereader.Connect;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class SpiceWebSocket {
    private WebSocketClient webSocketClient;

    public void connectWebSocket(String serverUri, final String idmValue) {
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
                sendCardId(idmValue);
            }

            @Override
            public void onMessage(String message) {
                // 接收到消息时触发
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                // 接收到二进制消息时触发
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                // 连接关闭时触发
            }

            @Override
            public void onError(Exception ex) {
                // 连接错误时触发
                ex.printStackTrace();
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
        }
    }
}
