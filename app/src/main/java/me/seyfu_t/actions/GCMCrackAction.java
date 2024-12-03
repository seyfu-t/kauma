package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GCMCrackAction implements Action {

    private static final int RELEVANT_DEGREE = 1;

    @Override
    public JsonObject execute(JsonObject arguments) {
        String base64Nonce = arguments.get("nonce").getAsString();

        JsonObject m1 = arguments.get("m1").getAsJsonObject();
        String base64C1 = m1.get("ciphertext").getAsString();
        String base64AD1 = m1.get("associated_data").getAsString();
        String base64Tag1 = m1.get("tag").getAsString();

        JsonObject m2 = arguments.get("m2").getAsJsonObject();
        String base64C2 = m2.get("ciphertext").getAsString();
        String base64AD2 = m2.get("associated_data").getAsString();
        String base64Tag2 = m2.get("tag").getAsString();

        JsonObject m3 = arguments.get("m3").getAsJsonObject();
        String base64C3 = m3.get("ciphertext").getAsString();
        String base64AD3 = m3.get("associated_data").getAsString();
        String base64Tag3 = m3.get("tag").getAsString();

        JsonObject forgery = arguments.get("forgery").getAsJsonObject();
        String base64ForgeryC = forgery.get("ciphertext").getAsString();
        String base64ForgeryAD = forgery.get("associated_data").getAsString();

        return gcmCrack(base64Nonce, base64C1, base64AD1, base64Tag1, base64C2, base64AD2, base64Tag2, base64C3,
                base64AD3, base64Tag3, base64ForgeryC, base64ForgeryAD);
    }

    private JsonObject gcmCrack(String base64Nonce, String base64C1, String base64AD1, String base64Tag1,
            String base64C2, String base64AD2, String base64Tag2, String base64C3, String base64AD3,
            String base64Tag3, String base64ForgeryC, String base64ForgeryAD) {

        byte[] ciphertext1Bytes = Base64.getDecoder().decode(base64C1);
        byte[] ad1Bytes = Base64.getDecoder().decode(base64AD1);
        UBigInt16 tag1 = UBigInt16.fromBase64(base64Tag1, true);

        byte[] ciphertext2Bytes = Base64.getDecoder().decode(base64C2);
        byte[] ad2Bytes = Base64.getDecoder().decode(base64AD2);
        UBigInt16 tag2 = UBigInt16.fromBase64(base64Tag2, true);

        byte[] ciphertext3Bytes = Base64.getDecoder().decode(base64C3);
        byte[] ad3Bytes = Base64.getDecoder().decode(base64AD3);
        UBigInt16 tag3 = UBigInt16.fromBase64(base64Tag3, true);

        byte[] ciphertextForgeryBytes = Base64.getDecoder().decode(base64ForgeryC);
        byte[] adForgeryBytes = Base64.getDecoder().decode(base64ForgeryAD);

        UBigInt16[] authKeyCandidates = getHCandidates(ciphertext1Bytes, ad1Bytes, tag1, ciphertext2Bytes, ad2Bytes,
                tag2);

        Tuple<UBigInt16, UBigInt16> maskAndAuthKey = getMaskAndAuthKey(authKeyCandidates, ciphertext1Bytes, ad1Bytes,
                tag1, ciphertext3Bytes, ad3Bytes, tag3);

        UBigInt16 mask = maskAndAuthKey.getFirst();
        UBigInt16 authKey = maskAndAuthKey.getSecond();

        UBigInt16 forgedTag = getForgedTag(authKey, mask, ciphertextForgeryBytes, adForgeryBytes);

        return ResponseBuilder.multiResponse(Arrays.asList(
                new Tuple<String, Object>("tag", forgedTag.toBase64()),
                new Tuple<String, Object>("H", authKey.toBase64()),
                new Tuple<String, Object>("mask", mask.toBase64())));
    }

    private static UBigInt16[] getHCandidates(byte[] ciphertext1Bytes, byte[] ad1Bytes, UBigInt16 tag1,
            byte[] ciphertext2Bytes,
            byte[] ad2Bytes, UBigInt16 tag2) {
        GF128Poly combinedPoly = getEquation(ciphertext1Bytes, ad1Bytes, tag1, ciphertext2Bytes, ad2Bytes, tag2);

        List<Tuple<GF128Poly, Integer>> sffList = GFPolyFactorSFFAction
                .sff(GFPolyMakeMonicAction.makeMonic(combinedPoly));

        List<GF128Poly> sffPolyList = new ArrayList<>();
        for (Tuple<GF128Poly, Integer> tuple : sffList)
            sffPolyList.add(tuple.getFirst());

        List<GF128Poly> ddfPolyList = new ArrayList<>();
        for (GF128Poly sffPoly : sffPolyList) {
            List<Tuple<GF128Poly, Integer>> ddfTuples = GFPolyFactorDDFAction.ddf(sffPoly);

            for (Tuple<GF128Poly, Integer> tuple : ddfTuples)
                if (tuple.getSecond() == RELEVANT_DEGREE)
                    ddfPolyList.add(tuple.getFirst());
        }

        List<GF128Poly> edfPolyList = new ArrayList<>();
        for (GF128Poly ddfPoly : ddfPolyList) {
            List<GF128Poly> edfPolys = GFPolyFactorEDFAction.edf(ddfPoly, RELEVANT_DEGREE);
            edfPolyList.addAll(edfPolys);
        }

        UBigInt16[] candidates = new UBigInt16[edfPolyList.size()];

        for (int i = 0; i < edfPolyList.size(); i++)
            candidates[i] = edfPolyList.get(i).getCoefficient(0);

        return candidates;
    }

    private static GF128Poly getEquation(byte[] ciphertext1Bytes, byte[] ad1Bytes, UBigInt16 tag1,
            byte[] ciphertext2Bytes,
            byte[] ad2Bytes, UBigInt16 tag2) {

        GF128Poly poly1 = constructPolyForEquation(ciphertext1Bytes, ad1Bytes, tag1);
        GF128Poly poly2 = constructPolyForEquation(ciphertext2Bytes, ad2Bytes, tag2);
        GF128Poly combinedPoly = new GF128Poly();

        int maxIndex = Math.max(poly1.degree(), poly2.degree());
        for (int i = 0; i <= maxIndex; i++)
            combinedPoly.setCoefficient(i, poly1.getCoefficient(i).xor(poly2.getCoefficient(i)));

        return combinedPoly;
    }

    private static GF128Poly constructPolyForEquation(byte[] ciphertextBytes, byte[] adBytes, UBigInt16 tag) {
        GF128Poly poly = new GF128Poly();

        UBigInt16 lengthBlock = GCMEncryptAction.calculateLengthOfADAndCiphertexts(adBytes, ciphertextBytes);

        poly.setCoefficient(0, tag);
        poly.setCoefficient(1, lengthBlock);

        List<byte[]> ciphertextChunkList = Util.splitIntoChunks(ciphertextBytes, UBigInt16.BYTE_COUNT);
        List<byte[]> adChunkList = Util.splitIntoChunks(adBytes, UBigInt16.BYTE_COUNT);

        int ciphertextMaxIndex = ciphertextChunkList.size() - 1;
        for (int i = 0; i <= ciphertextMaxIndex; i++)
            poly.setCoefficient(i + 2, new UBigInt16(ciphertextChunkList.get(ciphertextMaxIndex - i), true));

        int adMaxIndex = adChunkList.size() - 1;
        for (int i = 0; i <= adMaxIndex; i++)
            poly.setCoefficient(i + 3 + ciphertextMaxIndex, new UBigInt16(adChunkList.get(adMaxIndex - i), true));

        return poly;
    }

    private static Tuple<UBigInt16, UBigInt16> getMaskAndAuthKey(UBigInt16[] candidates, byte[] ciphertext1Bytes,
            byte[] ad1Bytes, UBigInt16 tag1, byte[] ciphertext3Bytes, byte[] ad3Bytes, UBigInt16 tag3) {

        Tuple<UBigInt16,UBigInt16> maskAndAuthKey = null;

        for (UBigInt16 candidate : candidates) {
            UBigInt16 lengthBlock1 = GCMEncryptAction.calculateLengthOfADAndCiphertexts(ad1Bytes, ciphertext1Bytes);
            UBigInt16 ghash1 = GCMEncryptAction.ghash(candidate, ad1Bytes, ciphertext1Bytes, lengthBlock1);

            UBigInt16 potentialMask = ghash1.xor(tag1);

            UBigInt16 lengthBlock3 = GCMEncryptAction.calculateLengthOfADAndCiphertexts(ad3Bytes, ciphertext3Bytes);
            UBigInt16 ghash3 = GCMEncryptAction.ghash(candidate, ad3Bytes, ciphertext3Bytes, lengthBlock3);

            UBigInt16 calculatedTag = ghash3.xor(potentialMask);

            if(calculatedTag.sameAs(tag3)){
                maskAndAuthKey = new Tuple<UBigInt16,UBigInt16>(potentialMask, candidate);
                break;
            }
        }

        return maskAndAuthKey;
    }

    private static UBigInt16 getForgedTag(UBigInt16 authKey, UBigInt16 mask, byte[] ciphertextForgeryBytes,
            byte[] adForgeryBytes) {
        UBigInt16 lengthBlock = GCMEncryptAction.calculateLengthOfADAndCiphertexts(adForgeryBytes,
                ciphertextForgeryBytes);

        UBigInt16 ghashResult = GCMEncryptAction.ghash(authKey, adForgeryBytes, ciphertextForgeryBytes, lengthBlock);

        return mask.xor(ghashResult);
    }
}
