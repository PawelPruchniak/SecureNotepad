package com.example.notatnik.screens.security.biometric;

import java.io.Serializable;

import javax.crypto.Cipher;

public class CipherSerializable implements Serializable {
    private Cipher cipher;

    public CipherSerializable(Cipher aCipher){
        this.cipher = aCipher;
    }

    public Cipher getCipher() {
        return this.cipher;
    }
}
