package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import java.util.Arrays;

import static com.rui.util.GlobalParas.logger;

/**
 * 正确率度量
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
                this.printTagging(unknownSentences[i],predictedTags[i],expectedTags[i]);
            }
        }

        return correctNum / sum;
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
