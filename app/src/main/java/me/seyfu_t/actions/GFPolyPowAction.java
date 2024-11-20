package me.seyfu_t.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class GFPolyPowAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] poly = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        int k = arguments.get("k").getAsInt();

        GF128Poly a = new GF128Poly(poly);

        GF128Poly z = gfPolyPow(a, k);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("Z", z.toBase64Array());

        return resultMap;
    }

    // Square and multiply algorithm
    public static GF128Poly gfPolyPow(GF128Poly poly, int pow) {
        if (pow < 0) {
            throw new IllegalArgumentException("Negative powers are not supported");
        }

        if (pow == 0) {
            GF128Poly one = new GF128Poly();
            one.setCoefficient(0, UBigInt16.Zero(true).setBit(0));
            return one;
        }

        if (pow == 1) {
            return poly.copy();
        }

        // Initialize result as 1 (identity element for multiplication)
        GF128Poly result = new GF128Poly();
        result.insertCoefficient(0, UBigInt16.Zero(true).setBit(0));

        GF128Poly base = poly.copy();

        // Binary exponentiation algorithm
        while (pow > 0) {
            // If odd, multiply
            if ((pow & 1) == 1) {
                result = GFPolyMulAction.gfPolyMul(result, base);
            }

            // Square
            base = GFPolyMulAction.gfPolyMul(base, base);

            // Divide power by 2
            pow >>= 1;
        }

        return result.popLeadingZeros();
    }

}
