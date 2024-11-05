package me.seyfu_t.actions;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;

public class GCMDecryptAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String algorithm = arguments.get("algorithm").getAsString();
        String nonce = arguments.get("nonce").getAsString();
        String key = arguments.get("key").getAsString();
        String ciphertext = arguments.get("ciphertext").getAsString();
        String ad = arguments.get("ad").getAsString();
        String authTag = arguments.get("tag").getAsString();

        Map<String, Object> resultMap = gcmDecrypt(algorithm, nonce, key, ciphertext, ad, authTag);
        return resultMap;
    }

    private static Map<String, Object> gcmDecrypt(String algorithm, String base64Nonce, String base64Key,
            String base64Ciphertext, String base64AD, String base64ExpectedAuthTag) {

        UBigInt16 key = UBigInt16.fromBase64(base64Key, true);
        UBigInt16 expectedAuthTag = UBigInt16.fromBase64(base64ExpectedAuthTag, true);

        byte[] ciphertextBytes = Base64.getDecoder().decode(base64Ciphertext);
        byte[] plaintextBytes = GCMEncryptAction.generateFullText(algorithm, base64Nonce, key, ciphertextBytes);
        byte[] adBytes = Base64.getDecoder().decode(base64AD);

        UBigInt16 H = GCMEncryptAction.calculateAuthKey(algorithm, key);
        UBigInt16 L = GCMEncryptAction.calculateLengthOfADAndCiphertexts(adBytes, ciphertextBytes);
        UBigInt16 ghash = GCMEncryptAction.ghash(H, adBytes, ciphertextBytes, L);
        // index -1 because 0 is the first ciphertext and this is one before that
        UBigInt16 actualAuthTag = GCMEncryptAction.generateSingleTextBlock(-1, algorithm, base64Nonce, key, ghash);

        String base64Plaintext = Base64.getEncoder().encodeToString(plaintextBytes);

        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("authentic", actualAuthTag.sameAs(expectedAuthTag));
        resultMap.put("plaintext", base64Plaintext);
        return resultMap;
    }

}
