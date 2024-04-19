package com.example.nfcaimereader.Services;

public interface NfcEventListener {
    void onTagDiscovered(String cardType, String cardNumber);
}
