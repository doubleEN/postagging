package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import java.util.Arrays;

/**
 *
 */
public class PreciseOOV implements Estimator {

    @Override
    public double eval(DictFactory dict, String[] unknownSentences, String[][] predictedTags, String[][] expectedTags) {
        double sumOOV = 0;
        double correctOOV = 0;

        int sizeOfsentences = predictedTags.length;
        for (int i = 0; i < sizeOfsentences; ++i) {
            String[] words = unknownSentences[i].trim().split("\\s+");

            int lenOfSentence = predictedTags[i].length;
            boolean flag = false;

            for (int j = 0; j < lenOfSentence; ++j) {
                if (dict.getWordId(words[j]) == null) {
                    ++sumOOV;
                    if (predictedTags[i][j].equals(expectedTags[i][j])) {
                        ++correctOOV;
                    } else {
                        flag = true;
                    }
                }
            }

            if (flag) {
                System.out.println(unknownSentences[i]);
                System.out.println("predict->" + Arrays.toString(predictedTags[i]));
                System.out.println("expect ->" + Arrays.toString(expectedTags[i]));
            }
        }

        return correctOOV / sumOOV;
    }

}
