package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

/**
 *
 */
public class Recall implements Estimator{

    @Override
    public double eval(DictFactory dict, String[] unknownSentences, String[][] predictedTags, String[][] expectedTags) {
        return 0;
    }
}
