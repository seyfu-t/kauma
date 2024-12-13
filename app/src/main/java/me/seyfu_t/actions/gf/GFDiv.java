package me.seyfu_t.actions.gf;

import java.math.BigInteger;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.util.ResponseBuilder;

public class GFDiv implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String base64A = arguments.get("a").getAsString();
        String base64B = arguments.get("b").getAsString();

        FieldElement a = FieldElement.fromBase64GCM(base64A);
        FieldElement b = FieldElement.fromBase64GCM(base64B);

        return ResponseBuilder.single("q", div(a, b).toBase64GCM());
    }

    public static FieldElement div(FieldElement a, FieldElement b) {
        return GFMul.mulAndReduce(a, inverseExtendedGCD(b.toBigInteger()));
    }

    public static FieldElement inverse(FieldElement a) {
        FieldElement result = FieldElement.One(); // 1
        FieldElement base = a;

        FieldElement pow = FieldElement.AllOne().unsetBit(0); // 2^128 - 2

        while (!pow.isZero()) {
            if (pow.testBit(0)) // if odd
                result = GFMul.mulAndReduce(result, base);

            base = GFMul.mulAndReduce(base, base);

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

}
