package com.example.nfcaimereader.Services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;

import com.example.nfcaimereader.MainActivity;

public class NfcHelper {
    // NFC
    private final NfcAdapter nfcAdapter;

    private final PendingIntent pendingIntent;
    private final IntentFilter[] intentFiltersArray;
    private final String[][] techListsArray;

    public NfcHelper(Activity activity) {
        // 检查设备是否支持NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

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

    public void enableNfcScan(Activity activity) {
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    public void disableNfcScan(Activity activity) {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    public boolean isNfcEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }
}
