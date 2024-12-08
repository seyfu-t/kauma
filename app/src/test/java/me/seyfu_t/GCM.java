package me.seyfu_t;

import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import me.seyfu_t.actions.GCMEncryptAction;
import me.seyfu_t.model.FieldElement;

class GCM {

    String B64_12 = "MhwlLWs7k+Ab+Ft2";
    String B64_16_1 = "NdJ7yMuFbgr0XhJT210xjQ==";
    String B64_16_2 = "i9sLThhQ4mLztdXp2jYMtA==";
    String B64_16_3 = "D3NQ9n6RIOln3vdYMLaVsA==";
    String B64_16_4 = "K9Y1DNR+ojyATyz0Pg5gkA==";
    String B64_16_5 = "aQ5FmXmbTHK5GjqkqzTT0A==";
    String B64_24_1 = "lynqhz7T0QnOTGIB6ZkIasm7u0PvxYPb";
    String B64_24_2 = "hPls66CAA3nk9zIdhyec59rcz6NQufz5";

    // @Test
    // void ghash1(){
    //     UBigInt16 U_12 = UBigInt16.fromBase64(B64_12, true);


    //     UBigInt16 U_16_1 = UBigInt16.fromBase64(B64_16_1, true);
    //     UBigInt16 U_16_2 = UBigInt16.fromBase64(B64_16_2, true);
    //     UBigInt16 U_16_3 = UBigInt16.fromBase64(B64_16_3, true);
    //     UBigInt16 U_16_4 = UBigInt16.fromBase64(B64_16_4, true);

    //     byte[] U_24_1 = Base64.getDecoder().decode(B64_24_1);
    //     byte[] U_24_2 = Base64.getDecoder().decode(B64_24_2);


    //     var concat1 = GCMEncryptAction.concatNonceAndCounter(B64_12, 4);

    //     var L1 = GCMEncryptAction.calculateLengthOfADAndCiphertexts(U_24_1, U_24_2);
    //     var singleText1 = GCMEncryptAction.generateSingleTextBlock(0, "aes128", B64_12, U_16_1, U_16_2);
    //     var ghash1 = GCMEncryptAction.ghash(U_16_1, U_24_1, U_24_2, U_16_2);
    //     var fulltext1 = GCMEncryptAction.generateFullText("aes128", B64_12, U_16_2, U_24_1);
    //     var authKey1 = GCMEncryptAction.calculateAuthKey("aes128", U_16_2);
    //     var singleGHASH1 = GCMEncryptAction.singleGashBlock(U_16_1, U_16_2, U_16_3);
        
    //     var L2 = GCMEncryptAction.calculateLengthOfADAndCiphertexts(U_24_2, U_24_1);
    //     var singleText2 = GCMEncryptAction.generateSingleTextBlock(0, "aes128", B64_12, U_16_3, U_16_4);
    //     var ghash2 = GCMEncryptAction.ghash(U_16_2, U_24_2, U_24_1, U_16_1);
    //     var fulltext2 = GCMEncryptAction.generateFullText("aes128", B64_12, U_16_1, U_24_2);
    //     var authKey2 = GCMEncryptAction.calculateAuthKey("aes128", U_16_1);
    //     var singleGHASH2 = GCMEncryptAction.singleGashBlock(U_16_2, U_16_3, U_16_4);

    //     System.err.println(Arrays.toString(U_12.toByteArray()));

    //     System.err.println(concat1);

    //     System.err.println("\nFIRST BLOCK\n");

    //     System.err.println(L1);
    //     System.err.println(singleText1);
    //     System.err.println(ghash1);
    //     System.err.println(Arrays.toString(fulltext1));
    //     System.err.println(authKey1);
    //     System.err.println(singleGHASH1);

    //     System.err.println("\nSECOND BLOCK\n");

    //     System.err.println(L2);
    //     System.err.println(singleText2);
    //     System.err.println(ghash2);
    //     System.err.println(Arrays.toString(fulltext2));
    //     System.err.println(authKey2);
    //     System.err.println(singleGHASH2);
        
    // }

    // @Test
    // void ghashNew1(){
    //     FieldElement U_12 = FieldElement.fromBase64GCM(B64_12);

    //     FieldElement U_16_1 = FieldElement.fromBase64GCM(B64_16_1);
    //     FieldElement U_16_2 = FieldElement.fromBase64GCM(B64_16_2);
    //     FieldElement U_16_3 = FieldElement.fromBase64GCM(B64_16_3);
    //     FieldElement U_16_4 = FieldElement.fromBase64GCM(B64_16_4);

    //     byte[] U_24_1 = Base64.getDecoder().decode(B64_24_1);
    //     byte[] U_24_2 = Base64.getDecoder().decode(B64_24_2);


    //     var concat1 = GCMEncryptAction.concatNonceAndCounter(B64_12, 4);
    //     var L1 = GCMEncryptAction.calculateLengthOfADAndCiphertexts(U_24_1, U_24_2);
    //     var authKey1 = GCMEncryptAction.calculateAuthKey("aes128", U_16_2);
    //     var singleGHASH1 = GCMEncryptAction.singleGashBlock(U_16_1, U_16_2, U_16_3);


    //     var singleText1 = GCMEncryptAction.generateSingleTextBlock(0, "aes128", B64_12, U_16_1, U_16_2);
    //     var ghash1 = GCMEncryptAction.ghash(U_16_1, U_24_1, U_24_2, U_16_2);
    //     var fulltext1 = GCMEncryptAction.generateFullText("aes128", B64_12, U_16_2, U_24_1);
        
    //     ///////////////////////////////////////////////////////////////////////////////

    //     var L2 = GCMEncryptAction.calculateLengthOfADAndCiphertexts(U_24_2, U_24_1);
    //     var authKey2 = GCMEncryptAction.calculateAuthKey("aes128", U_16_1);
    //     var singleGHASH2 = GCMEncryptAction.singleGashBlock(U_16_2, U_16_3, U_16_4);
        
    //     var singleText2 = GCMEncryptAction.generateSingleTextBlock(0, "aes128", B64_12, U_16_3, U_16_4);
    //     var ghash2 = GCMEncryptAction.ghash(U_16_2, U_24_2, U_24_1, U_16_1);
    //     var fulltext2 = GCMEncryptAction.generateFullText("aes128", B64_12, U_16_1, U_24_2);

    //     System.err.println(Arrays.toString(U_12.toByteArrayGCM()));

    //     System.err.println(concat1);

    //     System.err.println("\nFIRST BLOCK\n");

    //     System.err.println(L1);
    //     System.err.println(singleText1);
    //     System.err.println(ghash1);
    //     System.err.println(Arrays.toString(fulltext1));
    //     System.err.println(authKey1);
    //     System.err.println(singleGHASH1);

    //     System.err.println("\nSECOND BLOCK\n");

    //     System.err.println(L2);
    //     System.err.println(singleText2);
    //     System.err.println(ghash2);
    //     System.err.println(Arrays.toString(fulltext2));
    //     System.err.println(authKey2);
    //     System.err.println(singleGHASH2);
        
    // }
 
    
}