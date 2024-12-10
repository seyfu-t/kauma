package me.seyfu_t.actions.gf;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
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
        return GFMulAction.mulAndReduce(a, inverse(b));
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


    // extended Euclidean algorithm
    public static FieldElement inverseExtendedGCD1(FieldElement fe) {
        FieldElement a = new FieldElement(fe.toByteArrayXEX());
        FieldElement reduction = FieldElement.REDUCTION_POLY;
        FieldElement x = FieldElement.Zero();
        FieldElement lastX = FieldElement.One();
        FieldElement y = FieldElement.One();
        FieldElement lastY = FieldElement.Zero();
        
        // polynomial long division
        while (!a.isZero()) {
            int aDegree = a.getHighestSetBit();
            int reductionDegree = reduction.getHighestSetBit();
            
            // Compute degree of quotient
            if (aDegree < reductionDegree) {
                // Swap a and reduction
                FieldElement temp = a;
                a = reduction;
                reduction = temp;
                
                // Swap x and lastX
                temp = x;
                x = lastX;
                lastX = temp;
                
                // Swap y and lastY
                temp = y;
                y = lastY;
                lastY = temp;
                
                continue;
            }
            
            // XOR the polynomials
            int shift = aDegree - reductionDegree;
            FieldElement quotientTerm = reduction.shiftLeft(shift);
            a = a.xor(quotientTerm);
            
            // Update x and y through polynomial arithmetic
            FieldElement quotientX = lastX.shiftLeft(shift);
            x = x.xor(quotientX);
            
            FieldElement quotientY = lastY.shiftLeft(shift);
            y = y.xor(quotientY);
        }
        
        return lastX;
    }

    // extended Euclidean algorithm
    public static FieldElement inverseExtendedGCD2(FieldElement fe) {
        FieldElement u = new FieldElement(fe.toByteArrayXEX());
        FieldElement reduction = FieldElement.REDUCTION_POLY;
        FieldElement x = FieldElement.Zero();
        FieldElement lastX = FieldElement.One();
        FieldElement y = FieldElement.One();
        FieldElement lastY = FieldElement.Zero();
        
        return lastX;
    }

}
