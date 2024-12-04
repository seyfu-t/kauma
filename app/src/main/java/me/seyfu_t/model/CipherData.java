package me.seyfu_t.model;

import java.util.Base64;

import com.google.gson.JsonObject;

public class CipherData {
    public final byte[] ciphertext;
    public final byte[] ad;
    public final UBigInt16 tag;

    public CipherData(JsonObject message) {
        this.ciphertext = Base64.getDecoder().decode(getJsonPart(message, "ciphertext"));
        this.ad = Base64.getDecoder().decode(getJsonPart(message, "associated_data"));
        this.tag = UBigInt16.fromBase64(getJsonPart(message, "tag"), true);
    }

    public byte[] getCiphertext() {
        return this.ciphertext;
    }

    public byte[] getAD() {
        return this.ad;
    }

    public UBigInt16 getTag() {
        return this.tag;
    }

    private static String getJsonPart(JsonObject message, String part) {
        try {
            return message.get(part).getAsString();
        } catch (Exception e) {
            return null;
        }
    }
}
