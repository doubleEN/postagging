package com.rui.model;

import com.rui.ngram.WordTag;
import com.rui.probability.AbstractParas;

/**
 *
 */
public abstract class AbstractHMM {

    protected AbstractParas hmmParas;

    public abstract WordTag[] predict(String  sentences);

}
