package com.example.nfcaimereader.Client.Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RC4 {
    SecretKeySpec key;

    public RC4(byte[] key) {
        this.key = new SecretKeySpec(key, "RC4");
    }

    public byte[] encrypt(byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance("RC4");
        cipher.init(Cipher.ENCRYPT_MODE, this.key);
        return cipher.doFinal(plaintext);
    }
}
