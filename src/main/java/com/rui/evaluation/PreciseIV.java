package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import java.util.Arrays;

import static com.rui.util.GlobalParas.logger;

/**
 *
 */
public class PreciseIV implements Estimator{
    @Override
    public double eval(DictFactory dict, String[] unknownSentences, String[][] predictedTags, String[][] expectedTags) {
        double sumIV = 0;
        double correctIV = 0;

        int sizeOfsentences = predictedTags.length;
        for (int i = 0; i < sizeOfsentences; ++i) {
            String[] words = unknownSentences[i].trim().split("\\s+");

            int lenOfSentence = predictedTags[i].length;
            boolean flag = false;

            for (int j = 0; j < lenOfSentence && dict.getWordId(words[j]) != null; ++j) {
                ++sumIV;
                if (predictedTags[i][j].equals(expectedTags[i][j])) {
                    ++correctIV;
                } else {
                    flag = true;
                }
            }

            if (flag) {
                this.printTagging(unknownSentences[i],predictedTags[i],expectedTags[i]);
            }
        }

        return correctIV / sumIV;
    }
    @Override
    public void printTagging(String unknownSentence, String[] predictedTags, String[] expectedTags) {
        String predictedSentence="";
        String expectedSentence="";
        String[] words = unknownSentence.trim().split("\\s+");
        int len=words.length;
        for (int i=0;i<len;++i) {
            predictedSentence=predictedSentence+words[i]+"/"+predictedTags[i]+" ";
            expectedSentence=expectedSentence+words[i]+"/"+expectedTags[i]+" ";
        }
        logger.info("\n"+"Predict: "+"["+predictedSentence+"]"+"\n"+"Expect:  "+"["+expectedSentence+"]");
    }
}
