package com.rui.evaluation;

/**
 * 标注准确率
 */
public class Precies implements Estimator{

    @Override
    public double eval(String[][] predictedTags, String[][] expectedTags) {

        double sum = 0;
        double correctNum = 0;

        int sizeOfsentences = predictedTags.length;
        for (int i = 0; i < sizeOfsentences; ++i) {
            int lenOfSentence = predictedTags[i].length;
            sum += lenOfSentence;
            for (int j = 0; j < lenOfSentence; ++j) {
                if (predictedTags[i][j].equals(expectedTags[i][j])) {
                    ++correctNum;
                }
            }
        }
        return correctNum / sum;
    }
}
