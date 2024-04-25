package com.example.nfcaimereader.Services;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;

public class NfcHandler {
    private final NfcEventListener eventListener;

    public NfcHandler(NfcEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void processTag(Tag tag) {
        // 获取标签ID
        byte[] tagId = tag.getId();
        // 解析卡片类型
        String cardType = parseCardType(tag.getTechList());
        // 将标签ID转换为十六进制字符串，作为卡号
        String cardNumber = bytesToHex(tagId);
        //卡号长度小于16时进入循环补0
        while (cardNumber.length()<16){
            cardNumber = "0" + cardNumber;
        }
        // 通过事件监听器响应数据
        eventListener.onTagDiscovered(cardType, cardNumber);
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
