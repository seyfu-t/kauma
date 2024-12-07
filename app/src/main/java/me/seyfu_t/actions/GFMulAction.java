package me.seyfu_t.actions;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;

public class GFMulAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        String base64A = arguments.get("a").getAsString();
        String base64B = arguments.get("b").getAsString();

        boolean gcm = (semantic.equals("gcm"));

        FieldElement a;
        FieldElement b;
        String response;
        
        if (gcm) {
            a = FieldElement.fromBase64GCM(base64A);
            b = FieldElement.fromBase64GCM(base64B);
            
            response = mulAndReduce(a, b).toBase64GCM();
        } else {
            a = FieldElement.fromBase64XEX(base64A);
            b = FieldElement.fromBase64XEX(base64B);
            
            response = mulAndReduce(a, b).toBase64XEX();
        }

        return ResponseBuilder.singleResponse("product", response);

    }

    public static FieldElement mulAndReduce(FieldElement a, FieldElement b) {
        // Early zero checks
        if (a.isZero() || b.isZero())
            return FieldElement.Zero();

        FieldElement result = FieldElement.Zero();

        // doing more than 128 rounds isn't possible
        for (int i = 0; i < UBigInt16.BYTE_COUNT * Byte.SIZE; i++) {
            if (b.testBit(0))
                result = result.xor(a);

            boolean overflow = a.testBit(127);
            a = a.mulBy2();

            if (overflow)
                a = a.xor(FieldElement.REDUCTION_POLY);

            b = b.divBy2();
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
