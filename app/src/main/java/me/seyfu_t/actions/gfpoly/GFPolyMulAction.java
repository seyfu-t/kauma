package me.seyfu_t.actions.gfpoly;

import com.google.gson.JsonObject;

import me.seyfu_t.actions.gf.GFMulAction;
import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyMulAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] polyA = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] polyB = Util.convertJsonArrayToStringArray(arguments.get("B").getAsJsonArray());

        GFPoly a = new GFPoly(polyA);
        GFPoly b = new GFPoly(polyB);

        return ResponseBuilder.singleResponse("P", mul(a, b).toBase64Array());
    }

    public static GFPoly mul(GFPoly a, GFPoly b) {
        // 0 times anything is 0
        if (a.isZero() || b.isZero())
            return GFPoly.ZERO_POLY;

        GFPoly result = new GFPoly();

        // Go through first polynomial
        for (int i = 0; i < a.size(); i++) {
            // 0 terms can be skipped
            FieldElement coefA = a.getCoefficient(i);
            if (coefA.isZero())
                continue;

            // Go through second polynomial
            for (int j = 0; j < b.size(); j++) {
                // Again skip 0 terms
                FieldElement coefB = b.getCoefficient(j);
                if (coefB.isZero())
                    continue;

                // Multiply coefficients in GF(2^128)
                FieldElement multipliedCoef = GFMulAction.mulAndReduce(coefA, coefB);

                // Create a polynomial with single term at degree i+j
                GFPoly term = new GFPoly();
                term.setCoefficient(i + j, multipliedCoef);

                // Add this term to result
                result = GFPolyAddAction.add(result, term);
            }
        }

        if (result.isZero())
            return GFPoly.ZERO_POLY;

        return result;
    }

    public static GFPoly square(GFPoly a) {
        if (a.isZero())
            return GFPoly.ZERO_POLY;

        GFPoly result = new GFPoly();

        for (int i = 0; i < a.size(); i++) {
            if (!a.getCoefficient(i).isZero()) {
                FieldElement squared = GFMulAction.mulAndReduce(a.getCoefficient(i), a.getCoefficient(i));
                result.setCoefficient(i * 2, squared);
            }
        }

        return result;
    }

}
