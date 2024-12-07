package me.seyfu_t.actions;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyAddAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] polyA = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] polyB = Util.convertJsonArrayToStringArray(arguments.get("B").getAsJsonArray());

        GFPoly a = new GFPoly(polyA);
        GFPoly b = new GFPoly(polyB);

        return ResponseBuilder.singleResponse("S", add(a, b).toBase64Array());
    }

    public static GFPoly add(GFPoly a, GFPoly b) {
        int minIndex = Math.min(a.size(), b.size());
        int maxIndex = Math.max(a.size(), b.size());

        GFPoly result = new GFPoly();

        for (int i = 0; i < minIndex; i++)
            result.setCoefficient(i, a.getCoefficient(i).xor(b.getCoefficient(i)));

        for (int i = minIndex; i < maxIndex; i++)
            result.setCoefficient(i, a.size() > b.size() ? a.getCoefficient(i) : b.getCoefficient(i));

        if (result.isZero())
            return GFPoly.ZERO_POLY;

        return result.popLeadingZeros();
    }

    public static GF128Poly add(GF128Poly a, GF128Poly b) {
        int minIndex = Math.min(a.size(), b.size());
        int maxIndex = Math.max(a.size(), b.size());

        GF128Poly result = new GF128Poly();

        for (int i = 0; i < minIndex; i++)
            result.setCoefficient(i, a.getCoefficient(i).xor(b.getCoefficient(i).copy()));

        for (int i = minIndex; i < maxIndex; i++)
            result.setCoefficient(i, a.size() > b.size() ? a.getCoefficient(i).copy() : b.getCoefficient(i).copy());

        if (result.isZero())
            result.setCoefficient(0, UBigInt16.Zero(true));

        result = result.popLeadingZeros();

        return result;
    }

    public static GF128Poly add(GF128Poly[] summands) {
        GF128Poly result = new GF128Poly();

        for (GF128Poly summand : summands) {
            if (result.isZero())
                result = summand;

            result = add(result, summand);
        }

        return result;
    }

}
