package me.seyfu_t.actions;

import java.math.BigInteger;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.model.UBigInt512;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyPowModAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] poly = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] modPoly = Util.convertJsonArrayToStringArray(arguments.get("M").getAsJsonArray());
        UBigInt16 k = UBigInt16.fromBigInt(arguments.get("k").getAsBigInteger());
        // BigInteger k = arguments.get("k").getAsBigInteger();

        GF128Poly a = new GF128Poly(poly);
        GF128Poly m = new GF128Poly(modPoly);

        return ResponseBuilder.singleResponse("Z", powMod(a, k, m).toBase64Array());
    }

    public static GF128Poly powMod(GF128Poly poly, BigInteger pow, GF128Poly mod) {
        if (pow.equals(BigInteger.ZERO))
            return GF128Poly.DEGREE_ZERO_POLY_ONE;

        // Check if power is 1
        if (pow.equals(BigInteger.ONE))
            return poly.copy();

        // Initialize result as 1 (identity element for multiplication)
        GF128Poly result = GF128Poly.DEGREE_ZERO_POLY_ONE;

        GF128Poly base = poly.copy();

        UBigInt16 p = UBigInt16.fromBigInt(pow, false);
        // Square and multiply
        while (!p.isZero()) {
            // If odd, multiply
            if (p.testBit(0)) {
                result = GFPolyMulAction.mul(result, base);
                result = GFPolyDivModAction.divModRest(result, mod);
            }

            // Square
            base = GFPolyMulAction.square(base);
            // Reduce
            base = GFPolyDivModAction.divModRest(base, mod);

            // Divide power by 2
            p = p.shiftRight(1);
        }

        return result.popLeadingZeros();
    }



    // Square and multiply algorithm
    public static GF128Poly powMod(GF128Poly poly, UBigInt16 pow, GF128Poly mod) {
        if (pow.isZero())
            return GF128Poly.DEGREE_ZERO_POLY_ONE;

        UBigInt16 one = UBigInt16.One();
        boolean condition = pow.sameAs(one); 
        // Check if power is 1
        if (condition){
            System.err.println("CONDITION TRUE AT:");
            System.err.println("POW: "+pow);
            System.err.println("ONE: "+one);
            System.err.println("CONDITION: "+condition);
            return poly.copy();
        }

        // Initialize result as 1 (identity element for multiplication)
        GF128Poly result = GF128Poly.DEGREE_ZERO_POLY_ONE;

        GF128Poly base = poly.copy();

        UBigInt16 p = pow.copy();
        // Square and multiply
        while (!p.isZero()) {
            // If odd, multiply
            if (p.testBit(0)) {
                result = GFPolyMulAction.mul(result, base);
                result = GFPolyDivModAction.divModRest(result, mod);
            }

            // Square
            base = GFPolyMulAction.square(base);
            // Reduce
            base = GFPolyDivModAction.divModRest(base, mod);

            // Divide power by 2
            p = p.shiftRight(1);
        }

        return result.popLeadingZeros();
    }

    public static GF128Poly powMod(GF128Poly poly, UBigInt512 pow, GF128Poly mod) {
        if (pow.isZero())
            return GF128Poly.DEGREE_ZERO_POLY_ONE;

        // Check if power is 1
        if (pow.sameAs(UBigInt512.One(true)))
            return poly.copy();

        // Initialize result as 1 (identity element for multiplication)
        GF128Poly result = GF128Poly.DEGREE_ZERO_POLY_ONE;

        GF128Poly base = poly.copy();

        UBigInt512 p = pow.copy();
        // Square and multiply
        while (!p.isZero()) {
            // If odd, multiply
            if (p.testBit(0)) {
                result = GFPolyMulAction.mul(result, base);
                result = GFPolyDivModAction.divModRest(result, mod);
            }

            // Square
            base = GFPolyMulAction.square(base);
            // Reduce
            base = GFPolyDivModAction.divModRest(base, mod);

            // Divide power by 2
            p = p.shiftRight(1);
        }

        return result.popLeadingZeros();
    }
}
