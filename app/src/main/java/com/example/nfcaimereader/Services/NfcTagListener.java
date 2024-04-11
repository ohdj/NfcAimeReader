package com.example.nfcaimereader.Services;

public interface NfcTagListener {
    void onTagDetected(String cardType, String cardNumber);
}
