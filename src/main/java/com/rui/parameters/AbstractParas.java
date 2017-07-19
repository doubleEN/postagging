package com.rui.parameters;

import com.rui.dictionary.DictFactory;
import com.rui.ngram.WordTag;
import com.rui.stream.WordTagStream;

/**
 *
 */
public abstract class AbstractParas {

    protected DictFactory dictionary;

    protected boolean calcFlag=false;

    //从语料库中累计参数
    public void addCorpus(String corpusPath) {
        WordTagStream wordTagStream = this.openStream(corpusPath);
        WordTag[] wts;
        while ((wts = wordTagStream.readSentence()) != null) {
            this.addCorpus(wts);
        }
        wordTagStream.close();
        //有新的语料，可重新计算概率参数
        this.calcFlag=true;
    }

    //从wordTag数组中累计数组
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

        this.calcFlag=true;
    }

    //打开特定的语料库
    protected abstract WordTagStream openStream(String corpusPath);

    //统计参数
    protected abstract void countMatA(String[] tags);

    protected abstract void countMatB(String[] words, String[] tags);

    protected abstract void countPi(String[] tags);

    //扩展参数数组
    protected abstract void reBuildA();

    protected abstract void reBuildB();

    protected abstract void reBuildPi();

    /*
        计算三个概率参数
     */
    public void calcProbs(boolean smoothFlag) {
        if (!this.calcFlag){
            System.err.println("未添加语料库或未加入新的语料库。");
            return;
        }
        //这里最后计算A的概率，因为A的平滑需要pi的概率
        this.calcProbB();
        this.calcProbPi();
        this.calcProbA();
        if (smoothFlag) {
            this.smoothMatA();
        }

    }

    //转移矩阵A
    protected abstract void calcProbA();

    //混淆矩阵B
    protected abstract void calcProbB();

    //初始概率向量pi
    protected abstract void calcProbPi();

    //平滑的转移概率A
    protected abstract void smoothMatA();

    public int getSizeOfTags() {
        return this.dictionary.getSizeOfTags();
    }

    public abstract double getPi(int indexOfTag);

    public abstract double getProbB(int indexOfTag,int indexOfWord);

    public abstract double getProbA(int preTag,int nextTag);

    public abstract double getProbSmoothA(int preTag,int nextTag);

    public abstract String getTagOnId(int tagId);

    public abstract int getWordId(String word);
}
