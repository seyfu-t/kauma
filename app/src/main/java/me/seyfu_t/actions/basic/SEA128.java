package me.seyfu_t.actions.basic;

import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.AES;
import me.seyfu_t.util.ResponseBuilder;

public class SEA128 implements Action {

    // c0ffeec0ffeec0ffeec0ffeec0ffee11
    private static final byte[] XOR = new byte[] {
            (byte) 0xc0, (byte) 0xff, (byte) 0xee, (byte) 0xc0,
            (byte) 0xff, (byte) 0xee, (byte) 0xc0, (byte) 0xff,
            (byte) 0xee, (byte) 0xc0, (byte) 0xff, (byte) 0xee,
            (byte) 0xc0, (byte) 0xff, (byte) 0xee, (byte) 0x11,
    };

    @Override
    public JsonObject execute(JsonObject arguments) {
        String mode = arguments.get("mode").getAsString();
        byte[] key = Base64.getDecoder().decode(arguments.get("key").getAsString());
        byte[] input = Base64.getDecoder().decode(arguments.get("input").getAsString());

        return ResponseBuilder.singleResponse("output", Base64.getEncoder().encodeToString(sea128(mode, input, key)));
    }

    public static byte[] sea128(String mode, byte[] input, byte[] key) {

        byte[] output = switch (mode) {
            case "encrypt" -> encryptSEA128(input, key);
            case "decrypt" -> decryptSEA128(input, key);
            default -> throw new IllegalArgumentException("Null is not a valid mode");
        };

        return output;
    }

    public static byte[] encryptSEA128(byte[] msg, byte[] key) {
        // Throw into AES
        byte[] aes = AES.encrypt(msg, key);

        // XOR
        aes[0] ^= XOR[0];
        aes[1] ^= XOR[1];
        aes[2] ^= XOR[2];
        aes[3] ^= XOR[3];

        aes[4] ^= XOR[4];
        aes[5] ^= XOR[5];
        aes[6] ^= XOR[6];
        aes[7] ^= XOR[7];

        aes[8] ^= XOR[8];
        aes[9] ^= XOR[9];
        aes[10] ^= XOR[10];
        aes[11] ^= XOR[11];

        aes[12] ^= XOR[12];
        aes[13] ^= XOR[13];
        aes[14] ^= XOR[14];
        aes[15] ^= XOR[15];

        return aes;
    }

    public static byte[] decryptSEA128(byte[] msg, byte[] key) {
        // XOR
        msg[0] ^= XOR[0];
        msg[1] ^= XOR[1];
        msg[2] ^= XOR[2];
        msg[3] ^= XOR[3];

        msg[4] ^= XOR[4];
        msg[5] ^= XOR[5];
        msg[6] ^= XOR[6];
        msg[7] ^= XOR[7];

        msg[8] ^= XOR[8];
        msg[9] ^= XOR[9];
        msg[10] ^= XOR[10];
        msg[11] ^= XOR[11];

        msg[12] ^= XOR[12];
        msg[13] ^= XOR[13];
        msg[14] ^= XOR[14];
        msg[15] ^= XOR[15];

        // Then AES decrypt
        return AES.decrypt(msg, key);
    }

}
