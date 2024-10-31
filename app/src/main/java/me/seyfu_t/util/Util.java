package me.seyfu_t.util;

public class Util {

    public static byte[] swapByteOrder(byte[] byteArray){
        if (byteArray == null) {
            return null; // Handle null input
        }
        
        int length = byteArray.length;
        byte[] swappedArray = new byte[length];
        
        for (int i = 0; i < length; i++) {
            swappedArray[i] = byteArray[length - i - 1];
        }
        
        return swappedArray;
    }

}
