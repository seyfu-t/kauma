package me.seyfu_t.actions;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElementGCM;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;

public class GFMulAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        String base64A = arguments.get("a").getAsString();
        String base64B = arguments.get("b").getAsString();

        boolean gcm = (semantic.equals("gcm"));

        if (gcm) {
            FieldElementGCM a = FieldElementGCM.fromBase64(base64A);
            FieldElementGCM b = FieldElementGCM.fromBase64(base64B);

            return ResponseBuilder.singleResponse("product", mulAndReduce(a, b).toBase64());
        } else {
            UBigInt16 a = UBigInt16.fromBase64(base64A);
            UBigInt16 b = UBigInt16.fromBase64(base64B);

            return ResponseBuilder.singleResponse("product", mulAndReduce(a, b).toBase64());
        }

    }

    public static FieldElementGCM mulAndReduce(FieldElementGCM a, FieldElementGCM b) {
        // Early zero checks
        if (a.isZero() || b.isZero())
            return FieldElementGCM.Zero();

        FieldElementGCM result = FieldElementGCM.Zero();

        // doing more than 128 rounds isn't possible
        for (int i = 0; i < UBigInt16.BYTE_COUNT * Byte.SIZE; i++) {
            if (b.testBit(0))
                result = result.xor(a);

            boolean overflow = a.testBit(127);
            a = a.shiftLeft(1);

            if (overflow)
                a = a.xor(FieldElementGCM.REDUCTION_POLY);

            b = b.shiftRight(1);

            // Early termination if b becomes zero
            if (b.isZero())
                break;
        }

        return result;
    }

    public static UBigInt16 mulAndReduce(UBigInt16 a, UBigInt16 b) {
        // Early zero checks
        if (a.isZero() || b.isZero())
            return UBigInt16.Zero(a.isGCM());

        UBigInt16 result = new UBigInt16(a.isGCM());

        // doing more than 128 rounds isn't possible
        for (int i = 0; i < UBigInt16.BYTE_COUNT * Byte.SIZE; i++) {
            if (b.testBit(0))
                result = result.xor(a);

            boolean overflow = a.testBit(127);
            a = a.shiftLeft(1);

            if (overflow)
                a = a.xor(UBigInt16.REDUCTION_POLY);

            b = b.shiftRight(1);

            // Early termination if b becomes zero
            if (b.isZero())
                break;
        }

        return result;
    }

}
