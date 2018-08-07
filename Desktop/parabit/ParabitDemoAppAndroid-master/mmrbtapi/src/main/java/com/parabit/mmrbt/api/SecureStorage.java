package com.parabit.mmrbt.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by williamsnyder on 2/20/18.
 */

public class SecureStorage {
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final String PARABIT_KEY_ALIAS = "PARABIT_SDK";
    private static final String FIXED_IV = "PARABITSDKIV";
    private static final String PARABIT_SDK_PREFS = "PARABIT_SDK_PREFS";

    private Context context;
    private KeyStore keyStore;

    private static SecureStorage instance;

    private SecureStorage(Context context) throws Exception {
        this.context = context;
        generateKey();
    }

    public static SecureStorage getInstance(Context context) throws Exception {
        if (instance == null) {
            instance = new SecureStorage(context);
        }

        return instance;
    }

    public void putValue(String key, String value) throws Exception {
        SharedPreferences preferences = context.getSharedPreferences(PARABIT_SDK_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, encrypt(value.getBytes()));
        editor.apply();
    }

    public String getValue(String key) throws Exception{
        SharedPreferences preferences = context.getSharedPreferences(PARABIT_SDK_PREFS, MODE_PRIVATE);
        String encrypted = preferences.getString(key, null);
        return new String(decrypt(encrypted));
    }

    private void generateKey() throws Exception {
        keyStore = KeyStore.getInstance(AndroidKeyStore);
        keyStore.load(null);

        if (!keyStore.containsAlias(PARABIT_KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(PARABIT_KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setRandomizedEncryptionRequired(false)
                            .build());
            keyGenerator.generateKey();
        }
    }

    private java.security.Key getSecretKey() throws Exception {
        return keyStore.getKey(PARABIT_KEY_ALIAS, null);
    }

    private String encrypt(byte[] input) throws Exception{
        Cipher c = Cipher.getInstance(AES_MODE);
        c.init(Cipher.ENCRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, FIXED_IV.getBytes()));
        byte[] encodedBytes = c.doFinal(input);
        String encryptedBase64Encoded = Base64.encodeToString(encodedBytes, Base64.DEFAULT);
        return encryptedBase64Encoded;
    }

    private byte[] decrypt(String encrypted) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE);
        c.init(Cipher.DECRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, FIXED_IV.getBytes()));
        byte[] encryptedBase64Decoded = Base64.decode(encrypted, Base64.DEFAULT);
        byte[] decodedBytes = c.doFinal(encryptedBase64Decoded);
        return decodedBytes;
    }

    public static void main(String[] args) {

    }
}
