package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import static com.rui.util.GlobalParas.logger;

/**
 * 召回率度量。
 */
public class Recall implements Estimator{
    /**
     * 是否打印标注错误的句子
     */
    boolean printFlag=false;


    public Recall(){

    }
    public Recall(boolean printFlag){
        this.printFlag=printFlag;
    }

    @Override
    public void eval(DictFactory dict, String unknownSentences, String[] predictedTags, String[] expectedTags) {

    }

    @Override
    public void printTagging(String unknownSentence, String[] predictedTags, String[] expectedTags) {

    }

    @Override
    public double getResult() {
        return 0;
    }

    @Override
    public void reset() {

    }
}
