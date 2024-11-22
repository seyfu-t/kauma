package me.seyfu_t.actions;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;

public class GFDivAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String a = arguments.get("a").getAsString();
        String b = arguments.get("b").getAsString();

        String quotient = div(a, b);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("q", quotient);

        return resultMap;
    }

    private static String div(String base64A, String base64B) {
        byte[] blockA = Base64.getDecoder().decode(base64A);
        byte[] blockB = Base64.getDecoder().decode(base64B);

        UBigInt16 bigIntA = new UBigInt16(blockA, true);
        UBigInt16 bigIntB = new UBigInt16(blockB, true);

        UBigInt16 quotient = div(bigIntA, bigIntB);
        String base64 = Base64.getEncoder().encodeToString(quotient.toByteArray());

        return base64;
    }

    public static UBigInt16 div(UBigInt16 a, UBigInt16 b) {
        return GFMulAction.combinedMulAndModReduction(a, inverse(b));
    }

    public static UBigInt16 inverse(UBigInt16 a) {
        UBigInt16 result = UBigInt16.Zero(true).setBit(0); // 1
        UBigInt16 base = a.copy();

        UBigInt16 pow = UBigInt16.AllOne().unsetBit(0); // 2^128 - 2

        while (!pow.isZero()) {
            if (pow.testBit(0)) { // if odd
                result = GFMulAction.combinedMulAndModReduction(result, base);
            }
            base = GFMulAction.combinedMulAndModReduction(base, base);

            pow = pow.shiftRight(1); // div by 2
        }

        return result;
    }

}
