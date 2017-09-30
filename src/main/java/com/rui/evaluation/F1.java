package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import static com.rui.util.GlobalParas.logger;

/**
 * F1度量
 */
public class F1 implements Estimator{
    /**
     * 是否打印标注错误的句子
     */
    boolean printFlag=false;

    private double correctNum;

    public F1(){

    }
    public F1(boolean printFlag){
        this.printFlag=printFlag;
    }


    @Override
    public void reset() {
    }

    @Override
    public double getResult(){
        return 0;
    }

    @Override
    public void eval(DictFactory dict, String unknownSentences, String[] predictedTags, String[] expectedTags) {
        return ;
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
