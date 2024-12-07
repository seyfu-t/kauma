package me.seyfu_t.actions;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.UBigInt16;
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

    public static UBigInt16 div(UBigInt16 a, UBigInt16 b) {
        return GFMulAction.mulAndReduce(a, inverse(b));
    }

    public static UBigInt16 inverse(UBigInt16 a) {
        UBigInt16 result = UBigInt16.Zero(true).setBit(0); // 1
        UBigInt16 base = a;

        UBigInt16 pow = UBigInt16.AllOne().unsetBit(0); // 2^128 - 2

        while (!pow.isZero()) {
            if (pow.testBit(0)) // if odd
                result = GFMulAction.mulAndReduce(result, base);

            base = GFMulAction.mulAndReduce(base, base);

            pow = pow.shiftRight(1); // div by 2
        }

        return result;
    }

}
