package me.seyfu_t.actions.gcm;

import java.util.Arrays;
import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.util.ResponseBuilder;

public class GCMDecrypt implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String algorithm = arguments.get("algorithm").getAsString();

        byte[] nonce = Base64.getDecoder().decode(arguments.get("nonce").getAsString());
        byte[] key = Base64.getDecoder().decode(arguments.get("key").getAsString());
        byte[] ciphertext = Base64.getDecoder().decode(arguments.get("ciphertext").getAsString());
        byte[] ad = Base64.getDecoder().decode(arguments.get("ad").getAsString());
        byte[] expectedAuthTag = Base64.getDecoder().decode(arguments.get("tag").getAsString());

        return gcmDecrypt(algorithm, nonce, key, ciphertext, ad, expectedAuthTag);
    }

    public static JsonObject gcmDecrypt(String algorithm, byte[] nonce, byte[] key, byte[] ciphertext, byte[] ad,
            byte[] expectedAuthTag) {
        byte[] plaintextBytes = GCMEncrypt.crypt(algorithm, nonce, key, ciphertext);

        FieldElement lengthBlock = GCMEncrypt.lengthBlock(ad, ciphertext);
        FieldElement authKey = GCMEncrypt.authKey(algorithm, key);

        FieldElement ghash = GCMEncrypt.ghash(ciphertext, ad, authKey, lengthBlock);

        FieldElement actualAuthTagMask = new FieldElement(
                GCMEncrypt.mask(algorithm, key, nonce, GCMEncrypt.AUTH_TAG_COUNTER));
        FieldElement actualAuthTag = actualAuthTagMask.xor(ghash);

        return ResponseBuilder.multi(Arrays.asList(
                new Tuple<>("authentic", new FieldElement(expectedAuthTag).equals(actualAuthTag)),
                new Tuple<>("plaintext", Base64.getEncoder().encodeToString(plaintextBytes))));
    }

}
