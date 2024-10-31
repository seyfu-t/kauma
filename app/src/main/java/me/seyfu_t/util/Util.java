package me.seyfu_t.util;

import java.math.BigInteger;
import java.util.Arrays;

public class Util {

    public static BigInteger changeEndianness(BigInteger num) {
        // Note: More efficient algorithm exists
        byte[] oldBytes = num.toByteArray();
        byte[] newBytes = new byte[oldBytes.length];

        for (int i = 0; i < oldBytes.length; i++) {
            newBytes[i] = oldBytes[oldBytes.length - i - 1];
        }
        return new BigInteger(1, newBytes);
    }

    // Method for little-endian cutting/padding to 16 bytes
    public static BigInteger littleEndianSignedBigIntTo16Bytes(BigInteger bigInt) {
        byte[] byteArray = bigInt.toByteArray();
        byte[] limitedByteArray = new byte[16];

        // Handle sign-byte
        if(byteArray[0] == 0){
            byteArray = Arrays.copyOfRange(byteArray, 1, byteArray.length);
        }
        for (int i = 0; i<16;i++){
            if(i < byteArray.length){
                limitedByteArray[i] = byteArray[i];
            }else{
                limitedByteArray[i] = 0;
            }
        }
        return new BigInteger(limitedByteArray);
    }

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
    
    public static boolean littleEndianCheckSigned(BigInteger bigInt){
        byte[] array = bigInt.toByteArray();
        return array.length == 17 && array[0] == 0;
    }

    public static BigInteger bigEndianSignedBigIntTo16Bytes(BigInteger bigInt){
        byte[] array = bigInt.toByteArray();
        // byte index 0 is the sign byte
        if(array.length == 17)
            array = Arrays.copyOfRange(array, 1, 17);
        return new BigInteger(array);
    }
}
