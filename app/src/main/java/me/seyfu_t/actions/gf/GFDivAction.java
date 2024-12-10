package me.seyfu_t.actions.gf;

import java.math.BigInteger;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.BigLong;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.util.ResponseBuilder;

public class GFDivAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String base64A = arguments.get("a").getAsString();
        String base64B = arguments.get("b").getAsString();

        FieldElement a = FieldElement.fromBase64GCM(base64A);
        FieldElement b = FieldElement.fromBase64GCM(base64B);

        return ResponseBuilder.singleResponse("q", div(a, b).toBase64GCM());
    }

    public static FieldElement div(FieldElement a, FieldElement b) {
        return GFMulAction.mulAndReduce(a, inverseExtendedGCD(b.toBigInteger()));
    }

    public static FieldElement inverse(FieldElement a) {
        FieldElement result = FieldElement.One(); // 1
        FieldElement base = a;

        FieldElement pow = FieldElement.AllOne().unsetBit(0); // 2^128 - 2

        while (!pow.isZero()) {
            if (pow.testBit(0)) // if odd
                result = GFMulAction.mulAndReduce(result, base);

            base = GFMulAction.mulAndReduce(base, base);

            pow = pow.divBy2(); // div by 2
        }

        return result;
    }

    public static FieldElement inverseExtendedGCD(BigInteger a) {
        if (a.equals(BigInteger.ZERO))
            return null;

        BigInteger u = a;
        BigInteger v = BigInteger.ZERO.setBit(128).setBit(7).setBit(2).setBit(1).setBit(0);
        BigInteger g1 = BigInteger.ONE;
        BigInteger g2 = BigInteger.ZERO;

        while (!u.equals(BigInteger.ZERO)) {
            int degreeU = u.bitLength() - 1;
            int degreeV = v.bitLength() - 1;

            // Ensure u always has the higher or equal degree
            if (degreeU < degreeV) {
                // Swap u <-> v and g1 <-> g2
                BigInteger tempU = u;
                u = v;
                v = tempU;

                BigInteger tempG = g1;
                g1 = g2;
                g2 = tempG;

                // Recompute degrees after swap
                degreeU = u.bitLength() - 1;
                degreeV = v.bitLength() - 1;

            }

            int shift = degreeU - degreeV;
            u = u.xor(v.shiftLeft(shift));
            g1 = g1.xor(g2.shiftLeft(shift));

        }

        // At this point, v holds the gcd. If gcd == 1, g2 is the inverse of a mod p.
        if (v.equals(BigInteger.ONE))
            return new FieldElement(g2);
        else
            return null;

    }

    /*
     * These don't work because of 128 bit limitation and whatnot
     */

    public static FieldElement inverseExtendedGCD(FieldElement a) {
        FieldElement u = a;
        FieldElement v = FieldElement.Zero().setBit(7).setBit(2).setBit(1).setBit(0);
        FieldElement g1 = FieldElement.One();
        FieldElement g2 = FieldElement.Zero();

        boolean first = true;

        while (!u.equals(FieldElement.Zero())) {
            int degreeU = u.getHighestSetBitIndex();
            int degreeV = v.getHighestSetBitIndex();

            if (degreeU < degreeV) {
                FieldElement temp1 = u;
                u = v;
                v = temp1;

                FieldElement temp2 = g1;
                g1 = g2;
                g2 = temp2;

                degreeU = u.getHighestSetBitIndex();
                degreeV = first ? 128 : v.getHighestSetBitIndex();
            }

            int shift = degreeU - degreeV;
            u = u.xor(v.shiftLeft(shift));
            g1 = g1.xor(g2.shiftLeft(shift));

            System.err.println("U :" + u.toString(16));
            System.err.println("V :" + v.toString(16));
            System.err.println("G1 :" + g1.toString(16));
            System.err.println("G2 :" + g2.toString(16));
            System.err.println("SHIFT: " + shift);

            first = false;
        }

        if (v.equals(FieldElement.One())) {
            return g2; // g2 is the inverse
        } else {
            // No inverse exists for a
            return null; // or throw an appropriate exception
        }
    }

    public static FieldElement inverseExtendedGCD(BigLong a) {
        BigLong u = a.popLeadingZeros();
        BigLong v = new BigLong().setBit(128).setBit(7).setBit(2).setBit(1).setBit(0);
        BigLong g1 = BigLong.One();
        BigLong g2 = BigLong.Zero();

        while (!u.equals(BigLong.Zero())) {
            long degreeU = u.getMostSignificantBitIndex();
            long degreeV = v.getMostSignificantBitIndex();

            if (degreeU < degreeV) {
                BigLong temp1 = u;
                u = v;
                v = temp1;

                BigLong temp2 = g1;
                g1 = g2;
                g2 = temp2;

                degreeU = u.getMostSignificantBitIndex();
                degreeV = v.getMostSignificantBitIndex();
            }

            int shift = (int) (degreeU - degreeV);
            u = u.xor(v.shiftLeft(shift)).popLeadingZeros();
            g1 = g1.xor(g2.shiftLeft(shift)).popLeadingZeros();

            System.err.println("U :" + u.toString(16));
            System.err.println("V :" + v.toString(16));
            System.err.println("G1 :" + g1.toString(16));
            System.err.println("G2 :" + g2.toString(16));
            System.err.println("SHIFT: " + shift);
        }

        if (v.equals(BigLong.One())) {
            return new FieldElement(g2); // g2 is the inverse
        } else {
            // No inverse exists for a
            return null; // or throw an appropriate exception
        }
    }

}
