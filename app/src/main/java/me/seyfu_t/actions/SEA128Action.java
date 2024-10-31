package me.seyfu_t.actions;

import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.AES;

public class SEA128Action implements Action {

    // c0ffeec0ffeec0ffeec0ffeec0ffee11
    private static final UBigInt16 XOR = new UBigInt16(new byte[] {
            (byte) 0xc0, (byte) 0xff, (byte) 0xee, (byte) 0xc0,
            (byte) 0xff, (byte) 0xee, (byte) 0xc0, (byte) 0xff,
            (byte) 0xee, (byte) 0xc0, (byte) 0xff, (byte) 0xee,
            (byte) 0xc0, (byte) 0xff, (byte) 0xee, (byte) 0x11,
    });

    @Override
    public Entry<String, Object> execute(JsonObject arguments) {
        String mode = arguments.get("mode").getAsString();
        String key = arguments.get("key").getAsString();
        String input = arguments.get("input").getAsString();

        String output = sea128(mode, input, key);

        return new AbstractMap.SimpleEntry<>("output", output);
    }

    public static String sea128(String mode, String base64Input, String base64Key) {
        byte[] input = Base64.getDecoder().decode(base64Input);
        byte[] key = Base64.getDecoder().decode(base64Key);

        byte[] output = null;

        if (mode.equalsIgnoreCase("encrypt")) {
            output = encryptSEA128(input, key);
        } else if (mode.equalsIgnoreCase("decrypt")) {
            output = decryptSEA128(input, key);
        }

        String base64 = Base64.getEncoder().encodeToString(output);
        return base64;
    }

    private static byte[] encryptSEA128(byte[] msg, byte[] key) {
        // Throw into AES
        byte[] aes = AES.encrypt(msg, key);

        if (aes == null) {
            throw new RuntimeException("AES encryption failed");
        }
        
        // Then XOR with constant and return
        return new UBigInt16(aes).xor(XOR).toByteArray();
    }

    private static byte[] decryptSEA128(byte[] msg, byte[] key) {
        // First XOR with constant
        byte[] xored = new UBigInt16(msg).xor(XOR).toByteArray();
        // Then AES decrypt
        byte[] decrypted = AES.decrypt(xored, key);

        if (decrypted == null) {
            throw new RuntimeException("AES decryption failed");
        }
        
        return decrypted;
    }

}
