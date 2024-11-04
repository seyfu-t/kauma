package me.seyfu_t.actions;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class GFMulAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        String a = arguments.get("a").getAsString();
        String b = arguments.get("b").getAsString();

        String product = switch (semantic) {
            case "xex" -> mulGF(a, b, false);
            case "gcm" -> mulGF(a, b, true);
            default -> throw new IllegalArgumentException(semantic + " is not a valid semantic");
        };

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("product", product);

        return resultMap;
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
