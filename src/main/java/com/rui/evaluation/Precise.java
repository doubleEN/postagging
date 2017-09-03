package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import java.util.Arrays;

import static com.rui.util.GlobalParas.logger;

/**
 * 正确率度量
 */
public class Precise implements Estimator {
    /**
     * 是否打印标注错误的句子
     */
    boolean printFlag=false;

    /**
     * 累积输入评估对象的评估单元总数
     */
    private double sum;

    /**
     * 累积输入评估对象的正确评估单元总数
     */
    private double correctNum;

    public Precise(){

    }
    public Precise(boolean printFlag){
        this.printFlag=printFlag;
    }


    @Override
    public void eval(DictFactory dict, String unknownSentences, String[] predictedTags, String[] expectedTags) {

        String[] words = unknownSentences.trim().split("\\s+");

        int lenOfSentence = predictedTags.length;
        boolean flag = false;
        sum += lenOfSentence;

        for (int j = 0; j < lenOfSentence; ++j) {
            if (predictedTags[j].equals(expectedTags[j])) {
                ++correctNum;
            } else {
                flag = true;
            }
        }
        if (this.printFlag) {
            if (flag) {
                this.printTagging(unknownSentences, predictedTags, expectedTags);
            }
        }
    }

    @Override
    public void printTagging(String unknownSentence, String[] predictedTags, String[] expectedTags) {
        String predictedSentence = "";
        String expectedSentence = "";
        String[] words = unknownSentence.trim().split("\\s+");
        int len = words.length;
        for (int i = 0; i < len; ++i) {
            predictedSentence = predictedSentence + words[i] + "/" + predictedTags[i] + " ";
            expectedSentence = expectedSentence + words[i] + "/" + expectedTags[i] + " ";
        }
        logger.info("\n" + "Predict: " + "[" + predictedSentence + "]" + "\n" + "Expect:  " + "[" + expectedSentence + "]");
    }

    @Override
    public double getResult() {
        return this.correctNum/this.sum;
    }

    @Override
    public void reset() {
        this.sum=0;
        this.correctNum=0;
    }
}
