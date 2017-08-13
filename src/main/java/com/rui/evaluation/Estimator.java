package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

/**
 * 评估接口
 */
public interface Estimator {
    double eval(DictFactory dict, String[] unknownSentences, String[][] predictedTags, String[][] expectedTags);
}
