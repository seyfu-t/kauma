package me.seyfu_t.actions;

import java.util.Arrays;
import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.util.ResponseBuilder;

public class GCMDecryptAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String algorithm = arguments.get("algorithm").getAsString();
        String nonce = arguments.get("nonce").getAsString();
        String key = arguments.get("key").getAsString();
        String ciphertext = arguments.get("ciphertext").getAsString();
        String ad = arguments.get("ad").getAsString();
        String authTag = arguments.get("tag").getAsString();

        return gcmDecrypt(algorithm, nonce, key, ciphertext, ad, authTag);
    }

    private static JsonObject gcmDecrypt(String algorithm, String base64Nonce, String base64Key,
            String base64Ciphertext, String base64AD, String base64ExpectedAuthTag) {

        FieldElement key = FieldElement.fromBase64GCM(base64Key);
        FieldElement expectedAuthTag = FieldElement.fromBase64GCM(base64ExpectedAuthTag);

        byte[] ciphertextBytes = Base64.getDecoder().decode(base64Ciphertext);
        byte[] plaintextBytes = GCMEncryptAction.generateFullText(algorithm, base64Nonce, key, ciphertextBytes);
        byte[] adBytes = Base64.getDecoder().decode(base64AD);

        FieldElement H = GCMEncryptAction.calculateAuthKey(algorithm, key);
        FieldElement L = GCMEncryptAction.calculateLengthOfADAndCiphertexts(adBytes, ciphertextBytes);
        FieldElement ghash = GCMEncryptAction.ghash(H, adBytes, ciphertextBytes, L);
        // index -1 because 0 is the first ciphertext and this is one before that
        FieldElement actualAuthTag = GCMEncryptAction.generateSingleTextBlock(-1, algorithm, base64Nonce, key, ghash);

        String base64Plaintext = Base64.getEncoder().encodeToString(plaintextBytes);

        return ResponseBuilder.multiResponse(Arrays.asList(
                new Tuple<>("authentic", actualAuthTag.swapInnerGCMState().equals(expectedAuthTag)),
                new Tuple<>("plaintext", base64Plaintext)));
    }

}
