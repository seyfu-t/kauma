package me.seyfu_t.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class GFPolyMulAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] polyA = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] polyB = Util.convertJsonArrayToStringArray(arguments.get("B").getAsJsonArray());

        GF128Poly a = new GF128Poly(polyA);
        GF128Poly b = new GF128Poly(polyB);

        GF128Poly product = gfPolyMul(a, b);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("P", product.toBase64Array());

        return resultMap;
    }

    public static GF128Poly gfPolyMul(GF128Poly a, GF128Poly b) {
        GF128Poly result = new GF128Poly();

        // 0 times anything is 0
        if (a.isEmpty() || b.isEmpty()) {
            result.setCoefficient(0, UBigInt16.Zero(true));
            return result;
        }

        // Go through first polynomial
        for (int i = 0; i < a.size(); i++) {
            // 0 terms can be skipped
            UBigInt16 coefA = a.getCoefficient(i);
            if (coefA == null || coefA.isZero())
                continue;

            // Go through second polynomial
            for (int j = 0; j < b.size(); j++) {
                // Again skip 0 terms
                UBigInt16 coefB = b.getCoefficient(j);
                if (coefB == null || coefB.isZero()) {
                    continue;
                }

                // Multiply coefficients in GF(2^128)
                UBigInt16 multipliedCoef = GFMulAction.combinedMulAndModReduction(coefA, coefB);

                // Create a polynomial with single term at degree i+j
                GF128Poly term = new GF128Poly();
                term.setCoefficient(i + j, multipliedCoef);

                // Add this term to result
                result = GFPolyAddAction.gfPolyAdd(result, term);
            }
        }

        if (result.isEmpty())
            result.setCoefficient(0, UBigInt16.Zero(true));

        return result;
    }

}
