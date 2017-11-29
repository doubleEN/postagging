package com.rui.parameter;

import com.rui.dictionary.DictFactory;
import com.rui.util.GlobalParas;
import com.rui.wordtag.WordTag;
import com.rui.stream.WordTagStream;

import static com.rui.util.GlobalParas.logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

/**
 * 统计并计算HMM参数的接口
 */
public abstract class AbstractParas implements Serializable {

    /**
     * 初始状态计数矩阵
     */
    protected int[] numPi;

    /**
     * 初始状态概率
     */
    protected double[] probPi;

    /**
     * 状态发射计数矩阵
     * 每一行为同一个隐藏状态下，发射到的可能的同一时刻观察状态的计数[t_i]-->[w_i]
     */
    protected int[][] numMatB;

    /**
     * 状态发射概率矩阵
     * 每一行为同一个隐藏转移状态下，发射到可能的同一时刻观察状态的概率,即 P([t_i]-->[w_i])
     */
    protected double[][] probMatB;

    /**
     * word与tag的[映射词典]
     */
    protected DictFactory dictionary;

    /**
     * 判断当前计数参数是否变化，无变化则不允许重复计算概率参数
     */
    protected boolean calcFlag = false;

    /**
     * 是否使用留存平滑
     */
    protected boolean smoothFlag=false;

    /**
     * 未登录词处理方式
     */
    protected int unkHandle;

    /**
     * 初始化[语料库]，并计算概率参数的[模板方法]
     *
     * @param stream 读取特点语料的输入流
     */
    public void initParas(WordTagStream stream,int holdOutRatio) throws IOException {
        WordTag[] wts;

        if (holdOutRatio > 0) {
            Random generator = new Random(21);
            while ((wts = stream.readSentence()) != null) {
                int randNum = generator.nextInt(holdOutRatio);
                if (randNum == 1) {
                    this.addHoldOut(wts);
                } else {
                    this.addWordTags(wts);
                }
            }
        } else {
            while ((wts = stream.readSentence()) != null) {
                this.addWordTags(wts);
            }
        }
        //初始添加了语料库，可计算概率参数
        this.calcFlag = true;

        //计算概率
        this.calcProbs();
    }

    /**
     * 添加新语料，另外提供特点流处理这个字符串形式的句子
     *
     * @param sentence 添加的句子形式的语料
     * @param stream   能够处理sentence的具体流
     */
    public void addCorpus(String sentence, WordTagStream stream) throws IOException {
        WordTag[] wts = stream.segSentence(sentence);
        dictionary.addIndex(wts);
        this.addWordTags(wts);
        this.calcFlag = true;
    }

    /**
     * 添加新语料
     *
     * @param wts WordTag[]形式的新语料
     */
    public void addCorpus(WordTag[] wts) {
        dictionary.addIndex(wts);
        this.addWordTags(wts);
        this.calcFlag = true;
    }

    /**
     * 所有添加语料方法的底层方法
     *
     * @param wts WordTag[]形式的新语料
     */
    private void addWordTags(WordTag[] wts) {
        String[] words = new String[wts.length];
        String[] tags = new String[wts.length];
        for (int i = 0; i < wts.length; ++i) {
            words[i] = wts[i].getWord();
            tags[i] = wts[i].getTag();
        }
        this.countMatA(tags);
        this.countMatB(words, tags);
        this.countPi(tags);
    }

    /**
     * 统计[转移状态频数]
     *
     * @param tags 有序的标注序列
     */
    protected abstract void countMatA(String[] tags);

    /**
     * 统计[混淆状态频数]
     *
     * @param words 有序的单词序列
     * @param tags  有序的标注序列
     */
    protected void countMatB(String[] words, String[] tags) {
        if (words.length != tags.length) {
            logger.warning("词组，标注长度不匹配。");//Level.info
            return;
        }
        for (int i = 0; i < words.length; i++) {
            this.numMatB[this.dictionary.getTagId(tags[i])][this.dictionary.getWordId(words[i])]++;
        }
    }

    /**
     * 平滑[混淆状态频数]
     */
    protected void smoothMatB() {
        for (int i = 0; i < this.numMatB.length; ++i) {
            for (int j = 0; j < this.numMatB[0].length; ++j) {
                ++this.numMatB[i][j];
            }
        }
    }

    /**
     * 统计[初始状态频数]
     *
     * @param tags 标注集
     */
    protected void countPi(String[] tags) {
        for (String tag : tags) {
            this.numPi[this.dictionary.getTagId(tag)]++;
        }
    }


    /**
     * 划分[留存数据]
     *
     * @param wts WordTag[]形式的留存语料
     */
    public abstract void addHoldOut(WordTag[] wts);

    /**
     * 计算概率参数的[模板方法]
     */
    public void calcProbs() {

        if (!this.calcFlag) {
            logger.severe("未添加初始语料库或未加入新的语料,不能计算转移概率。");
        }

        //+1平滑混淆状态频数
        this.smoothMatB();
        this.calcProbB();

        this.calcProbPi();

        this.calcProbA();
        if (this.smoothFlag) {
            this.smoothMatA();
        }
        this.calcFlag = false;
    }

    /**
     * 计算[转移概率矩阵]，未平滑
     */
    protected abstract void calcProbA();

    /**
     * 计算[混淆概率矩阵]
     */
    protected void calcProbB() {
        int rowSize = this.dictionary.getSizeOfTags();
        int colSize = this.dictionary.getSizeOfWords();

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
                    probMatB[row][col] = 0.0;
                }
            }
        }

    }

    /**
     * 计算[初始概率向量]
     */
    protected void calcProbPi() {
        int vectorSize = this.dictionary.getSizeOfTags();

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

    /**
     * [平滑]的转移概率矩阵
     */
    protected abstract void smoothMatA();

    /**
     * 获得指定标注的初始概率
     *
     * @param indexOfTag 标注的id
     * @return 标注的初始概率
     */
    public double getProbPi(int indexOfTag) {
        return this.probPi[indexOfTag];
    }

    /**
     * 获得指定[tag-->word]的混淆概率
     *
     * @param indexOfTag  标注的id
     * @param indexOfWord 单词的id
     * @return 标注到词的发射概率
     */
    public double getProbB(int indexOfTag, int indexOfWord) {
        return this.probMatB[indexOfTag][indexOfWord];
    }


    /**
     * 获得指定[tag_i-->tag_i+1]的转移概率
     *
     * @param tagIndexs 多个标注的id
     * @return 指定n-gram下的标注转移概率
     */
    public abstract double getProbA(boolean smoothFlag, int... tagIndexs);

    /**
     * @return 返回[映射词典]
     */
    public DictFactory getDictionary() {
        return this.dictionary;
    }

    /**
     * 未登录词概率
     */
    public double getUnkProb(int currTag,String... preWord){
        if (GlobalParas.UNK_INITPROB == unkHandle) {
            return this.unkInitProb(currTag);
        } else if (GlobalParas.UNK_MAXPROB == this.unkHandle) {
            return this.unkMaxProb();
        } else if (GlobalParas.UNK_ZXF == this.unkHandle) {
            return unkZXF(currTag,preWord);
        } else {
            throw new IllegalArgumentException("未提供有效未登录词处理参数。");
        }
    }

    /**
     * 用隐藏状态初始概率作为未登录词每种隐藏状态概率
     */
    private double unkInitProb(int currTag) {
        return this.probPi[currTag];
    }

    /**
     * 拉普拉斯处理未登录比概率
     */
    private double unkMaxProb() {
        return 1.0;
    }
    /**
     * 张孝飞未登录词处理
     */
    protected abstract double unkZXF( int currTag,String... preWord);

}
