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
        long[] bitLengths = Util.convertJsonArrayToLongArray(arguments.get("bit_lengths").getAsJsonArray());

        return ResponseBuilder.single("ints", intBits(key, seed, bitLengths));
    }

    public static long[] intBits(byte[] key, byte[] seed, long[] lengths) {
        int size = lengths.length;
        long[] response = new long[size];

        for (int i = 0; i < size; i++)
            response[i] = intBits(key, seed, lengths[i]);

        return response;
    }

    public static long intBits(byte[] key, byte[] seed, long b) {
        long l = (b + 7) / 8; // seal

        String[] randomBytes = GlasskeyPRNG.prngAll(key, seed, new int[] { (int) l });
        byte[] s = Base64.getDecoder().decode(randomBytes[0]);

        long sStar = Util.bytesToLong(s);

        long mask = (1 << b) - 1;
        return (int) ((sStar & mask) & 0xFF);
    }

}
