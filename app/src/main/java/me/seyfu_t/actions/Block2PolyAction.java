package me.seyfu_t.actions;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.Util;

public class Block2PolyAction implements Action {

    @Override
    public Entry<String, Object> execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        String base64Block = arguments.get("block").getAsString();

        int[] coefficients = null;

        if (semantic.equalsIgnoreCase("xex")) {
            coefficients = convertBlock2PolyXEX(base64Block);
        }

        return new AbstractMap.SimpleEntry<>("coefficients", coefficients);
    }

    private static int[] convertBlock2PolyXEX(String base64Block) {
        byte[] blockByteArray = Base64.getDecoder().decode(base64Block);

        // BigInt from byte array
        BigInteger block = new BigInteger(1, blockByteArray);
        block = Util.changeEndianness(block);
        
        int[] coefficients = new int[block.bitCount()];
        int slot = 0;// to know the current index of the coefficients array
        // check each bit and add coefficient if bit is set
        for(int i = 0; i<16*8;i++){
            if(block.testBit(i)){
                coefficients[slot] = i;
                slot++;
            }
        }

        Arrays.sort(coefficients);
        return coefficients;
    }

}
