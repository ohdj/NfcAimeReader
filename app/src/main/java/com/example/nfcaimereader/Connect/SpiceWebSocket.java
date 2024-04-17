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

    public byte[] RC4(String password, byte[] data) throws Exception {
        // 创建一个RC4的密钥
        KeyGenerator kgen = KeyGenerator.getInstance("RC4");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(password.getBytes());
        kgen.init(128, sr);
        SecretKey key = kgen.generateKey();
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "RC4");

        // 创建并初始化Cipher对象
        Cipher cipher = Cipher.getInstance("RC4");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        // 使用Cipher对象对数据进行加密
        return cipher.doFinal(data);
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
                    byte[] decoded = bytes.array();

                    // 如果存在密码，使用RC4方法进行解密
                    if (password != null && !password.isEmpty())
                        decoded = RC4(password, bytes.array());

                    // 将解密后的数据转换为字符串
                    String json = new String(decoded, "UTF-8");

                    // 打印JSON数据
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

    public void sendCardId(String password, String idmValue) {
        try {
            String jsonRequest = "{\"id\": 1, \"module\": \"card\", \"function\": \"insert\", \"params\": [0, \"" + idmValue + "\"]}";
            byte[] encoded = jsonRequest.getBytes();

            // 如果存在密码，使用RC4方法进行加密
            if (password != null && !password.isEmpty())
                encoded = RC4(password, jsonRequest.getBytes());

            ByteBuffer buffer = ByteBuffer.wrap(encoded);
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
