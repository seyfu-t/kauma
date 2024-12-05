package me.seyfu_t.actions;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;

public class GFDivAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String base64A = arguments.get("a").getAsString();
        String base64B = arguments.get("b").getAsString();

        UBigInt16 a = UBigInt16.fromBase64(base64A, true);
        UBigInt16 b = UBigInt16.fromBase64(base64B, true);
    
        return ResponseBuilder.singleResponse("q", div(a, b).toBase64());
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
