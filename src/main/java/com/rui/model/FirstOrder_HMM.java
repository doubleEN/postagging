package com.rui.model;

import com.rui.ngram.WordTag;
import com.rui.probability.AbstractParas;

import java.io.FileInputStream;

/**
 *
 */
public class FirstOrder_HMM extends AbstractHMM {

    public FirstOrder_HMM(AbstractParas hmmParas) {
        this.hmmParas = hmmParas;
    }

    @Override
    public WordTag[] predict(String sentence) {

        String[] words = sentence.trim().split("\\s+");

        double[][] matrixA = this.hmmParas.getSmoothingMatA()[0];
        double[][] matrixB = this.hmmParas.getProbMatB();
        double[] vectorPi = this.hmmParas.getProbPi();

        double[][]probsOfsentence=new double[matrixA.length][words.length];

        return new WordTag[0];
    }
}
