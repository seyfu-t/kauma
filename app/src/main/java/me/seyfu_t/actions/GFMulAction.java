package me.seyfu_t.actions;

import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class GFMulAction implements Action {

    @Override
    public Entry<String, Object> execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        String a = arguments.get("a").getAsString();
        String b = arguments.get("b").getAsString();

        String product = "";

        if (semantic.equalsIgnoreCase("xex")) {
            product = mulGF(a, b, false);
        } else if (semantic.equalsIgnoreCase("gcm")) {
            product = mulGF(a, b, true);
        }

        return new AbstractMap.SimpleEntry<>("product", product);
    }

    private static String mulGF(String base64A, String base64B, boolean gcm) {
        byte[] blockA = Base64.getDecoder().decode(base64A);
        byte[] blockB = Base64.getDecoder().decode(base64B);

        UBigInt16 bigIntA = new UBigInt16(blockA, gcm);
        UBigInt16 bigIntB = new UBigInt16(blockB, gcm);

        UBigInt16 product = Util.combinedMulAndModReduction(bigIntA, bigIntB);
        String base64 = Base64.getEncoder().encodeToString(product.toByteArray());

        return base64;
    }

}
