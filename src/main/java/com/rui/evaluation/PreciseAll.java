package com.rui.evaluation;

import com.rui.dictionary.DictFactory;
import com.sun.org.apache.xpath.internal.SourceTree;

import static com.rui.util.GlobalParas.logger;
import java.util.Arrays;

/**
 * 标注准确率
 */
public class PreciseAll implements Estimator {

    private double preciseOfAll;

    private double preciseOfIV;

    private double preciseOfOOV;

    public double eval(DictFactory dict, String[] unknownSentences, String[][] predictedTags, String[][] expectedTags) {

        double sum = 0;
        double sumIV = 0;
        double sumOOV = 0;
        double correctNum = 0;
        double correctIV = 0;
        double correctOOV = 0;

        int sizeOfsentences = predictedTags.length;

        for (int i = 0; i < sizeOfsentences; ++i) {
            String[] words = unknownSentences[i].trim().split("\\s+");

            int lenOfSentence = predictedTags[i].length;
            boolean flag = false;
            sum += lenOfSentence;

            for (int j = 0; j < lenOfSentence; ++j) {
                if (dict.getWordId(words[j]) != null) {
                    ++sumIV;
                } else {
                    ++sumOOV;
                }

                if (predictedTags[i][j].equals(expectedTags[i][j])) {
                    ++correctNum;

                    if (dict.getWordId(words[j]) != null) {
                        ++correctIV;
                    } else {
                        ++correctOOV;
                    }

                } else {
                    flag = true;
                }
            }

            if (flag) {
                this.printTagging(unknownSentences[i],predictedTags[i],expectedTags[i]);
            }
        }

        this.preciseOfAll = correctNum / sum;
        this.preciseOfIV = correctIV / sumIV;
        this.preciseOfOOV = correctOOV / sumOOV;
//        System.out.println(this.preciseOfAll+" "+this.preciseOfIV+" "+this.preciseOfOOV);
//        System.out.println(correctOOV+"/"+sumOOV);//1444.0/3219.0=0.44858651755203477
//        System.out.println(correctIV+"/"+sumIV);
//        System.out.println(correctNum+"/"+sum);
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