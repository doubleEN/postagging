package com.rui.evaluation;

import com.rui.dictionary.DictFactory;
import com.sun.org.apache.xpath.internal.SourceTree;

import static com.rui.util.GlobalParas.logger;
import java.util.Arrays;

/**
 * 各度量标准的汇总类。
 */
public class PreciseAll implements Estimator {
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