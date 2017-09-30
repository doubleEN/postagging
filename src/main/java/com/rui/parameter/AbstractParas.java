package com.rui.parameter;

import com.rui.dictionary.DictFactory;
import com.rui.wordtag.WordTag;
import com.rui.stream.WordTagStream;

import static com.rui.util.GlobalParas.logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/**
 * 统计并计算HMM参数的接口
 */
public abstract class AbstractParas implements Serializable {

    /**
     * word与tag的[映射词典]
     */
    protected DictFactory dictionary;

    /**
     * 判断当前计数参数是否变化，无变化则不允许重复计算概率参数
     */
    protected boolean calcFlag = false;

    /**
     * 统计语料库规模，并生成映射词典
     * @param stream 特定语料的输入流
     */
    protected void generateDict(WordTagStream stream)throws IOException{
        WordTag[]wts;
        while ((wts=stream.readSentence())!=null) {
            this.dictionary.addIndex(wts);
        }
        stream.close();
    }

    /**
     * 初始化[语料库]，并计算概率参数的[模板方法]
     * @param stream 读取特点语料的输入流
     */
    protected void initParas(WordTagStream stream) throws IOException{
        WordTag[] wts;
        Random generator = new Random(21);

        while ((wts = stream.readSentence()) != null) {
            //按[1:3]换分[留存:训练集]
            int randNum = generator.nextInt(4);
            if (randNum == 1) {
                this.addHoldOut(wts);
            } else {
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
     * @param sentence 添加的句子形式的语料
     * @param stream 能够处理sentence的具体流
     */
    public void addCorpus(String sentence, WordTagStream stream) throws IOException{
        WordTag[] wts = stream.segSentence(sentence);
        dictionary.addIndex(wts);
        this.addWordTags(wts);
        this.calcFlag = true;
    }

    /**
     * 添加新语料
     * @param wts WordTag[]形式的新语料
     */
    public void addCorpus(WordTag[] wts) {
        dictionary.addIndex(wts);
        this.addWordTags(wts);
        this.calcFlag = true;
    }

    /**
     * 所有添加语料方法的底层方法
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
     * @param tags 有序的标注序列
     */
    protected abstract void countMatA(String[] tags);

    /**
     * 统计[混淆状态频数]
     * @param words 有序的单词序列
     * @param tags 有序的标注序列
     */
    protected abstract void countMatB(String[] words, String[] tags);

    /**
     * 平滑[混淆状态频数]
     */
    protected abstract void smoothMatB();

    /**
     * 统计[初始状态频数]
     * @param tags 标注集
     */
    protected abstract void countPi(String[] tags);

    /**
     * 扩展[转移状态矩阵]
     */
    protected abstract void reBuildA();

    /**
     * 扩展[混淆状态矩阵]
     */
    protected abstract void reBuildB();

    /**
     * 扩展[初始状态向量]
     */
    protected abstract void reBuildPi();

    /**
     * 划分[留存数据]
     * @param wts WordTag[]形式的留存语料
     */
    public abstract void addHoldOut(WordTag[] wts);

    /**
     * 扩展[留存状态矩阵]
     */
    protected abstract void expandHoldOut();

    /**
     * 由频数参数计算概率参数前，确定标注集大小是否合法
     */
    protected abstract void ensureLenOfTag();

    /**
     * 计算概率参数的[模板方法]
     */
    public void calcProbs() {

        if (!this.calcFlag) {
            logger.severe("未添加初始语料库或未加入新的语料,不能计算转移概率。");
        }

        //在计算概率以前，保证训练集，留存和映射词典的编号长度一致
        this.ensureLenOfTag();
        //+1平滑混淆状态频数
        this.smoothMatB();
        this.calcProbB();

        this.calcProbPi();

        this.calcProbA();
        this.smoothMatA();
        this.calcFlag = false;
    }

    /**
     * 计算[转移概率矩阵]，未平滑
     */
    protected abstract void calcProbA();

    /**
     * 计算[混淆概率矩阵]
     */
    protected abstract void calcProbB();

    /**
     * 计算[初始概率向量]
     */
    protected abstract void calcProbPi();

    /**
     * [平滑]的转移概率矩阵
     */
    protected abstract void smoothMatA();

    /**
     * 获得指定标注的初始概率
     * @param indexOfTag 标注的id
     * @return 标注的初始概率
     */
    public abstract double getProbPi(int indexOfTag);

    /**
     * 获得指定[tag-->word]的混淆概率
     * @param indexOfTag 标注的id
     * @param indexOfWord 单词的id
     * @return 标注到词的发射概率
     */
    public abstract double getProbB(int indexOfTag, int indexOfWord);

    /**
     * 获得指定[tag_i-->tag_i+1]的转移概率
     * @param tagIndexs 多个标注的id
     * @return 指定n-gram下的标注转移概率
     */
    public abstract double getProbA(int... tagIndexs);

    /**
     * 获得指定[tag_i-->tag_i+1]的平滑后的转移概率
     * @param tagIndexs 多个标注的id
     * @return 指定n-gram下的平滑后标注转移概率
     */
    public abstract double getProbSmoothA(int... tagIndexs);

    /**
     * @return 返回[映射词典]
     */
    public DictFactory getDictionary() {
        return this.dictionary;
    }

}
