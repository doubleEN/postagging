package com.rui.parameter;

import com.rui.dictionary.DictFactory;
import com.rui.util.GlobalMethods;
import com.rui.util.GlobalParas;
import com.rui.wordtag.WordTag;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;

import java.io.IOException;

import static com.rui.util.GlobalParas.logger;

/**
 * 三元语法参数训练。
 */
public class TrigramParas extends AbstractParas {

    /**
     * 3-gram状态转移计数矩阵
     * 三维数组中的每一个三元组代表时序上相连的三元隐藏状态[t_i,t_i+1,t_i+2]
     * 在相同[t_i,t_i+1]的情况下，下一个可能的隐藏状态的计数，即[t_i,t_i+1]-->[t_i+2]
     */
    private int[][][] triNumMatA;

    /**
     * 2-gram状态转移计数矩阵
     * 每一行为同一个隐藏转移状态下，转移到可能的下一个隐藏状态的计数，[t_i]-->[t_i+1]
     */
    private int[][] biNumMatA;

    /**
     * 3-gram留存状态转移计数矩阵
     * 三维数组中的每一个三元组代表时序上相连的三元隐藏状态[t_i,t_i+1,t_i+2]
     * 在相同[t_i,t_i+1]的情况下，下一个可能的隐藏状态的计数，即[t_i,t_i+1]-->[t_i+2]
     */
    private int[][][] holdOut;

    /**
     * 3-gram状态转移概率矩阵
     * 三维数组中的每一个三元组代表时序上相连的三元隐藏状态[t_i,t_i+1,t_i+2]
     * 在相同[t_i,t_i+1]的情况下，下一个可能的隐藏状态的概率，即 P([t_i,t_i+1]-->[t_i+2])
     */
    private double[][][] triProbMatA;

    /**
     * 2-gram状态转移概率矩阵
     * 每一行为同一个隐藏转移状态下，转移到可能的下一个隐藏状态的概率，即 p([t_i]-->[t_i+1])
     */
    private double[][] biProbMatA;

    /**
     * 3-gram状态转移平滑概率矩阵
     * 对triProbMatA的平滑
     */
    private double[][][] smoothingMatA;

    public TrigramParas(int unkHandle) {
        this.dictionary = new DictFactory();
        this.triNumMatA = new int[1][1][1];
        this.biNumMatA = new int[1][1];
        this.holdOut = new int[1][1][1];
        this.numMatB = new int[1][1];
        this.numPi = new int[1];
        this.unkHandle = unkHandle;
        logger.info("使用 " + GlobalParas.getUnkHandle(unkHandle));
    }


    public TrigramParas(DictFactory dict,int unkHandle) {
        this.dictionary = dict;
        this.triNumMatA = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.biNumMatA = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.holdOut = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.numMatB = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfWords()];
        this.numPi = new int[this.dictionary.getSizeOfTags()];
        this.unkHandle = unkHandle;
        logger.info("使用 " + GlobalParas.getUnkHandle(unkHandle));
    }

    /**
     * @param stream 指明特点语料路径的语料读取流
     */
    public TrigramParas(WordTagStream stream, int holdOutRatio,int unkHandle) throws IOException {
        this.dictionary = DictFactory.generateDict(stream);//一次扫描生成语料库对应的[映射词典]
        stream.openReadStream();
        this.triNumMatA = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.biNumMatA = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.holdOut = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.numMatB = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfWords()];
        this.numPi = new int[this.dictionary.getSizeOfTags()];
        this.initParas(stream, holdOutRatio);
        this.unkHandle = unkHandle;
        logger.info("使用 " + GlobalParas.getUnkHandle(unkHandle));
    }

    @Override
    protected void countMatA(String[] tags) {
        if (tags.length < 3) {
            logger.info("标注长度小于3，不能用于统计转移频数。");
            return;
        }
        //三元标注状态统计
        for (int i = 2; i < tags.length; i++) {
            this.triNumMatA[this.dictionary.getTagId(tags[i - 2])][this.dictionary.getTagId(tags[i - 1])][this.dictionary.getTagId(tags[i])]++;
        }
        //二元标注状态统计
        for (int i = 1; i < tags.length; i++) {
            this.biNumMatA[this.dictionary.getTagId(tags[i - 1])][this.dictionary.getTagId(tags[i])]++;
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
    public void addHoldOut(WordTag[] wts) {
        if (wts.length < 3) {
            logger.info("句子长度不够，不能添加留存频数。");
            return;
        }
        this.smoothFlag = true;
        this.dictionary.addIndex(wts);
        for (int i = 2; i < wts.length; i++) {
            this.holdOut[this.dictionary.getTagId(wts[i - 2].getTag())][this.dictionary.getTagId(wts[i - 1].getTag())][this.dictionary.getTagId(wts[i].getTag())]++;
        }
    }

    @Override
    protected void calcProbA() {

        int len = this.dictionary.getSizeOfTags();

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
                        this.triProbMatA[t_1][t_2][t_3] = 0.0;
                    }
                }
            }

        }
    }

    @Override
    protected void smoothMatA() {

        int len = this.dictionary.getSizeOfTags();

        this.smoothingMatA = new double[len][len][len];

        double lambd_count1 = 0.0;
        double lambd_count2 = 0.0;
        double lambd_count3 = 0.0;

        int N = 0;
        for (int dim1 = 0; dim1 < len; ++dim1) {
            for (int dim2 = 0; dim2 < len; ++dim2) {
                for (int dim3 = 0; dim3 < len; ++dim3) {
                    N += this.holdOut[dim1][dim2][dim3];
                }
            }
        }
        if (N == 0) {
            logger.severe("留存数据不存在,不能平滑概率。");
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

                    /*  稀疏语料中，t_2的出现概率大多数情况下要比t_2的条件概率大，对应的t_2的联合频数t_1_2要小；
                        少数情况下，t_2的条件概率比t_2的出现概率大，这时对应的t_2的联合频数t_1_2要大；
                        所以，虽然expression1大的情况多一些，但因为累加的联合频数偏小，所以最后对应系数并不会格外大
                        是否取等号，对系数的取值影响很大
                        因为是三元语法，留存数据不大时，数据非常稀疏
                        */
                    if (expression1 > expression2 && expression1 > expression3) {
                        lambd_count1 += t_1_2_3;
                    } else if (expression2 > expression3) {
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
        logger.info("系数：" + lambd1 + "-" + lambd2 + "-" + lambd3);

        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                for (int t_3 = 0; t_3 < len; ++t_3) {
                    this.smoothingMatA[t_1][t_2][t_3] = lambd1 * this.probPi[t_3] + lambd2 * this.biProbMatA[t_2][t_3] + lambd3 * this.triProbMatA[t_1][t_2][t_3];
                }
            }
        }
    }

    /**
     * 获得指定概率
     */
    @Override
    public double getProbPi(int indexOfTag) {
        return this.probPi[indexOfTag];
    }

    @Override
    public double getProbB(boolean smoothFlag, int indexOfTag, int indexOfWord) {
        return this.probMatB[indexOfTag][indexOfWord];
    }

    @Override
    public double getProbA(boolean smoothFlag, int... tagIndex) {
        /**
         * 一阶、二阶概念糅杂到了一起
         */
        if (tagIndex.length == 3) {
            if (smoothFlag) {
                if (this.smoothFlag) {
                    return this.smoothingMatA[tagIndex[0]][tagIndex[1]][tagIndex[2]];
                } else {
//                    logger.severe("未构造留存信息"+",返回未平滑概率替代。");
                    return this.triProbMatA[tagIndex[0]][tagIndex[1]][tagIndex[2]];
                }
            } else {
                return this.triProbMatA[tagIndex[0]][tagIndex[1]][tagIndex[2]];
            }
        } else if (tagIndex.length == 2) {
            return this.biProbMatA[tagIndex[0]][tagIndex[1]];
        } else {
            logger.warning("参数不合法。");
            System.exit(1);
        }
        return -1;
    }

    @Override
    public double unkZXF(String preWord, int currTag) {
        return 1.0;
    }

}
