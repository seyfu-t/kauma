package me.seyfu_t.actions;

import java.util.Arrays;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyDivModAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] a = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] b = Util.convertJsonArrayToStringArray(arguments.get("B").getAsJsonArray());

        GFPoly polyA = new GFPoly(a);
        GFPoly polyB = new GFPoly(b);

        return divMod(polyA, polyB);
    }

    public static JsonObject divMod(GFPoly dividend, GFPoly divisor) {
        Tuple<GFPoly, GFPoly> result = divModTuple(dividend, divisor);

        return ResponseBuilder.multiResponse(Arrays.asList(
                new Tuple<>("Q", result.getFirst().toBase64Array()),
                new Tuple<>("R", result.getSecond().toBase64Array())));
    }

    private static Tuple<GFPoly, GFPoly> divModTuple(GFPoly dividend, GFPoly divisor) {
        // If dividend degree < divisor degree, quotient is 0 and remainder is dividend
        if (dividend.size() < divisor.size())
            return new Tuple<GFPoly, GFPoly>(GFPoly.ZERO_POLY, dividend);

        // Initialize quotient and remainder
        GFPoly quotient = new GFPoly();
        GFPoly remainder = dividend.copy();

        // leading coefficient
        FieldElement divisorLC = divisor.getCoefficient(divisor.degree());

        while (!remainder.isZero() && remainder.degree() >= divisor.degree()) {
            int degDiff = remainder.degree() - divisor.degree();

            // Calculate the quotient term
            FieldElement quotientTerm = GFDivAction.div(remainder.getCoefficient(remainder.degree()), divisorLC);

            // Create a new polynomial for the term
            GFPoly term = new GFPoly();
            term.setCoefficient(degDiff, quotientTerm);

            quotient = GFPolyAddAction.add(quotient, term);

            // Subtract (divisor * term) from remainder
            GFPoly subtrahend = GFPolyMulAction.mul(divisor, term);
            remainder = GFPolyAddAction.add(remainder, subtrahend);
        }

        return new Tuple<GFPoly, GFPoly>(quotient, remainder);
    }

    public static GFPoly divModRest(GFPoly dividend, GFPoly divisor) {
        return divModTuple(dividend, divisor).getSecond();
    }

    public static GFPoly divModQuotient(GFPoly dividend, GFPoly divisor) {
        return divModTuple(dividend, divisor).getFirst();
    }

    public static JsonObject divMod(GF128Poly dividend, GF128Poly divisor) {
        Tuple<GF128Poly, GF128Poly> result = divModTuple(dividend, divisor);

        return ResponseBuilder.multiResponse(Arrays.asList(
                new Tuple<>("Q", result.getFirst().toBase64Array()),
                new Tuple<>("R", result.getSecond().toBase64Array())));
    }

    private static Tuple<GF128Poly, GF128Poly> divModTuple(GF128Poly dividend, GF128Poly divisor) {
        // If dividend degree < divisor degree, quotient is 0 and remainder is dividend
        if (dividend.size() < divisor.size())
            return new Tuple<GF128Poly, GF128Poly>(new GF128Poly().setCoefficient(0, UBigInt16.Zero()), dividend);

        // Initialize quotient and remainder
        GF128Poly quotient = new GF128Poly();
        GF128Poly remainder = dividend.copy();

        // leading coefficient
        UBigInt16 divisorLC = divisor.getCoefficient(divisor.degree());

        while (!remainder.isZero() && remainder.degree() >= divisor.degree()) {
            int degDiff = remainder.degree() - divisor.degree();

            // Calculate the quotient term
            UBigInt16 quotientTerm = GFDivAction.div(remainder.getCoefficient(remainder.degree()), divisorLC);

            // Create a new polynomial for the term
            GF128Poly term = new GF128Poly();
            term.setCoefficient(degDiff, quotientTerm);

            quotient = GFPolyAddAction.add(quotient, term);

            // Subtract (divisor * term) from remainder
            GF128Poly subtrahend = GFPolyMulAction.mul(divisor, term);
            remainder = GFPolyAddAction.add(remainder, subtrahend);
        }

        return new Tuple<GF128Poly, GF128Poly>(quotient, remainder);
    }

    public static GF128Poly divModRest(GF128Poly dividend, GF128Poly divisor) {
        return divModTuple(dividend, divisor).getSecond();
    }

    public static GF128Poly divModQuotient(GF128Poly dividend, GF128Poly divisor) {
        return divModTuple(dividend, divisor).getFirst();
    }

}
