package me.seyfu_t.actions.gf;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.util.ResponseBuilder;

public class GFMul implements Action {

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

        return ResponseBuilder.single("product", response);

    }

    public static FieldElement mulAndReduce(FieldElement a, FieldElement b) {
        // Early zero checks
        if (a.isZero() || b.isZero())
            return FieldElement.Zero();

        FieldElement result = FieldElement.Zero();

        // doing more than 128 rounds isn't possible
        for (int i = 0; i < FieldElement.BYTE_COUNT * Byte.SIZE; i++) {
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

    public static FieldElement mulAndReduceGHASH(FieldElement a, FieldElement b) {
        return mulAndReduce(a.swapInnerGCMState(), b.swapInnerGCMState()).swapInnerGCMState();
    }

}
