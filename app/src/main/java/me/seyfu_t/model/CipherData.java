package me.seyfu_t.model;

import java.util.Base64;

import com.google.gson.JsonObject;

public class CipherData {
    public final byte[] ciphertext;
    public final byte[] ad;
    public final FieldElement tag;

    public CipherData(JsonObject message) {
        this.ciphertext = Base64.getDecoder().decode(getJsonPart(message, "ciphertext"));
        this.ad = Base64.getDecoder().decode(getJsonPart(message, "associated_data"));
        String base64Tag = getJsonPart(message, "tag");
        this.tag = (base64Tag == null ? null : new FieldElement(Base64.getDecoder().decode(base64Tag)));
    }

    public byte[] getCiphertext() {
        return this.ciphertext;
    }

    public byte[] getAD() {
        return this.ad;
    }

    public FieldElement getTag() {
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
