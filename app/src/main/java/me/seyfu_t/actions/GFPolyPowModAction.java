package me.seyfu_t.actions;

import java.math.BigInteger;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.BigLong;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyPowModAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] poly = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] modPoly = Util.convertJsonArrayToStringArray(arguments.get("M").getAsJsonArray());
        // FieldElement k = FieldElement.fromBigInt(arguments.get("k").getAsBigInteger());
        BigInteger k = arguments.get("k").getAsBigInteger();

        GFPoly a = new GFPoly(poly);
        GFPoly m = new GFPoly(modPoly);

        return ResponseBuilder.singleResponse("Z", powMod(a, k, m).toBase64Array());
    }

    // Square and multiply algorithm
    // TODO: implement conversion from BigInteger to FieldElement
    public static GFPoly powMod(GFPoly base, FieldElement pow, GFPoly mod) {
        if (pow.isZero())
            return GFPoly.DEGREE_ZERO_POLY_ONE;

        // Check if power is 1
        if (pow.equals(FieldElement.One()))
            return GFPolyDivModAction.divModRest(base, mod);

        // Initialize result as 1 (identity element for multiplication)
        GFPoly result = GFPoly.DEGREE_ZERO_POLY_ONE;

        // Square and multiply
        while (!pow.isZero()) {
            // If odd, multiply
            if (pow.testBit(0)) {
                result = GFPolyMulAction.mul(result, base);
                result = GFPolyDivModAction.divModRest(result, mod);
            }

            // Square
            base = GFPolyMulAction.square(base);
            // Reduce
            base = GFPolyDivModAction.divModRest(base, mod);

            // Divide power by 2
            pow = pow.divBy2();
        }

        return result;
    }

    public static GFPoly powMod(GFPoly base, BigInteger pow, GFPoly mod) {
        if (pow.equals(BigInteger.ZERO))
            return GFPoly.DEGREE_ZERO_POLY_ONE;

        // Check if power is 1
        if (pow.equals(BigInteger.ONE))
            return GFPolyDivModAction.divModRest(base, mod);

        // Initialize result as 1 (identity element for multiplication)
        GFPoly result = GFPoly.DEGREE_ZERO_POLY_ONE;

        // Square and multiply
        while (!pow.equals(BigInteger.ZERO)) {
            // If odd, multiply
            if (pow.testBit(0)) {
                result = GFPolyMulAction.mul(result, base);
                result = GFPolyDivModAction.divModRest(result, mod);
            }

            // Square
            base = GFPolyMulAction.square(base);
            // Reduce
            base = GFPolyDivModAction.divModRest(base, mod);

            // Divide power by 2
            pow = pow.shiftRight(1);
        }

        return result.popLeadingZeros();
    }

    public static GFPoly powMod(GFPoly base, BigLong pow, GFPoly mod) {
        if (pow.equals(BigLong.Zero()))
            return GFPoly.DEGREE_ZERO_POLY_ONE;

        // Check if power is 1
        if (pow.equals(BigLong.One()))
            return GFPolyDivModAction.divModRest(base, mod);

        // Initialize result as 1 (identity element for multiplication)
        GFPoly result = GFPoly.DEGREE_ZERO_POLY_ONE;

        // Square and multiply
        while (!pow.equals(BigLong.Zero())) {
            // If odd, multiply
            if (pow.testBit(0)) {
                result = GFPolyMulAction.mul(result, base);
                result = GFPolyDivModAction.divModRest(result, mod);
            }

            // Square
            base = GFPolyMulAction.square(base);
            // Reduce
            base = GFPolyDivModAction.divModRest(base, mod);

            // Divide power by 2
            pow = pow.shiftRight(1);
        }

        return result.popLeadingZeros();
    }

   
}
