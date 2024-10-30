package me.seyfu_t.actions;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.Util;

public class GFMullAction implements Action {

    private static final BigInteger REDUCTION_POLY = BigInteger.ZERO.setBit(128).setBit(7).setBit(2).setBit(1)
            .setBit(0);

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
        BigInteger blockA = new BigInteger(Base64.getDecoder().decode(base64A)); 
        BigInteger blockB = new BigInteger(Base64.getDecoder().decode(base64B));

        // Make big-endian to work with internally
        blockA = Util.changeEndianness(blockA);
        blockB = Util.changeEndianness(blockB);

        BigInteger product = combinedMulAndModReductionInBigEndian(blockA, blockB);

        // Go back to little-endian
        product = Util.changeEndianness(product);
        byte[] sizedProduct = Util.littleEndianSignedBigIntTo16Bytes(product).toByteArray();

        String base64 = Base64.getEncoder().encodeToString(sizedProduct);

        return base64;
    }
    
    private static BigInteger combinedMulAndModReductionInBigEndian(BigInteger a, BigInteger b){
        BigInteger result = BigInteger.ZERO;
        
        while(!b.equals(BigInteger.ZERO)){ 

            if(b.testBit(0)){
                result = result.xor(a);
            }

            a = a.shiftLeft(1);

            if(a.testBit(128)){
                a = a.xor(REDUCTION_POLY);
            }
            
            b = b.shiftRight(1);
        }
        return result;
    }
}
