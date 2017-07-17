package com.rui.probability;

import com.rui.statistic.AbstractParasCount;
import com.rui.statistic.BigramParasCount;

import java.util.Arrays;
import java.util.Map;

/**
 *
 */
public class BigramParas extends AbstractParas {

    public static void main(String[] args) {
        AbstractParas paras = new BigramParas();
        paras.addCorpus("/home/mjx/桌面/PoS/test/testSet2.txt");
        paras.calcProbs(true);

        System.out.println("probA:");
        for (double[] p : paras.getProbMatA()[0]) {
            System.out.println(Arrays.toString(p));
        }
        System.out.println("probPi:");
        System.out.println(Arrays.toString(paras.getProbPi()));

        System.out.println("smoothA:");
        for (double[] p : paras.getSmoothingMatA()[0]) {
            System.out.println(Arrays.toString(p));
        }
//
//        System.out.println("probB:");
//        for (double[] p : paras.getProbMatB()) {
//            System.out.println(Arrays.toString(p));
//        }

    }

    public BigramParas() {
        this.parasCount = new BigramParasCount();
    }

    @Override
    protected void calcOriginalProbA(int[][][] numA) {
        int[][] numMidA = numA[0];

        int len = numMidA.length;

        this.probMatA = new double[1][len][len];

        double[][] probMidA = probMatA[0];

        for (int row = 0; row < len; ++row) {

            double sumPerRow = 0;
            for (int col = 0; col < len; ++col) {
                sumPerRow += numMidA[row][col];
            }

            for (int col = 0; col < len; ++col) {
                if (sumPerRow != 0) {
                    probMidA[row][col] = (numMidA[row][col]) / (sumPerRow);
                } else {
                    //处理分母为0的情况
                    probMidA[row][col] = 0.0;
                }
            }
        }

    }

    @Override
    protected void calcProbB(int[][] numB) {

        int rowSize = numB.length;
        int colSize = numB[0].length;

        this.probMatB = new double[rowSize][colSize];

        for (int row = 0; row < rowSize; ++row) {
            double sumPerRow = 0;

            for (int col = 0; col < colSize; ++col) {
                sumPerRow += numB[row][col];
            }

            for (int col = 0; col < colSize; ++col) {
                if (sumPerRow != 0) {
                    probMatB[row][col] = (numB[row][col]) / (sumPerRow);
                } else {
                    //处理分母为0的情况
                    probMatB[row][col] = 0.0;
                }
            }
        }

    }

    @Override
    protected void calcProbPi(int[] numPi) {

        int vectorSize = numPi.length;

        this.probPi = new double[vectorSize];

        double sumOfVector = 0.0;
        for (int val : numPi) {
            sumOfVector += val;
        }
        for (int index = 0; index < vectorSize; ++index) {
            if (sumOfVector != 0) {
                this.probPi[index] = numPi[index] / sumOfVector;
            } else {
                this.probPi[index] = 0.0;
            }

        }

    }

    @Override
    protected void smoothMatA(int[] numPi, int[][][] numMatA) {
        int len = this.probMatA[0].length;

        this.smoothingMatA = new double[1][len][len];

        double lambd_count1 = 0.0;
        double lambd_count2 = 0.0;

        double sumOfTag = 0.0;
        for (int num : numPi) {
            sumOfTag += num;
        }
        System.out.println(sumOfTag);
        if (sumOfTag == 0) {
            System.err.println("隐藏状态数为0.");
        }
        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                int t_1_2 = numMatA[0][t_1][t_2] + numMatA[0][t_2][t_1];
                double expression1 = (numPi[t_2] - 1) / (sumOfTag - 1);
                double expression2 = 0.0;

                if (numPi[t_1] - 1 != 0) {
                    expression2 = (t_1_2 - 1) / (numPi[t_1] - 1);
                }
                //这里等号对结果的影响
                if (expression1 > expression2) {
                    lambd_count1 += t_1_2;
                } else {
                    lambd_count2 += t_1_2;
                }
            }
        }
        double lambd1 = lambd_count1 / (lambd_count1 + lambd_count2);
        double lambd2 = lambd_count2 / (lambd_count1 + lambd_count2);
        System.out.println(lambd1 + ":" + lambd2);

        double[][] midA = this.smoothingMatA[0];
        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                midA[t_1][t_2] = lambd1 * this.probPi[t_2] + lambd2 * this.probMatA[0][t_1][t_2];
            }
        }
    }
}
