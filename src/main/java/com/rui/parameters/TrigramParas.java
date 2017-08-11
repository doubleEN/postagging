package com.rui.parameters;

import com.rui.dictionary.DictFactory;
import com.rui.wordtag.WordTag;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;

import java.util.Arrays;

/**
 * 三元语法参数训练。
 */
public class TrigramParas extends AbstractParas {
        /*
        计数参数
     */

    private int numOfLowGram=0;
    private int[][][] triNumMatA;

    private int[][] biNumMatA;

    private int[][] numMatB;

    private int[] numPi;

    private int[][][] holdOut;

    /*
        概率参数
     */
    private double[][][] triProbMatA;

    private double[][] biProbMatA;

    private double[][][] smoothingMatA;

    private double[][] probMatB;

    private double[] probPi;

    public TrigramParas() {
        this.dictionary = new DictFactory();
        this.triNumMatA = new int[1][1][1];
        this.biNumMatA = new int[1][1];
        this.holdOut = new int[1][1][1];
        this.numMatB = new int[1][1];
        this.numPi = new int[1];
    }

    public TrigramParas(String corpusPath) {
        this.dictionary = new DictFactory();
        this.triNumMatA = new int[1][1][1];
        this.biNumMatA = new int[1][1];
        this.holdOut = new int[1][1][1];
        this.numMatB = new int[1][1];
        this.numPi = new int[1];
        this.initParas(corpusPath);
    }



    public TrigramParas(String corpusPath, int tagNum, int wordNum) {
        this.dictionary = new DictFactory();
        this.triNumMatA = new int[tagNum][tagNum][tagNum];
        this.biNumMatA = new int[1][1];
        this.holdOut = new int[tagNum][tagNum][tagNum];
        this.numMatB = new int[tagNum][wordNum];
        this.numPi = new int[tagNum];
        this.initParas(corpusPath);
    }


    @Override
    protected WordTagStream chooseStream() {
        return new PeopleDailyWordTagStream();
    }

    @Override
    protected void countMatA(String[] tags) {
        if (tags.length < 3) {
            System.err.println("标注长度小于3，不能用于统计转移频数。");
            this.numOfLowGram++;
            return;
        }
        //是否扩展数组，在reBuildA方法内判断
        this.reBuildA();
        //三元标注状态统计
        for (int i = 2; i < tags.length; i++) {
            this.triNumMatA[this.getTagId(tags[i - 2])][this.getTagId(tags[i - 1])][this.getTagId(tags[i])]++;
        }
        //二元标注状态统计
        for (int i = 1; i < tags.length; i++) {
            this.biNumMatA[this.getTagId(tags[i - 1])][this.getTagId(tags[i])]++;
        }
    }

    @Override
    protected void countMatB(String[] words, String[] tags) {
        if (words.length != tags.length) {
            System.err.println("词组，标注长度不匹配。");
            return;
        }
        if (this.getSizeOfTags() > this.numMatB.length || this.getSizeOfWords() > this.numMatB[0].length) {
            this.reBuildB();
        }

        for (int i = 0; i < words.length; i++) {
            this.numMatB[this.getTagId(tags[i])][this.getWordId(words[i])]++;
        }
    }

    @Override
    protected void smoothMatB() {
        for (int i = 0; i < this.numMatB.length; ++i) {
            for (int j = 0; j < this.numMatB[0].length; ++j) {
                ++this.numMatB[i][j];
            }
        }
    }

    @Override
    protected void countPi(String[] tags) {
        if (this.getSizeOfTags() > this.numPi.length) {
            this.reBuildPi();
        }
        for (String tag : tags) {
            this.numPi[this.getTagId(tag)]++;
        }
    }

    @Override
    protected void reBuildA() {
        int len=this.getSizeOfTags();
        if (len > this.triNumMatA.length) {
            int[][][] newA = new int[len][len][len];
            for (int i = 0; i < this.triNumMatA.length; ++i) {
                for (int j = 0; j < this.triNumMatA.length; ++j) {
                    for (int k = 0; k < this.triNumMatA.length; ++k) {

                        newA[i][j][k] = this.triNumMatA[i][j][k];
                    }
                }
            }
            this.triNumMatA = newA;
        }

        if (len > this.biNumMatA.length) {
            int[][] newA = new int[len][len];
            for (int i = 0; i < this.biNumMatA.length; ++i) {
                for (int j = 0; j < this.biNumMatA[0].length; ++j) {
                    newA[i][j] = this.biNumMatA[i][j];
                }
            }
            this.biNumMatA = newA;
        }

    }

    @Override
    protected void reBuildB() {
        int row = this.getSizeOfTags() > this.numMatB.length ? this.getSizeOfTags() : this.numMatB.length;
        int col = this.getSizeOfWords() > this.numMatB[0].length ? this.getSizeOfWords() : this.numMatB[0].length;
        int[][] newB = new int[row][col];
        for (int i = 0; i < this.numMatB.length; ++i) {
            for (int j = 0; j < this.numMatB[0].length; ++j) {
                newB[i][j] = this.numMatB[i][j];
            }
        }
        this.numMatB = newB;
    }

    @Override
    protected void reBuildPi() {
        int[] pi = new int[this.getSizeOfTags()];
        for (int i = 0; i < this.numPi.length; ++i) {
            pi[i] = this.numPi[i];

        }
        this.numPi = pi;
    }

    @Override
    public void addHoldOut(WordTag[] wts) {
        if (wts.length < 3) {
            this.numOfLowGram++;
            System.err.println("句子长度不够，不能添加留存频数。");
            return;
        }
        this.dictionary.addIndex(wts);
        if (this.getSizeOfTags() > this.holdOut.length) {
            this.expandHoldOut();
        }
        for (int i = 2; i < wts.length; i++) {
            this.holdOut[this.getTagId(wts[i - 2].getTag())][this.getTagId(wts[i - 1].getTag())][this.getTagId(wts[i].getTag())]++;
        }
    }

    @Override
    protected void expandHoldOut() {
        int len = this.getSizeOfTags();
        int[][][] holdOut = new int[len][len][len];

        for (int i = 0; i < this.holdOut.length; ++i) {
            for (int j = 0; j < this.holdOut[0].length; ++j) {
                for (int k = 0; k < this.holdOut.length; ++k) {
                    holdOut[i][j][k] = this.holdOut[i][j][k];
                }
            }
        }
        this.holdOut = holdOut;
    }

    @Override
    protected void ensureLenOfTag() {
        int tagSize = this.getSizeOfTags();
        this.reBuildA();
        if (this.getSizeOfTags() > this.holdOut.length) {
            this.expandHoldOut();
        }

        if (this.getSizeOfTags() > this.numMatB.length || this.getSizeOfWords() > this.numMatB[0].length) {
            this.reBuildB();
        }
        if (this.getSizeOfTags() > this.numMatB.length || this.getSizeOfWords() > this.numMatB[0].length) {
            this.reBuildB();
        }
    }

    @Override
    protected void calcProbA() {

        int len = this.getSizeOfTags();
        //二元概率计算
        this.biProbMatA = new double[len][len];

        for (int row = 0; row < len; ++row) {

            double sumPerRow = 0;
            for (int col = 0; col < len; ++col) {
                sumPerRow += this.biNumMatA[row][col];
            }

            for (int col = 0; col < len; ++col) {
                if (sumPerRow != 0) {
                    this.biProbMatA[row][col] = (this.biNumMatA[row][col]) / (sumPerRow);
                } else {
                    //处理分母为0的情况
                    this.biProbMatA[row][col] = 0.0;
                }
            }
        }


        //三元概率计算
        this.triProbMatA = new double[len][len][len];

        //p(t_3|t_2,t_1)=num(t_3,t_2,t_1)/num(t_2,t_1)
        for (int t_1 = 0; t_1 < len; ++t_1) {

            for (int t_2 = 0; t_2 < len; ++t_2) {
                double sumPerRow = 0;

                for (int col = 0; col < len; ++col) {
                    sumPerRow += this.triNumMatA[t_1][t_2][col];
                }

                for (int t_3 = 0; t_3 < len; ++t_3) {
                    if (sumPerRow != 0) {
                        this.triProbMatA[t_1][t_2][t_3] = (this.triNumMatA[t_1][t_2][t_3]) / (sumPerRow);
                    } else {
                        //处理分母为0的情况
                        this.triProbMatA[t_1][t_2][t_3] = 0.0;
                    }
                }
            }

        }
    }

    @Override
    protected void calcProbB() {

        int rowSize = this.getSizeOfTags();
        int colSize = this.getSizeOfWords();

        this.probMatB = new double[rowSize][colSize];

        for (int row = 0; row < rowSize; ++row) {
            double sumPerRow = 0;

            for (int col = 0; col < colSize; ++col) {
                sumPerRow += this.numMatB[row][col];
            }

            for (int col = 0; col < colSize; ++col) {
                if (sumPerRow != 0) {
                    probMatB[row][col] = (this.numMatB[row][col]) / (sumPerRow);
                } else {
                    //处理分母为0的情况
                    probMatB[row][col] = 0.0;
                }
            }
        }

    }

    @Override
    protected void calcProbPi() {

        int vectorSize = this.getSizeOfTags();

        this.probPi = new double[vectorSize];

        double sumOfVector = 0.0;
        for (int val : this.numPi) {
            sumOfVector += val;
        }
        for (int index = 0; index < vectorSize; ++index) {
            if (sumOfVector != 0) {
                this.probPi[index] = this.numPi[index] / sumOfVector;
            } else {
                this.probPi[index] = 0.0;
            }

        }
    }

    @Override
    protected void smoothMatA() {

        //        System.out.println(sumOfTag);
        int len = this.getSizeOfTags();

        this.smoothingMatA = new double[len][len][len];

        double lambd_count1 = 0.0;
        double lambd_count2 = 0.0;
        double lambd_count3 = 0.0;

        //N
        int N = 0;
        for (int dim1 = 0; dim1 < len; ++dim1) {
            for (int dim2 = 0; dim2 < len; ++dim2) {
                for (int dim3 = 0; dim3 < len; ++dim3) {
                    N += this.holdOut[dim1][dim2][dim3];
                }
            }
        }
        if (N == 0) {
            System.err.println("留存数据不存在,不能平滑概率。");
            return;
        }
        //f(t_i)，严格来说，得到的t_i应该与隐藏状态的频次相等
        int[] t_i = new int[len];
        for (int dim1 = 0; dim1 < len; ++dim1) {
            for (int dim2 = 0; dim2 < len && dim2 != dim1; ++dim2) {

                for (int dim3 = 0; dim3 < len && dim3 != dim1 && dim3 != dim2; ++dim3) {
                    t_i[dim3] += this.holdOut[dim1][dim2][dim3];
                    t_i[dim2] += this.holdOut[dim1][dim2][dim3];
                    t_i[dim1] += this.holdOut[dim1][dim2][dim3];
                }

            }
            for (int temp = 0; temp < len && temp != dim1; ++temp) {
                t_i[dim1] += this.holdOut[dim1][dim1][temp];
                t_i[dim1] += this.holdOut[dim1][temp][dim1];
                t_i[dim1] += this.holdOut[temp][dim1][dim1];
            }
            t_i[dim1] += this.holdOut[dim1][dim1][dim1];
        }

        //f(t_i,t_j)
        int[][] t_i_j = new int[len][len];
        for (int dim1 = 0; dim1 < len; ++dim1) {
            for (int dim2 = 0; dim2 < len && dim2 != dim1; ++dim2) {

                for (int dim3 = 0; dim3 < len && dim3 != dim1 && dim3 != dim2; ++dim3) {
                    t_i_j[dim1][dim2] += this.holdOut[dim1][dim2][dim3];
                    t_i_j[dim2][dim3] += this.holdOut[dim1][dim2][dim3];
                }
            }

            for (int temp = 0; temp < len && temp != dim1; ++temp) {
                t_i_j[dim1][dim1] += this.holdOut[dim1][dim1][temp];
                t_i_j[dim1][temp] += this.holdOut[dim1][dim1][temp];

                t_i_j[dim1][temp] += this.holdOut[dim1][temp][dim1];
                t_i_j[temp][dim1] += this.holdOut[dim1][temp][dim1];

                t_i_j[dim1][dim1] += this.holdOut[temp][dim1][dim1];
                t_i_j[temp][dim1] += this.holdOut[temp][dim1][dim1];
            }

            t_i_j[dim1][dim1] += (this.holdOut[dim1][dim1][dim1] * 2);
        }

        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {

                for (int t_3 = 0; t_3 < len; ++t_3) {

                    // 系数：0.1514629948364888:0.8485370051635112
                    //系数：0.3683304647160069:0.6316695352839932
                    int t_1_2_3 = this.holdOut[t_1][t_2][t_3];
                    int t_2_3 = t_i_j[t_2][t_3];


                    double expression1 = (t_i[t_3] - 1) / (N - 1);
                    double expression2 = 0.0;
                    double expression3 = 0.0;

                    if (t_i[t_2] - 1 != 0) {
                        expression2 = (t_2_3 - 1) / (t_i[t_2] - 1);
                    }

                    if (t_i_j[t_1][t_2] - 1 != 0) {
                        expression2 = (t_1_2_3 - 1) / (t_i_j[t_1][t_2] - 1);
                    }

//                    System.out.println(expression1+":"+expression2+":"+expression3+"-->"+t_1_2_3);

                    //稀疏语料中，t_2的出现概率大多数情况下要比t_2的条件概率大，对应的t_2的联合频数t_1_2要小；
                    // 少数情况下，t_2的条件概率比t_2的出现概率大，这时对应的t_2的联合频数t_1_2要大；
                    //所以，虽然expression1大的情况多一些，但因为累加的联合频数偏小，所以最后对应系数并不会格外大
                    //是否取等号，对系数的取值影响很大
                    //因为是三元语法，留存数据不大时，数据非常稀疏
                    if (expression1 > expression2 && expression1 > expression3) {
                        lambd_count1 += t_1_2_3;
                    } else if (expression2 >expression3) {
                        lambd_count2 += t_1_2_3;
                    } else {
                        lambd_count3 += t_1_2_3;

                    }
                }
            }
        }

        double lambd1 = lambd_count1 / (lambd_count1 + lambd_count2 + lambd_count3);
        double lambd2 = lambd_count2 / (lambd_count1 + lambd_count2 + lambd_count3);
        double lambd3 = lambd_count3 / (lambd_count1 + lambd_count2 + lambd_count3);
        System.out.println("系数频次：" + lambd_count1 + ":" + lambd_count2+ ":" + lambd_count3);
        System.out.println("系数：" + lambd1 + ":" + lambd2 + ":" + lambd3);
//        System.out.println("系数的计数频次："+i1 + ":" + i2);

        //系数：0.9968451753824765:0.003154824617523456
        //系数：0.956853536835926:0.043146463164073966
        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                for (int t_3 = 0; t_3 < len; ++t_3) {
//                    问题：
                    this.smoothingMatA[t_1][t_2][t_3] = lambd1 * this.probPi[t_3] + lambd2 * this.biProbMatA[t_2][t_3] + lambd3 * this.triProbMatA[t_1][t_2][t_3];
                }
            }
        }
    }

    /*
        获得指定概率
     */
    @Override
    public double getProbPi(int indexOfTag) {
        return this.probPi[indexOfTag];
    }

    @Override
    public double getProbB(int indexOfTag, int indexOfWord) {
        return this.probMatB[indexOfTag][indexOfWord];
    }

    @Override
    public double getProbA(int... tagIndex) {
        if (tagIndex.length == 3) {
            return this.triProbMatA[tagIndex[0]][tagIndex[1]][tagIndex[2]];
        }else if (tagIndex.length==2){
            return this.biProbMatA[tagIndex[0]][tagIndex[1]];
        }else {
            System.err.println("参数不合法。");
            return -1;
        }
    }

    @Override
    public double getProbSmoothA(int... tagIndex) {
        if (tagIndex.length == 3) {
            return this.smoothingMatA[tagIndex[0]][tagIndex[1]][tagIndex[2]];
        }else if (tagIndex.length==2){
            //这个二元转移概率未平滑
            return this.biProbMatA[tagIndex[0]][tagIndex[1]];
        }else {
            System.err.println("参数不合法。");
            return -1;
        }
    }

    public int getNumOfLowGram() {
        return numOfLowGram;
    }
}
