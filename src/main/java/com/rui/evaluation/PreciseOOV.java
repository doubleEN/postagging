package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import java.util.Arrays;

import static com.rui.util.GlobalParas.logger;

/**
 * 未登录词准确率。
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
                this.printTagging(unknownSentences[i],predictedTags[i],expectedTags[i]);
            }
        }

        return correctOOV / sumOOV;
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
