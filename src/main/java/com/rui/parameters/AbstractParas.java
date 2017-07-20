package com.rui.parameters;

import com.rui.dictionary.DictFactory;
import com.rui.ngram.WordTag;
import com.rui.stream.WordTagStream;

/**
 * 统计并计算HMM参数的接口
 */
public abstract class AbstractParas {

    //word与word的编号词典
    protected DictFactory dictionary;

    //判断当前计数参数是否变化，无变化则不允许重复计算概率参数
    protected boolean calcFlag = false;

    //添加语料库并累计参数
    public void addCorpus(String corpusPath) {
        WordTagStream wordTagStream = this.openStream(corpusPath);
        WordTag[] wts;
        while ((wts = wordTagStream.readSentence()) != null) {
            this.addCorpus(wts);
        }
        wordTagStream.close();
        //有新的语料，可重新计算概率参数
        this.calcFlag = true;
    }

//   ?????句子重载

    //从wordTag数组中累计参数
    public void addCorpus(WordTag[] wts) {
        dictionary.addIndex(wts);
        String[] words = new String[wts.length];
        String[] tags = new String[wts.length];
        for (int i = 0; i < wts.length; ++i) {
            words[i] = wts[i].getWord();
            tags[i] = wts[i].getTag();
        }
        this.countMatA(tags);
        this.countMatB(words, tags);
        this.countPi(tags);
        //有新的语料，可重新计算概率参数

        this.calcFlag = true;
    }

    //打开特定的语料库
    protected abstract WordTagStream openStream(String corpusPath);

    //统计[转移状态频数]
    protected abstract void countMatA(String[] tags);

    //统计[混淆状态频数]
    protected abstract void countMatB(String[] words, String[] tags);

    //统计[初始状态频数]
    protected abstract void countPi(String[] tags);

    //扩展[转移状态矩阵]
    protected abstract void reBuildA();

    //扩展[混淆状态矩阵]
    protected abstract void reBuildB();

    //扩展[初始状态向量]
    protected abstract void reBuildPi();

    /*
        以下为由计数计算概率的方法。
     */
    //计算概率参数的[模板方法]
    public void calcProbs() {
        if (!this.calcFlag) {
            System.err.println("未添加语料库或未加入新的语料库。");
            return;
        }
        //这里最后计算A的概率，因为A的平滑需要pi的概率
        this.calcProbB();
        this.calcProbPi();
        this.calcProbA();
        this.smoothMatA();

    }

    //计算[转移概率矩阵]，未平滑
    protected abstract void calcProbA();

    //计算[混淆概率矩阵]
    protected abstract void calcProbB();

    //计算[初始概率向量]
    protected abstract void calcProbPi();

    //[平滑]的转移概率矩阵
    protected abstract void smoothMatA();

    /*
        以下，提供访问参数的方法与接口
     */
    //获得标注集大小
    public int getSizeOfTags() {
        return this.dictionary.getSizeOfTags();
    }

    //获得指定标注的初始概率
    public abstract double getProbPi(int indexOfTag);

    //获得指定[tag-->word]的混淆概率
    public abstract double getProbB(int indexOfTag, int indexOfWord);

    //获得指定[tag_i-->tag_i+1]的转移概率
    public abstract double getProbA(int preTag, int nextTag);

    //获得指定[tag_i-->tag_i+1]的平滑后的转移概率
    public abstract double getProbSmoothA(int preTag, int nextTag);

    //根据tagId获取tag的字符串形式
    public abstract String getTagOnId(int tagId);

    //根据word的字符串形式获取对应的wordId
    public abstract int getWordId(String word);

    //根据tag的字符串形式获取对应的tagId
    public abstract int getTagId(String tag);

    //获得整个标注集的字符串形式
    public abstract String[] getTagSet();
}
