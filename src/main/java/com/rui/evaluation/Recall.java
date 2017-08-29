package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

import static com.rui.util.GlobalParas.logger;

/**
 * 召回率度量。
 */
public class Recall implements Estimator{

    @Override
    public double eval(DictFactory dict, String[] unknownSentences, String[][] predictedTags, String[][] expectedTags) {
        return 0;
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
