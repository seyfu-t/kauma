package me.seyfu_t.actions.glasskey;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.Tuple;

public class GlasskeyGenkey implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        
        return null;
    }

    public static Tuple<Long, Long> genkey(byte[] key, byte[] seed, long bitLength) {
        long pLength = bitLength / 2;
        // long p = GlasskeyPrngIntBitsAction.intBits(key, seed, pLength);
        // p |= 1L;
        return null;

    }

}
