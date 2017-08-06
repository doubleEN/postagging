package com.rui.evaluation;

/**
 * 评估方式的接口
 */
public interface Estimator {

    double eval(String[] unknownSentences,String[][] predictedTags, String[][] expectedTags);

}
