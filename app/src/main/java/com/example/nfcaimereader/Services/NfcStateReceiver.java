package com.example.nfcaimereader.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

public class NfcStateReceiver extends BroadcastReceiver {
    private final NfcStateChangeListener listener;

    public NfcStateReceiver(NfcStateChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(action)) {
            final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);
            // 通知监听器状态变化
            if (listener != null) {
                listener.onNfcStateChanged(state);
            }
        }
    }

    public interface NfcStateChangeListener {
        void onNfcStateChanged(int state);
    }
}
