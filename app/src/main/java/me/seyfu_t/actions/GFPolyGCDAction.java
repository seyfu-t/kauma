package me.seyfu_t.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.util.Util;

public class GFPolyGCDAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] polyA = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] polyB = Util.convertJsonArrayToStringArray(arguments.get("B").getAsJsonArray());

        GF128Poly a = new GF128Poly(polyA);
        GF128Poly b = new GF128Poly(polyB);

        GF128Poly gcd = gfPolyGCD(a, b);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("G", gcd.toBase64Array());

        return resultMap;
    }

    public static GF128Poly gfPolyGCD(GF128Poly a, GF128Poly b) {
        // Handle special cases
        if (a.isZero())
            return b.copy();
        if (b.isZero())
            return a.copy();

        // Ensure a has degree >= b for first division
        GF128Poly dividend = a.degree() >= b.degree() ? a.copy() : b.copy();
        GF128Poly divisor = a.degree() >= b.degree() ? b.copy() : a.copy();

        while (!divisor.isZero()) {
            // Get remainder after using gfpoly_divmod
            Map<String, Object> divResult = GFPolyDivModAction.gfPolyDivMod(dividend, divisor);
            String[] remainderArray = (String[]) divResult.get("R");

            // Update polynomials for next iteration
            dividend = divisor.copy();
            divisor = new GF128Poly(remainderArray);
        }

        // Normalize the result - make the leading coefficient 1 if possible
        GF128Poly monicResult = GFPolyMakeMonicAction.gfPolyMakeMonic(dividend);

        return monicResult;
    }
}
