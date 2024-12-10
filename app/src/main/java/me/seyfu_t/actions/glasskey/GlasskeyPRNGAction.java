package me.seyfu_t.actions.glasskey;

import java.util.Arrays;
import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.SHA;
import me.seyfu_t.util.Util;

public class GlasskeyPRNGAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        byte[] key = Base64.getDecoder().decode(arguments.get("agency_key").getAsString());
        byte[] seed = Base64.getDecoder().decode(arguments.get("seed").getAsString());
        long[] lengths = Util.convertJsonArrayToLongArray(arguments.get("lengths").getAsJsonArray());

        return ResponseBuilder.singleResponse("blocks", prngAll(key, seed, lengths));
    }

    public static String[] prngAll(byte[] key, byte[] seed, long[] nums) {
        String[] result = new String[nums.length];

        for (int i = 0; i < nums.length; i++) {
            byte[] block = Arrays.copyOfRange(prngSingle(key, seed, nums[i]), 0, (int) nums[i]);
            result[i] = Base64.getEncoder().encodeToString(block);
        }

        return result;

    }

    public static byte[] prngSingle(byte[] key, byte[] seed, long num) {
        byte[] numStar = Util.longToBytesLittleEndian(num);

        byte[] keySha = Util.swapByteOrder(SHA.sha256(key));
        byte[] seedSha = Util.swapByteOrder(SHA.sha256(seed));

        byte[] keyStar = new byte[32];
        System.arraycopy(keySha, 0, keyStar, 0, 16);
        System.arraycopy(seedSha, 0, keyStar, 16, 16);

        byte[] hmac = Util.swapByteOrder(SHA.hmacSha256(numStar, keyStar));
        hmac = Util.swapByteOrder(hmac);
        System.out.println("KEY: " + toHex(keySha));
        System.out.println("SEED: " + toHex(seedSha));
        System.out.println("HMAC: " + toHex(hmac));

        return hmac;
    }

    public static String toHex(byte[] input) {
        if (input.length == 0)
            return "0";

        StringBuilder hex = new StringBuilder();
        for (int i = input.length - 1; i >= 0; i--)
            hex.append(String.format("%02x", input[i]));

        while (hex.charAt(0) == '0' && hex.length() > 1)
            hex.deleteCharAt(0);

        hex.deleteCharAt(hex.length() - 1); // remove final space

        return hex.toString().toUpperCase();
    }

}