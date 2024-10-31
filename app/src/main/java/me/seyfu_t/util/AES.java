package me.seyfu_t.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    @SuppressWarnings("UseSpecificCatch")
    public static byte[] encrypt(byte[] plainText, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(plainText);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public static byte[] decrypt(byte[] encryptedText, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(encryptedText);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }

}
