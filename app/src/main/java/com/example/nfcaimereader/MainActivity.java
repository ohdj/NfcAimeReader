package com.example.nfcaimereader;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nfcaimereader.Connect.SpiceWebSocket;
import com.example.nfcaimereader.Controllers.EditableHostnameAndPort;
import com.example.nfcaimereader.Drawables.UiUpdater;
import com.example.nfcaimereader.Drawables.UiUpdaterManager;

public class MainActivity extends AppCompatActivity {
    private UiUpdater uiUpdater;

    // NFC相关
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    // WebSocket相关
    private SpiceWebSocket spiceWebSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiUpdater = new UiUpdaterManager(this);

        new EditableHostnameAndPort(this, uiUpdater);

        // 检查设备是否支持NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // 停用NFC功能，因为设备不支持
            Toast.makeText(this, "NFC不可用", Toast.LENGTH_LONG).show();
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            // 提示用户在设置中启用NFC
            Toast.makeText(this, "请在设置中启用NFC功能", Toast.LENGTH_LONG).show();

            // 跳转到设置页面
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        } else {
            pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE
            );

            IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

            intentFiltersArray = new IntentFilter[]{tech};
            techListsArray = new String[][]{
                    new String[]{"android.nfc.tech.NfcF"},
                    new String[]{"android.nfc.tech.MifareClassic"}
            };
        }

        spiceWebSocket = new SpiceWebSocket();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 如果是TECH_DISCOVERED中包含的，则处理它
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            processIntent(intent);
        } else {
            Toast.makeText(this, "不是NfcF或MifareClassic类型的卡片", Toast.LENGTH_LONG).show();
        }
    }

    // 处理NFC
    private void processIntent(Intent intent) {
        // 获取标签
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            Toast.makeText(this, "NFC标签无法识别", Toast.LENGTH_LONG).show();
            return;
        }

        for (String tech : tag.getTechList()) {
            if (tech.equals(NfcF.class.getName())) {
                // NFC F (Felica) 卡片
                uiUpdater.setCardType("卡片类型: Felica");

                // 得到Felica卡IDm值后，将字节数据转换为十六进制字符串
                byte[] idm = tag.getId();
                StringBuilder sb = new StringBuilder();
                for (byte b : idm) {
                    sb.append(String.format("%02X", b));
                }
                String idmString = sb.toString();

                // 设置UI上显示的卡号
                uiUpdater.setCardNumber("卡号：" + idmString);

//                if (edittext_hostname.getText().toString().isEmpty() || edittext_port.getText().toString().isEmpty()) {
//                    Toast.makeText(this, "请填写HostName以及Port", Toast.LENGTH_LONG).show();
//                    return;
//                }
//
//                // 构建WebSocket服务器的URI
//                String webSocketUri = edittext_hostname + ":" + edittext_port;
//
//                // 在获取到IDm值后调用SpiceWebSocket连接WebSocket
//                if (!idmString.isEmpty()) {
//                    spiceWebSocket.connectWebSocket(webSocketUri, idmString);
//                }

                return;
            } else if (tech.equals(MifareClassic.class.getName())) {
                // Mifare Classic 卡片
                uiUpdater.setCardType("卡片类型: Mifare");
                return;
            }
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