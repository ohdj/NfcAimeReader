package com.example.nfcaimereader.Connect;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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

    // RC4类，包含初始化key，并加解密数据的方法，需要通过构造函数传入key
    public class RC4 {
        SecretKeySpec key;

        public RC4(byte[] key) {
            initialize(key);
        }

        public void initialize(byte[] key) {
            this.key = new SecretKeySpec(key, "RC4");
        }

        public byte[] encrypt(byte[] plaintext) throws Exception {
            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(Cipher.ENCRYPT_MODE, this.key);
            return cipher.doFinal(plaintext);
        }
    }

    public void connectWebSocket(String serverUri, String password) {
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
                try {
                    byte[] decrypted;
                    if (password == null || password.isEmpty()) {
                        decrypted = bytes.array();
                    } else {
                        RC4 rc4 = new RC4(password.getBytes());
                        decrypted = rc4.encrypt(bytes.array());
                    }
                    String json = new String(decrypted, "UTF-8");
                    Log.d("WebSocket", "接收到消息: " + json);
                } catch (Exception e) {
                    Log.e("WebSocket", "解密出错", e);
                }
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
