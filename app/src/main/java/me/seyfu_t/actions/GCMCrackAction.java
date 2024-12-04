package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.CipherData;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GCMCrackAction implements Action {

    private static final int RELEVANT_DEGREE = 1;

    @Override
    public JsonObject execute(JsonObject arguments) {
        CipherData m1 = new CipherData(arguments.get("m1").getAsJsonObject());
        CipherData m2 = new CipherData(arguments.get("m2").getAsJsonObject());
        CipherData m3 = new CipherData(arguments.get("m3").getAsJsonObject());
        CipherData forgery = new CipherData(arguments.get("forgery").getAsJsonObject());

        return gcmCrack(m1, m2, m3, forgery);
    }

    private JsonObject gcmCrack(CipherData m1, CipherData m2, CipherData m3, CipherData forgery) {
        UBigInt16[] authKeyCandidates = getHCandidates(m1, m2);

        Tuple<UBigInt16, UBigInt16> maskAndAuthKey = getMaskAndAuthKey(authKeyCandidates, m1, m3);

        UBigInt16 mask = maskAndAuthKey.getFirst();
        UBigInt16 authKey = maskAndAuthKey.getSecond();

        UBigInt16 forgedTag = getForgedTag(authKey, mask, forgery);

        return ResponseBuilder.multiResponse(Arrays.asList(
                new Tuple<String, Object>("tag", forgedTag.toBase64()),
                new Tuple<String, Object>("H", authKey.toBase64()),
                new Tuple<String, Object>("mask", mask.toBase64())));
    }

    private static UBigInt16[] getHCandidates(CipherData m1, CipherData m2) {
        GF128Poly combinedPoly = getEquation(m1, m2);

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

    private static GF128Poly getEquation(CipherData m1, CipherData m2) {

        GF128Poly poly1 = constructPolyForEquation(m1);
        GF128Poly poly2 = constructPolyForEquation(m2);
        GF128Poly combinedPoly = new GF128Poly();

        int maxIndex = Math.max(poly1.degree(), poly2.degree());
        for (int i = 0; i <= maxIndex; i++)
            combinedPoly.setCoefficient(i, poly1.getCoefficient(i).xor(poly2.getCoefficient(i)));

        return combinedPoly;
    }

    private static GF128Poly constructPolyForEquation(CipherData data) {
        GF128Poly poly = new GF128Poly();

        UBigInt16 lengthBlock = GCMEncryptAction.calculateLengthOfADAndCiphertexts(data.ad, data.ciphertext);

        poly.setCoefficient(0, data.tag);
        poly.setCoefficient(1, lengthBlock);

        List<byte[]> ciphertextChunkList = Util.splitIntoChunks(data.ciphertext, UBigInt16.BYTE_COUNT);
        List<byte[]> adChunkList = Util.splitIntoChunks(data.ad, UBigInt16.BYTE_COUNT);

        int ciphertextMaxIndex = ciphertextChunkList.size() - 1;
        for (int i = 0; i <= ciphertextMaxIndex; i++)
            poly.setCoefficient(i + 2, new UBigInt16(ciphertextChunkList.get(ciphertextMaxIndex - i), true));

        int adMaxIndex = adChunkList.size() - 1;
        for (int i = 0; i <= adMaxIndex; i++)
            poly.setCoefficient(i + 3 + ciphertextMaxIndex, new UBigInt16(adChunkList.get(adMaxIndex - i), true));

        return poly;
    }

    private static Tuple<UBigInt16, UBigInt16> getMaskAndAuthKey(UBigInt16[] candidates, CipherData m1, CipherData m3) {

        Tuple<UBigInt16, UBigInt16> maskAndAuthKey = null;

        for (UBigInt16 candidate : candidates) {
            UBigInt16 lengthBlock1 = GCMEncryptAction.calculateLengthOfADAndCiphertexts(m1.ad, m1.ciphertext);
            UBigInt16 ghash1 = GCMEncryptAction.ghash(candidate, m1.ad, m1.ciphertext, lengthBlock1);

            UBigInt16 potentialMask = ghash1.xor(m1.tag);

            UBigInt16 lengthBlock3 = GCMEncryptAction.calculateLengthOfADAndCiphertexts(m3.ad, m3.ciphertext);
            UBigInt16 ghash3 = GCMEncryptAction.ghash(candidate, m3.ad, m3.ciphertext, lengthBlock3);

            UBigInt16 calculatedTag = ghash3.xor(potentialMask);

            if (calculatedTag.sameAs(m3.tag)) {
                maskAndAuthKey = new Tuple<UBigInt16, UBigInt16>(potentialMask, candidate);
                break;
            }
        }

        return maskAndAuthKey;
    }

    private static UBigInt16 getForgedTag(UBigInt16 authKey, UBigInt16 mask, CipherData forgery) {
        UBigInt16 lengthBlock = GCMEncryptAction.calculateLengthOfADAndCiphertexts(forgery.ad,
                forgery.ciphertext);

        UBigInt16 ghashResult = GCMEncryptAction.ghash(authKey, forgery.ad, forgery.ciphertext, lengthBlock);

        return mask.xor(ghashResult);
    }
}
