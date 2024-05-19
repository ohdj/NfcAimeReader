package com.example.nfcaimereader.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NfcViewModel extends ViewModel {
    private final MutableLiveData<String> cardType = new MutableLiveData<>();
    private final MutableLiveData<String> cardNumber = new MutableLiveData<>();

    public LiveData<String> getCardType() {
        return cardType;
    }

    public LiveData<String> getCardNumber() {
        return cardNumber;
    }

    public void onTagDiscovered(String type, String number) {
        cardType.postValue(type);
        cardNumber.postValue(number);
    }

    private final MutableLiveData<Integer> nfcState = new MutableLiveData<>();

    public LiveData<Integer> getNfcState() {
        return nfcState;
    }

    // BroadcastReceiver中更新LiveData
    public class NfcStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(intent.getAction())) {
                final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);
                // 直接更新LiveData的值
                nfcState.postValue(state);
            }
        }
    }
}
