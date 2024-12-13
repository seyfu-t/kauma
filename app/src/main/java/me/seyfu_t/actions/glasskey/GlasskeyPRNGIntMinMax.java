package me.seyfu_t.actions.glasskey;

import java.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;

public class GlasskeyPRNGIntMinMax implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        byte[] key = Base64.getDecoder().decode(arguments.get("agency_key").getAsString());
        byte[] seed = Base64.getDecoder().decode(arguments.get("seed").getAsString());
        JsonArray array = arguments.get("specification").getAsJsonArray();

        return ResponseBuilder.single("ints", intMinMax(key, seed, array));
    }

    public static long[] intMinMax(byte[] key, byte[] seed, JsonArray specification) {
        int size = specification.size();
        long[] result = new long[size];

        for (int i = 0; i < size; i++) {
            JsonObject pair = specification.get(i).getAsJsonObject();
            long min = pair.get("min").getAsLong();
            long max = pair.get("max").getAsLong();
            result[i] = intMinMax(key, seed, min, max);
        }

        return result;
    }

    public static long intMinMax(byte[] key, byte[] seed, long min, long max) {
        long s = max - min + 1;
        long b = Long.bitCount(s);

        while (true) {
            // long r = GlasskeyPRNGIntBitsAction.intBits(key, seed, b);

            // if (r < max)
            //     return r;
        }
    }

}
