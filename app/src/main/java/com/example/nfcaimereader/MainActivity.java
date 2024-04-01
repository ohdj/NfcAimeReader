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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView cardType, cardCode;
    private NfcAdapter nfcAdapter;

    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardType = findViewById(R.id.cardType);
        cardCode = findViewById(R.id.cardCode);

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
                cardType.setText("卡片类型: Felica");

                NfcF nfcF = NfcF.get(tag);
                byte[] idm = tag.getId();
                byte[] pmm = nfcF.getManufacturer();
                byte[] systemCode = nfcF.getSystemCode();

                // 直接转换字节数据为十六进制字符串
                String idmString = bytesToHex(idm);
                String pmmString = bytesToHex(pmm);
                String systemCodeString = bytesToHex(systemCode);

                cardCode.setText("IDm: " + idmString + "\nPMm: " + pmmString + "\nSystemCode: " + systemCodeString);

                return;
            } else if (tech.equals(MifareClassic.class.getName())) {
                // Mifare Classic 卡片
                cardType.setText("卡片类型: Mifare");
                return;
            }
        }
    }

    // 将字节转换为十六进制字符串
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}