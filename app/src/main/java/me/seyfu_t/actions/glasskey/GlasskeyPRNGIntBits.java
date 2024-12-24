package me.seyfu_t.actions.glasskey;

import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GlasskeyPRNGIntBits implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        byte[] key = Base64.getDecoder().decode(arguments.get("agency_key").getAsString());
        byte[] seed = Base64.getDecoder().decode(arguments.get("seed").getAsString());
        int[] bitLengths = Util.convertJsonArrayToIntegerArray(arguments.get("bit_lengths").getAsJsonArray());

        return ResponseBuilder.single("ints", intBits(key, seed, bitLengths));
    }

    public static int[] intBits(byte[] key, byte[] seed, int[] lengths) {
        int size = lengths.length;
        int[] response = new int[size];
        
        // Convert lengths to bytes needed
        int[] byteRequests = new int[size];
        for (int i = 0; i < size; i++)
            byteRequests[i] = (lengths[i] + 7) / 8;  // ceil(bits/8)
        
        // Get all required bytes using PRNG (stream)
        String[] randomBytes = GlasskeyPRNG.prngAll(key, seed, byteRequests);
        
        // Process each request
        for (int i = 0; i < size; i++) {
            byte[] bytes = Base64.getDecoder().decode(randomBytes[i]);
            int value = Util.bytesToInteger(bytes);
            // Create mask for required bits: (1 << bits) - 1
            int mask = (1 << lengths[i]) - 1;
            response[i] = value & mask;
        }

        return response;
    }
}
