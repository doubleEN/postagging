package com.rui.probability;

import com.rui.ngram.WordTag;
import com.rui.statistic.AbstractParasCount;

/**
 * 计算两个概率矩阵，一个概率向量的类
 */
public abstract class AbstractParas {

    AbstractParasCount parasCount;

    protected double[][][] probMatA;

    protected double[][][] smoothingMatA;

    protected double[][] probMatB;

    protected double[] probPi;

    //添加语料
    public void addCorpus(String corpus){
        parasCount.countParas(corpus);
    }

    public void addCorpus(WordTag[] wordTags){
        parasCount.countParas(wordTags);
    }

    //计算概率参数的主要方法
    public void calcProbs(boolean smoothFlag) {
        int[][][] numA = this.parasCount.getNumMatA();
        int[][] numB = this.parasCount.getNumMatB();
        int[] numPi = this.parasCount.getNumPi();
        //这里最后计算A的概率，因为A的平滑需要pi的概率
        calcProbB(numB);
        calcProbPi(numPi);
        calcProbA(numA, numPi,smoothFlag);
    }

    //概率矩阵A
    protected void calcProbA(int[][][] numA, int[] numPi,boolean smoothFlag) {
        calcOriginalProbA(numA);
        if (smoothFlag) {
            this.smoothMatA(numPi, numA);
        }
    }

    //概率矩阵B
    protected abstract void calcProbB(int[][] numB);

    //概率向量pi
    protected abstract void calcProbPi(int[] numPi);

    //计算未平滑的概率A
    protected abstract void calcOriginalProbA(int[][][] numA);

    //计算平滑的概率A
    protected abstract void smoothMatA(int[] pi, int[][][] numMatA);


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
