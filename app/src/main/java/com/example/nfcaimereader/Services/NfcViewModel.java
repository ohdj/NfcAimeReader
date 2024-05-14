package com.example.nfcaimereader.Services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NfcViewModel extends ViewModel {
    private final MutableLiveData<Boolean> nfcEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> cardType = new MutableLiveData<>();
    private final MutableLiveData<String> cardNumber = new MutableLiveData<>();

    public LiveData<Boolean> getNfcEnabled() {
        return nfcEnabled;
    }

    public LiveData<String> getCardType() {
        return cardType;
    }

    public LiveData<String> getCardNumber() {
        return cardNumber;
    }

    public void onNfcStateChanged(boolean isEnabled) {
        nfcEnabled.postValue(isEnabled);
    }

    public void onTagDiscovered(String type, String number) {
        cardType.postValue(type);
        cardNumber.postValue(number);
    }
}
