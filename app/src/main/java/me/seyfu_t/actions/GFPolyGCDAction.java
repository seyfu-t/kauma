package me.seyfu_t.actions;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyGCDAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] polyA = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] polyB = Util.convertJsonArrayToStringArray(arguments.get("B").getAsJsonArray());

        GFPoly a = new GFPoly(polyA);
        GFPoly b = new GFPoly(polyB);

        return ResponseBuilder.singleResponse("G", gcd(a, b).toBase64Array());
    }

    public static GFPoly gcd(GFPoly a, GFPoly b) {
        // Handle special cases
        if (a.isZero())
            return b.copy();
        if (b.isZero())
            return a.copy();

        // Ensure a has degree >= b for first division
        GFPoly dividend = a.degree() >= b.degree() ? a.copy() : b.copy();
        GFPoly divisor = a.degree() >= b.degree() ? b.copy() : a.copy();

        while (!divisor.isZero()) {
            // Get remainder after using gfpoly_divmod
            // Update polynomials for next iteration
            GFPoly tempDividend = dividend.copy();
            dividend = divisor.copy();
            divisor = GFPolyDivModAction.divModRest(tempDividend, divisor);
        }

        // Normalize the result - make the leading coefficient 1 if possible
        GFPoly monicResult = GFPolyMakeMonicAction.makeMonic(dividend);

        return monicResult;
    }

}
