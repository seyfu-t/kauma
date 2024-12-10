package me.seyfu_t.actions.gcm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;

import me.seyfu_t.actions.gfpoly.GFPolyFactorDDFAction;
import me.seyfu_t.actions.gfpoly.GFPolyFactorEDFAction;
import me.seyfu_t.actions.gfpoly.GFPolyFactorSFFAction;
import me.seyfu_t.actions.gfpoly.GFPolyMakeMonicAction;
import me.seyfu_t.model.Action;
import me.seyfu_t.model.CipherData;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.model.FieldElement;
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
        FieldElement[] authKeyCandidates = candidatesForH(m1, m2);

        Tuple<FieldElement, FieldElement> maskAndAuthKey = getMaskAndAuthKey(authKeyCandidates, m1, m3);

        FieldElement mask = maskAndAuthKey.getFirst();
        FieldElement authKey = maskAndAuthKey.getSecond();

        FieldElement forgedTag = forgeTag(authKey, mask, forgery);

        return ResponseBuilder.multiResponse(Arrays.asList(
                new Tuple<String, Object>("tag", forgedTag.toBase64XEX()),
                new Tuple<String, Object>("H", authKey.toBase64XEX()),
                new Tuple<String, Object>("mask", mask.toBase64XEX())));
    }

    private static FieldElement[] candidatesForH(CipherData m1, CipherData m2) {
        GFPoly combinedPoly = getEquation(m1, m2);
        List<Tuple<GFPoly, Integer>> sffList = GFPolyFactorSFFAction.sff(GFPolyMakeMonicAction.makeMonic(combinedPoly));

        List<GFPoly> sffPolyList = new ArrayList<>();
        for (Tuple<GFPoly, Integer> tuple : sffList)
            sffPolyList.add(tuple.getFirst());

        List<GFPoly> ddfPolyList = new ArrayList<>();
        for (GFPoly sffPoly : sffPolyList) {
            List<Tuple<GFPoly, Integer>> ddfTuples = GFPolyFactorDDFAction.ddf(sffPoly);

            for (Tuple<GFPoly, Integer> tuple : ddfTuples)
                if (tuple.getSecond() == RELEVANT_DEGREE)
                    ddfPolyList.add(tuple.getFirst());
        }

        List<GFPoly> edfPolyList = new ArrayList<>();
        for (GFPoly ddfPoly : ddfPolyList) {
            List<GFPoly> edfPolys = GFPolyFactorEDFAction.edf(ddfPoly, RELEVANT_DEGREE);
            edfPolyList.addAll(edfPolys);
        }

        FieldElement[] candidates = new FieldElement[edfPolyList.size()];

        for (int i = 0; i < edfPolyList.size(); i++)
            candidates[i] = edfPolyList.get(i).getCoefficient(0).swapInnerGCMState();

        return candidates;
    }

    private static GFPoly getEquation(CipherData m1, CipherData m2) {

        GFPoly poly1 = constructPolyForEquation(m1);
        GFPoly poly2 = constructPolyForEquation(m2);
        GFPoly combinedPoly = new GFPoly();

        int maxIndex = Math.max(poly1.degree(), poly2.degree());
        for (int i = 0; i <= maxIndex; i++)
            combinedPoly.setCoefficient(i, poly1.getCoefficient(i).xor(poly2.getCoefficient(i)).swapInnerGCMState());

        return combinedPoly;
    }

    private static GFPoly constructPolyForEquation(CipherData data) {
        GFPoly poly = new GFPoly();

        FieldElement lengthBlock = GCMEncryptAction.lengthBlock(data.ad, data.ciphertext);

        poly.setCoefficient(0, data.tag);
        poly.setCoefficient(1, lengthBlock);

        List<byte[]> ciphertextChunkList = Util.splitIntoChunks(data.ciphertext, FieldElement.BYTE_COUNT);
        List<byte[]> adChunkList = Util.splitIntoChunks(data.ad, FieldElement.BYTE_COUNT);

        int ciphertextMaxIndex = ciphertextChunkList.size() - 1;
        for (int i = 0; i <= ciphertextMaxIndex; i++)
            poly.setCoefficient(i + 2, new FieldElement(ciphertextChunkList.get(ciphertextMaxIndex - i)));

        int adMaxIndex = adChunkList.size() - 1;
        for (int i = 0; i <= adMaxIndex; i++)
            poly.setCoefficient(i + 3 + ciphertextMaxIndex, new FieldElement(adChunkList.get(adMaxIndex - i)));

        return poly;
    }

    private static Tuple<FieldElement, FieldElement> getMaskAndAuthKey(FieldElement[] candidates, CipherData m1,
            CipherData m3) {

        Tuple<FieldElement, FieldElement> maskAndAuthKey = null;

        for (FieldElement candidate : candidates) {
            FieldElement lengthBlock1 = GCMEncryptAction.lengthBlock(m1.ad, m1.ciphertext);
            FieldElement ghash1 = GCMEncryptAction.ghash(m1.ciphertext, m1.ad, candidate, lengthBlock1);

            FieldElement potentialMask = ghash1.xor(m1.tag);

            FieldElement lengthBlock3 = GCMEncryptAction.lengthBlock(m3.ad, m3.ciphertext);
            FieldElement ghash3 = GCMEncryptAction.ghash(m3.ciphertext, m3.ad, candidate, lengthBlock3);

            FieldElement calculatedTag = ghash3.xor(potentialMask);

            if (calculatedTag.equals(m3.tag)) {
                maskAndAuthKey = new Tuple<FieldElement, FieldElement>(potentialMask, candidate);
                break;
            }
        }

        return maskAndAuthKey;
    }

    private static FieldElement forgeTag(FieldElement authKey, FieldElement mask, CipherData forgery) {
        FieldElement lengthBlock = GCMEncryptAction.lengthBlock(forgery.ad, forgery.ciphertext);

        FieldElement ghashResult = GCMEncryptAction.ghash(forgery.ciphertext, forgery.ad, authKey, lengthBlock);

        return mask.xor(ghashResult);
    }
}
