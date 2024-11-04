package me.seyfu_t.actions;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
    public Map<String, Object> execute(JsonObject arguments) {
        String mode = arguments.get("mode").getAsString();
        String key = arguments.get("key").getAsString();
        String input = arguments.get("input").getAsString();

        String output = sea128(mode, input, key);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("output", output);

        return resultMap;
    }

    public static String sea128(String mode, String base64Input, String base64Key) {
        byte[] input = Base64.getDecoder().decode(base64Input);
        byte[] key = Base64.getDecoder().decode(base64Key);

        byte[] output = switch (mode) {
            case "encrypt" -> encryptSEA128(input, key);
            case "decrypt" -> decryptSEA128(input, key);
            default -> throw new IllegalArgumentException("Null is not a valid mode");
        };

        String base64 = Base64.getEncoder().encodeToString(output);
        return base64;
    }

    public static byte[] encryptSEA128(byte[] msg, byte[] key) {
        // Throw into AES
        byte[] aes = AES.encrypt(msg, key);

        if (aes == null) {
            throw new RuntimeException("AES encryption failed");
        }

        // Then XOR with constant and return
        return new UBigInt16(aes).xor(XOR).toByteArray();
    }

    public static byte[] decryptSEA128(byte[] msg, byte[] key) {
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
