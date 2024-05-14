package com.example.nfcaimereader.Services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
import com.example.nfcaimereader.MainActivity;

public class NfcManager {
    private final NfcAdapter nfcAdapter;
    private final PendingIntent pendingIntent;
    private final IntentFilter[] intentFilters;
    private final String[][] techLists;

    public NfcManager(Context context) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);

        // 初始化pendingIntent, intentFilters和techLists
        pendingIntent = PendingIntent.getActivity(
                context, 0, new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
        );
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFilters = new IntentFilter[]{tech};
        techLists = new String[][]{
                new String[]{"android.nfc.tech.NfcF"},
                new String[]{"android.nfc.tech.MifareClassic"}
        };
    }

    public boolean isNfcSupported() {
        return nfcAdapter != null;
    }

    public boolean isNfcEnabled() {
        return isNfcSupported() && nfcAdapter.isEnabled();
    }

    public void enableForegroundDispatch(Activity activity) {
        if (isNfcSupported()) {
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFilters, techLists);
        }
    }

    public void disableForegroundDispatch(Activity activity) {
        if (isNfcSupported()) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    public void processTag(Tag tag, NfcViewModel viewModel) {
        byte[] tagId = tag.getId();                                         // 获取标签ID
        String cardType = parseCardType(tag.getTechList());                 // 解析卡片类型
        StringBuilder cardNumber = new StringBuilder(bytesToHex(tagId));    // 将IDm转换为十六进制字符串，并将其作为卡号

        // 卡号长度小于16位时，进入循环补0
        while (cardNumber.length() < 16) {
            cardNumber.insert(0, "0");
        }
        viewModel.onTagDiscovered(cardType, cardNumber.toString());
    }

    private String parseCardType(String[] techList) {
        // 遍历所有技术类型，返回对应的类型字符串
        for (String tech : techList) {
            if (tech.equals(NfcF.class.getName())) {
                // NfcF类型的卡片
                return "Felica";
            } else if (tech.equals(MifareClassic.class.getName())) {
                // Mifare Classic类型的卡片
                return "Mifare Classic";
            }
        }
        return "未知卡片类型";
    }

    // 将字节数组转换为十六进制字符串
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
