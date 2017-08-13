package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import java.util.Arrays;

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
                System.out.println(unknownSentences[i]);
                System.out.println("predict->" + Arrays.toString(predictedTags[i]));
                System.out.println("expect ->" + Arrays.toString(expectedTags[i]));
            }
        }

        return correctIV / sumIV;
    }
}
