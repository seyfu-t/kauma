package me.seyfu_t.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class GFPolyPowModAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] poly = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] modPoly = Util.convertJsonArrayToStringArray(arguments.get("M").getAsJsonArray());
        UBigInt16 k = UBigInt16.fromBigInt(arguments.get("k").getAsBigInteger());

        GF128Poly a = new GF128Poly(poly);
        GF128Poly m = new GF128Poly(modPoly);

        GF128Poly z = gfPolyPowMod(a, k, m);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("Z", z.toBase64Array());

        return resultMap;
    }

    // Square and multiply algorithm
    public static GF128Poly gfPolyPowMod(GF128Poly poly, UBigInt16 pow, GF128Poly mod) {
        if (pow.isZero()) {
            GF128Poly one = new GF128Poly();
            one.setCoefficient(0, UBigInt16.Zero(true).setBit(0));
            return one;
        }

        // Check if power is 1
        if (pow.sameAs(UBigInt16.Zero(true).setBit(0))) {
            return poly.copy();
        }

        // Initialize result as 1 (identity element for multiplication)
        GF128Poly result = new GF128Poly();
        result.insertCoefficient(0, UBigInt16.Zero(true).setBit(0));

        GF128Poly base = poly.copy();

        UBigInt16 p = pow.copy();
        // Square and multiply
        while (!p.isZero()) {
            // If odd, multiply
            if (p.testBit(0)) {
                result = GFPolyMulAction.gfPolyMul(result, base);
                result = GFPolyDivModAction.gfPolyDivModRest(result, mod);
            }

            // Square
            base = GFPolyMulAction.gfPolyMul(base, base);
            // Reduce
            base = GFPolyDivModAction.gfPolyDivModRest(base, mod);

            // Divide power by 2
            p = p.shiftRight(1);
        }

        return result.popLeadingZeros();
    }
}
