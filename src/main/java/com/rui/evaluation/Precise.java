package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import java.util.Arrays;

/**
 *
 */
public class Precise implements Estimator {
    @Override
    public double eval(DictFactory dict, String[] unknownSentences, String[][] predictedTags, String[][] expectedTags) {

        double sum = 0;
        double correctNum = 0;

        int sizeOfSentences = predictedTags.length;

        for (int i = 0; i < sizeOfSentences; ++i) {
            String[] words = unknownSentences[i].trim().split("\\s+");

            int lenOfSentence = predictedTags[i].length;
            boolean flag = false;
            sum += lenOfSentence;

            for (int j = 0; j < lenOfSentence; ++j) {
                if (predictedTags[i][j].equals(expectedTags[i][j])) {
                    ++correctNum;
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

        return correctNum / sum;
    }


}
