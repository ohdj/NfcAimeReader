package com.example.nfcaimereader.Services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nfcaimereader.MainActivity;
import com.example.nfcaimereader.R;

public class NfcHandler {
    private final Activity activity;

    // UI
    private final TextView textviewNfcStatus, textViewCardType, textViewCardNumber;

    // NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    public NfcHandler(Activity activity) {
        this.activity = activity;

        // UI
        textviewNfcStatus = activity.findViewById(R.id.textview_nfc_status);
        textViewCardType = activity.findViewById(R.id.textview_card_type);
        textViewCardNumber = activity.findViewById(R.id.textview_card_number);

        initializeNFC();
    }

    public void initializeNFC() {
        // 检查设备是否支持NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter == null) {
            // NFC不可用
            Toast.makeText(activity, "NFC不可用，或是设备不支持NFC", Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

        setupForegroundDispatchSystem();

        if (!nfcAdapter.isEnabled()) {
            // 提示用户在设置中启用NFC
            Toast.makeText(activity, "请在设置中启用NFC功能", Toast.LENGTH_LONG).show();
            activity.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
    }

    private void setupForegroundDispatchSystem() {
        pendingIntent = PendingIntent.getActivity(
                activity, 0, new Intent(activity, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
        );

        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        intentFiltersArray = new IntentFilter[]{tech};
        techListsArray = new String[][]{
                new String[]{"android.nfc.tech.NfcF"},
                new String[]{"android.nfc.tech.MifareClassic"}
        };
    }

    public void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag == null) {
                Toast.makeText(activity, "NFC标签无法识别", Toast.LENGTH_LONG).show();
                return;
            }
            processTag(tag);
        } else {
            Toast.makeText(activity, "不是NfcF或Mifare Classic类型的卡片", Toast.LENGTH_LONG).show();
        }
    }

    private void processTag(Tag tag) {
        StringBuilder cardType = new StringBuilder();
        StringBuilder cardNumber = new StringBuilder();

        // 检索支持的技术列表
        String[] techList = tag.getTechList();
        for (String tech : techList) {
            if (tech.equals(NfcF.class.getName())) {
                // 处理NfcF类型的卡片
                cardType.append("卡片类型: Felica");

                byte[] tagId = tag.getId();
                cardNumber.append("卡号: ").append(bytesToHex(tagId));

                break;
            } else if (tech.equals(MifareClassic.class.getName())) {
                // 处理Mifare Classic类型的卡片
                cardType.append("卡片类型: Mifare Classic");

                byte[] tagId = tag.getId();
                cardNumber.append("卡号: ").append(bytesToHex(tagId));

                break;
            }
        }

        textViewCardType.setText(cardType);
        textViewCardNumber.setText(cardNumber);
    }

    // 将字节数组转换为十六进制字符串
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public void enableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    public void disableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    public void updateNfcStatus() {
        if (nfcAdapter != null) {
            if (nfcAdapter.isEnabled()) {
                textviewNfcStatus.setText("NFC已启用");
            } else {
                textviewNfcStatus.setText("NFC已禁用");
            }
        } else {
            textviewNfcStatus.setText("此设备不支持NFC");
        }
    }
}
