package com.example.nfcaimereader.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class AppSetting {
    private static final String PREFERENCES = "AppSettings";
    private static final String KEY_HOSTNAME = "hostname";
    private static final String KEY_PORT = "port";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_CARD_NUMBERS = "card_numbers";
    private final SharedPreferences sharedPreferences;

    public AppSetting(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public void saveServerSettings(String hostname, String port, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_HOSTNAME, hostname);
        editor.putString(KEY_PORT, port);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public String getHostname() {
        return sharedPreferences.getString(KEY_HOSTNAME, "");
    }

    public String getPort() {
        return sharedPreferences.getString(KEY_PORT, "");
    }

    public String getPassword() {
        return sharedPreferences.getString(KEY_PASSWORD, "");
    }

    public boolean hasServerSettings() {
        return !getHostname().isEmpty() && !getPort().isEmpty();
    }

    public void saveCardNumber(String cardNumber) {
        Set<String> cardNumbers = getCardNumbers();
        cardNumbers.add(cardNumber);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_CARD_NUMBERS, cardNumbers);
        editor.apply();
    }

    public Set<String> getCardNumbers() {
        return new HashSet<>(sharedPreferences.getStringSet(KEY_CARD_NUMBERS, new HashSet<>()));
    }

    public void deleteCardNumber(String cardNumber) {
        Set<String> cardNumbers = getCardNumbers(); // 获取当前所有卡号
        cardNumbers.remove(cardNumber); // 从集合中删除卡号
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_CARD_NUMBERS, cardNumbers); // 重新保存修改后的集合
        editor.apply(); // 提交修改
    }

    // 修改卡号
    public void editCardNumber(String oldCardNumber, String newCardNumber) {
        Set<String> cardNumbers = getCardNumbers();
        if (cardNumbers.contains(oldCardNumber)) {
            cardNumbers.remove(oldCardNumber); // 删除旧的卡号
            cardNumbers.add(newCardNumber);    // 添加新的卡号
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(KEY_CARD_NUMBERS, cardNumbers); // 保存变更
            editor.apply();
        }
    }
}
