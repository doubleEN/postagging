package com.rui.evaluation;

/**
 * 评估方式的接口
 */
public interface Estimator {

    double eval(String[][] predictedTags, String[][] expectedTags);

}
