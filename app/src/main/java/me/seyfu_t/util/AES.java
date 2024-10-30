package me.seyfu_t.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class AES {

    @SuppressWarnings("UseSpecificCatch")
    public static String encrypt(String plainText, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public static String decrypt(String encryptedText, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            return "";
        }
    }

}
