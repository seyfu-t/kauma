package me.seyfu_t.actions;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class GFPolyDivModAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] a = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] b = Util.convertJsonArrayToStringArray(arguments.get("B").getAsJsonArray());

        GF128Poly polyA = new GF128Poly(a);
        GF128Poly polyB = new GF128Poly(b);

        Map<String, Object> resultMap = gfPolyDivMod(polyA, polyB);

        return resultMap;
    }

    public static Map<String, Object> gfPolyDivMod(GF128Poly dividend, GF128Poly divisor) {
        if (divisor.isEmpty()) {
            throw new ArithmeticException("Division by zero polynomial");
        }

        // If dividend degree < divisor degree, quotient is 0 and remainder is dividend
        if (dividend.size() < divisor.size()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("Q", new GF128Poly().setCoefficient(0, UBigInt16.Zero()).toBase64Array());
            map.put("R", dividend.toBase64Array());
            return map;
        }

        // Initialize quotient and remainder
        GF128Poly quotient = new GF128Poly();
        GF128Poly remainder = dividend.copy();

        // leading coefficient
        UBigInt16 divisorLC = divisor.getCoefficient(divisor.degree());

        while (!remainder.isZero() && remainder.degree() >= divisor.degree()) {
            int degDiff = remainder.degree() - divisor.degree();

            // Calculate the quotient term
            UBigInt16 quotientTerm = GFDivAction.divide(remainder.getCoefficient(remainder.degree()), divisorLC);

            // Create a new polynomial for the term
            GF128Poly term = new GF128Poly();
            term.setCoefficient(degDiff, quotientTerm);

            quotient = GFPolyAddAction.gfPolyAdd(quotient, term);

            // Subtract (divisor * term) from remainder
            GF128Poly subtrahend = GFPolyMulAction.gfPolyMul(divisor, term);
            remainder = GFPolyAddAction.gfPolyAdd(remainder, subtrahend);

            remainder = remainder.popLeadingZeros();
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Q", quotient.toBase64Array());
        map.put("R", remainder.toBase64Array());
        return map;
    }

    public static GF128Poly gfPolyDivModRest(GF128Poly dividend, GF128Poly divisor) {
        return new GF128Poly((String[]) gfPolyDivMod(dividend, divisor).get("R"));
    }

    public static GF128Poly gfPolyDivModQuotient(GF128Poly dividend, GF128Poly divisor) {
        return new GF128Poly((String[]) gfPolyDivMod(dividend, divisor).get("Q"));
    }

}
