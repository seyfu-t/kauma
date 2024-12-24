package me.seyfu_t.actions.glasskey;

import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.SHA;
import me.seyfu_t.util.Util;

public class GlasskeyPRNG implements Action {

    private static final int BLOCK_SIZE = 32;

    @Override
    public JsonObject execute(JsonObject arguments) {
        byte[] key = Base64.getDecoder().decode(arguments.get("agency_key").getAsString());
        byte[] seed = Base64.getDecoder().decode(arguments.get("seed").getAsString());
        int[] lengths = Util.convertJsonArrayToIntegerArray(arguments.get("lengths").getAsJsonArray());

        return ResponseBuilder.single("blocks", prngAll(key, seed, lengths));
    }

    public static String[] prngAll(byte[] key, byte[] seed, int[] nums) {
        String[] result = new String[nums.length];

        int currentBlock = 0;
        byte[] currentData = new byte[0];
        int currentPosition = 0;

        for (int i = 0; i < nums.length; i++) {
            byte[] resultBytes = new byte[nums[i]];
            int bytesNeeded = nums[i];
            int bytesCollected = 0;

            while (bytesCollected < bytesNeeded) {
                // If we've used all current data, generate new block
                if (currentPosition >= currentData.length) {
                    currentData = prngSingle(key, seed, currentBlock++);
                    currentPosition = 0;
                }

                // Calculate how many bytes we can take from current block
                int bytesToTake = Math.min(currentData.length - currentPosition, bytesNeeded - bytesCollected);

                // Copy bytes to result
                System.arraycopy(currentData, currentPosition, resultBytes, bytesCollected, bytesToTake);

                currentPosition += bytesToTake;
                bytesCollected += bytesToTake;
            }

            result[i] = Base64.getEncoder().encodeToString(resultBytes);
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