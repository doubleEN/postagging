package com.rui.evaluation;

import java.util.Arrays;

/**
 * 标注准确率
 */
public class Precies implements Estimator {

    @Override
    public double eval(String[] unknownSentences, String[][] predictedTags, String[][] expectedTags) {

        double sum = 0;
        double correctNum = 0;

        int sizeOfsentences = predictedTags.length;
        for (int i = 0; i < sizeOfsentences; ++i) {

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
                System.out.println("expect->" + Arrays.toString(expectedTags[i]));
            }
        }
        return correctNum / sum;
    }
}