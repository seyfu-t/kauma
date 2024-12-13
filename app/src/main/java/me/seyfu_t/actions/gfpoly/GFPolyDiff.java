package me.seyfu_t.actions.gfpoly;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyDiff implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] poly = Util.convertJsonArrayToStringArray(arguments.get("F").getAsJsonArray());

        GFPoly q = new GFPoly(poly);

        return ResponseBuilder.single("F'", diff(q).toBase64Array());
    }

    public static GFPoly diff(GFPoly poly) {
        if (poly.isZero())
            return GFPoly.ZERO_POLY;

        GFPoly result = new GFPoly();

        // Skip first one, effectively reduce all exponents by 1
        for (int i = 1; i < poly.size(); i++)
            if (i % 2 == 1)
                result.setCoefficient(i - 1, poly.getCoefficient(i));

        // result = result.popLeadingZeros();

        if (result.isZero())
            return GFPoly.ZERO_POLY;

        return result;
    }

}
