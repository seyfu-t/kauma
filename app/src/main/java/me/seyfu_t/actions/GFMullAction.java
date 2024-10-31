package me.seyfu_t.actions;

import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;

public class GFMullAction implements Action {

    // skipping index 128 (because only 16 bytes), will fall out when using XOR anyway
    private static final UBigInt16 REDUCTION_POLY = new UBigInt16().setBit(7).setBit(2).setBit(1).setBit(0);

    @Override
    public Entry<String, Object> execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        String a = arguments.get("a").getAsString();
        String b = arguments.get("b").getAsString();

        String product = "";

        if (semantic.equalsIgnoreCase("xex")) {
            product = mulGF(a, b);
        }

        return new AbstractMap.SimpleEntry<>("product", product);
    }

    private static String mulGF(String base64A, @SuppressWarnings("unused") String base64B) {
        byte[] blockA = Base64.getDecoder().decode(base64A);
        byte[] blockB = Base64.getDecoder().decode(base64B);

        UBigInt16 bigIntA = new UBigInt16(blockA);
        UBigInt16 bigIntB = new UBigInt16(blockB);

        UBigInt16 product = combinedMulAndModReductionInBigEndian(bigIntA, bigIntB);
        String base64 = Base64.getEncoder().encodeToString(product.toByteArray());

        return base64;
    }

    private static UBigInt16 combinedMulAndModReductionInBigEndian(UBigInt16 a, UBigInt16 b) {
        UBigInt16 result = new UBigInt16();
        while (!b.sameAs(new UBigInt16())) {
            boolean overflow;
            if (b.testBit(0)) {
                result = result.xor(a);
            }

            overflow = a.testBit(127);
            
            a = a.shiftLeft(1);

            if (overflow) {
                a = a.xor(REDUCTION_POLY);
            }

            b = b.shiftRight(1);
        }
        return result;
    }
}
