package me.seyfu_t.actions;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;

public class GFMulAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        String a = arguments.get("a").getAsString();
        String b = arguments.get("b").getAsString();

        String product = switch (semantic) {
            case "xex" -> mul(a, b, false);
            case "gcm" -> mul(a, b, true);
            default -> throw new IllegalArgumentException(semantic + " is not a valid semantic");
        };

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("product", product);

        return resultMap;
    }

    private static String mul(String base64A, String base64B, boolean gcm) {
        byte[] blockA = Base64.getDecoder().decode(base64A);
        byte[] blockB = Base64.getDecoder().decode(base64B);

        UBigInt16 bigIntA = new UBigInt16(blockA, gcm);
        UBigInt16 bigIntB = new UBigInt16(blockB, gcm);

        UBigInt16 product = combinedMulAndModReduction(bigIntA, bigIntB);
        String base64 = Base64.getEncoder().encodeToString(product.toByteArray());

        return base64;
    }

    public static UBigInt16 combinedMulAndModReduction(UBigInt16 a, UBigInt16 b) {
        // Early zero checks
        if (a.isZero() || b.isZero()) {
            return UBigInt16.Zero(a.isGCM());
        }

        UBigInt16 result = new UBigInt16(a.isGCM());

        // doing more than 128 rounds isn't possible
        for (int i = 0; i < UBigInt16.BYTE_COUNT * Byte.SIZE; i++) {
            if (b.testBit(0))
                result = result.xor(a);

            boolean overflow = a.testBit(127);
            a = a.shiftLeft(1);

            if (overflow)
                a = a.xor(UBigInt16.REDUCTION_POLY);

            b = b.shiftRight(1);

            // Early termination if b becomes zero
            if (b.isZero())
                break;
        }

        return result;
    }

}
