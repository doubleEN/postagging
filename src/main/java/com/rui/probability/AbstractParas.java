package com.rui.probability;

import com.rui.statistic.AbstractParasCount;
import com.rui.statistic.BigramParasCount;

/**
 *
 */
public abstract class AbstractParas {

    protected double[][][] probMatA;

    protected double[][][] smoothingMatA;

    protected double[][] probMatB;

    protected double[] probPi;

    public void calcProbs(String corpusPath,boolean smoothFlag) {
        AbstractParasCount statisticFactory = new BigramParasCount();
        statisticFactory.countParas(corpusPath);
        int[][][] numA = statisticFactory.getNumMatA();
        int[][] numB = statisticFactory.getNumMatB();
        int[] numPi = statisticFactory.getNumPi();
        //这里最后计算A的概率，因为A的平滑需要pi的概率
        calcProbB(numB);
        calcProbPi(numPi);
        calcProbA(numA, numPi,smoothFlag);
    }

    protected void calcProbA(int[][][] numA, int[] numPi,boolean smoothFlag) {
        calcOriginalProbA(numA);
        if (smoothFlag) {
            this.smoothMatA(numPi, numA);
        }
    }

    protected abstract void calcOriginalProbA(int[][][] numA);

    protected abstract void smoothMatA(int[] pi, int[][][] numMatA);

    protected abstract void calcProbB(int[][] numB);

    protected abstract void calcProbPi(int[] numPi);

    public double[][][] getProbMatA() {
        return probMatA;
    }

    public double[][][] getSmoothingMatA() {
        return smoothingMatA;
    }

    public double[][] getProbMatB() {
        return probMatB;
    }

    public double[] getProbPi() {
        return probPi;
    }
}
