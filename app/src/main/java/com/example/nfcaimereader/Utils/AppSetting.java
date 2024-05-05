package com.example.nfcaimereader.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSetting {
    private static final String PREFERENCES = "AppSettings";
    private static final String KEY_HOSTNAME = "hostname";
    private static final String KEY_PORT = "port";
    private static final String KEY_PASSWORD = "password";
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
}
