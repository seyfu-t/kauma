package me.seyfu_t.actions;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyPowAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] poly = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        long k = arguments.get("k").getAsLong();

        GFPoly a = new GFPoly(poly);

        return ResponseBuilder.singleResponse("Z", pow(a, k).toBase64Array());
    }

    // Square and multiply algorithm
    public static GFPoly pow(GFPoly poly, long exp) {
        if (exp == 0)
            return GFPoly.DEGREE_ZERO_POLY_ONE;

        if (exp == 1)
            return poly;

        // Initialize result as 1 (identity element for multiplication)
        GFPoly result = GFPoly.DEGREE_ZERO_POLY_ONE;

        GFPoly base = poly.copy();

        // Binary exponentiation algorithm
        while (exp > 0) {
            // If odd, multiply
            if ((exp & 1) == 1)
                result = GFPolyMulAction.mul(result, base);

            // Square
            base = GFPolyMulAction.square(base);

            // Divide exponent by 2
            exp >>= 1;
        }

        return result.popLeadingZeros();
    }

    // Square and multiply algorithm
    public static GFPoly pow(GFPoly poly, FieldElement exp) {
        if (exp.isZero())
            return GFPoly.DEGREE_ZERO_POLY_ONE;

        if (exp.equals(FieldElement.One()))
            return poly;

        // Initialize result as 1 (identity element for multiplication)
        GFPoly result = GFPoly.DEGREE_ZERO_POLY_ONE;

        GFPoly base = poly.copy();

        // Binary exponentiation algorithm
        while (!exp.isZero()) {
            // If odd, multiply
            if (exp.testBit(0))
                result = GFPolyMulAction.mul(result, base);

            // Square
            base = GFPolyMulAction.square(base);

            // Divide exponent by 2
            exp = exp.divBy2();
        }

        return result;
    }

    // Square and multiply algorithm
    public static GF128Poly pow(GF128Poly poly, int exp) {
        if (exp == 0) {
            GF128Poly one = new GF128Poly();
            one.setCoefficient(0, UBigInt16.Zero(true).setBit(0));
            return one;
        }

        if (exp == 1)
            return poly.copy();

        // Initialize result as 1 (identity element for multiplication)
        GF128Poly result = GF128Poly.DEGREE_ZERO_POLY_ONE;

        GF128Poly base = poly.copy();

        // Binary exponentiation algorithm
        while (exp > 0) {
            // If odd, multiply
            if ((exp & 1) == 1)
                result = GFPolyMulAction.mul(result, base);

            // Square
            base = GFPolyMulAction.square(base);

            // Divide exponent by 2
            exp >>= 1;
        }

        return result.popLeadingZeros();
    }

    public static GF128Poly pow(GF128Poly poly, UBigInt16 exp) {
        if (exp.isZero()) {
            GF128Poly one = new GF128Poly();
            one.setCoefficient(0, UBigInt16.Zero(true).setBit(0));
            return one;
        }

        if (exp.sameAs(UBigInt16.Zero(true).setBit(0)))
            return poly.copy();

        // Initialize result as 1 (identity element for multiplication)
        GF128Poly result = GF128Poly.DEGREE_ZERO_POLY_ONE;

        GF128Poly base = poly.copy();

        // Binary exponentiation algorithm
        while (!exp.isZero()) {
            // If odd, multiply
            if (exp.testBit(0))
                result = GFPolyMulAction.mul(result, base);

            // Square
            base = GFPolyMulAction.square(base);

            // Divide exponent by 2
            exp = exp.shiftRight(1);
        }

        return result.popLeadingZeros();
    }

}
