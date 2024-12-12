package me.seyfu_t.actions.glasskey;

import java.util.Arrays;
import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.SHA;
import me.seyfu_t.util.Util;

public class GlasskeyPRNGAction implements Action {

    private static final int BLOCK_SIZE = 32;

    @Override
    public JsonObject execute(JsonObject arguments) {
        byte[] key = Base64.getDecoder().decode(arguments.get("agency_key").getAsString());
        byte[] seed = Base64.getDecoder().decode(arguments.get("seed").getAsString());
        int[] lengths = Util.convertJsonArrayToIntegerArray(arguments.get("lengths").getAsJsonArray());

        return ResponseBuilder.singleResponse("blocks", prngAll(key, seed, lengths));
    }

    public static String[] prngAll(byte[] key, byte[] seed, int[] nums) {
        String[] result = new String[nums.length];

        byte[] buffer = new byte[nums.length * BLOCK_SIZE];

        for (int i = 0; i < nums.length; i++) {
            byte[] block = prngSingle(key, seed, i);
            System.arraycopy(block, 0, buffer, i * BLOCK_SIZE, block.length);
        }

        for (int i = 0; i < nums.length; i++) {
            result[i] = Base64.getEncoder().encodeToString(Arrays.copyOfRange(buffer, 0, nums[i]));
            buffer = Arrays.copyOfRange(buffer, nums[i], buffer.length);
        }

        return result;

    }

    public static byte[] prngSingle(byte[] key, byte[] seed, int num) {
        byte[] numStar = Util.intToBytesLittleEndian(num);

        byte[] keySha = SHA.sha256(key);
        byte[] seedSha = SHA.sha256(seed);

        byte[] keyStar = new byte[64];
        System.arraycopy(keySha, 0, keyStar, 0, BLOCK_SIZE);
        System.arraycopy(seedSha, 0, keyStar, BLOCK_SIZE, BLOCK_SIZE);

        return SHA.hmacSha256(numStar, keyStar);
    }

}