package com.rui.parameter;

import com.rui.dictionary.DictFactory;
import com.rui.wordtag.WordTag;
import com.rui.stream.WordTagStream;

import java.io.Serializable;
import java.util.Random;

/**
 * 统计并计算HMM参数的接口
 */
public abstract class AbstractParas implements Serializable{

    //读取特点语料库的流
    protected WordTagStream wordTagStream;

    //word与tag的映射词典
    protected DictFactory dictionary;

    //判断当前计数参数是否变化，无变化则不允许重复计算概率参数
    protected boolean calcFlag = false;

    //初始化[语料库]，并计算概率参数的[模板方法]
    protected void initParas() {
        this.wordTagStream.openReadStream();

        WordTag[] wts;
        Random generator = new Random(21);
        while ((wts = this.wordTagStream.readSentence()) != null) {
            //在统计参数以前，给新语料增加映射关系
            dictionary.addIndex(wts);

            //按[1:3]换分[留存:训练集]
            int randNum = generator.nextInt(4);
            if (randNum == 1) {
                this.addHoldOut(wts);
            } else {
                this.addWordTags(wts);
            }
        }
//        this.wordTagStream.close();

        //初始添加了语料库，可计算概率参数
        this.calcFlag = true;

        //计算概率
        this.calcProbs();
    }

    //添加新语料。另外提供特点流处理这个字符串形式的句子
    public void addCorpus(String sentence,WordTagStream stream) {
        WordTag[] wts = stream.segSentence(sentence);
        dictionary.addIndex(wts);
        this.addWordTags(wts);
        this.calcFlag = true;
    }

    //添加新语料
    public void addCorpus(String sentence) {
        WordTag[] wts = this.wordTagStream.segSentence(sentence);
        dictionary.addIndex(wts);
        this.addWordTags(wts);
        this.calcFlag = true;
    }

    //添加新语料
    public void addCorpus(WordTag[] wts) {
        dictionary.addIndex(wts);
        this.addWordTags(wts);
        this.calcFlag = true;
    }

    //添加新语料
    public void addWordTags(WordTag[] wts) {
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
    }

    //统计[转移状态频数]
    protected abstract void countMatA(String[] tags);

    //统计[混淆状态频数]
    protected abstract void countMatB(String[] words, String[] tags);

    //平滑[混淆状态频数]
    protected abstract void smoothMatB();

    //统计[初始状态频数]
    protected abstract void countPi(String[] tags);

    //扩展[转移状态矩阵]
    protected abstract void reBuildA();

    //扩展[混淆状态矩阵]
    protected abstract void reBuildB();

    //扩展[初始状态向量]
    protected abstract void reBuildPi();

    //划分[留存数据]
    public abstract void addHoldOut(WordTag[] wts);

    //扩展[留存状态矩阵]
    protected abstract void expandHoldOut();

    protected abstract void ensureLenOfTag();

    /*
        以下为由计数计算概率的方法。
     */
    //计算概率参数的[模板方法]
    public void calcProbs() {
        if (!this.calcFlag) {
            System.err.println("未添加初始语料库或未加入新的语料,不能计算概率。");
            return;
        }

        //在计算概率以前，保证训练集，留存和映射词典的编号长度一致
        this.ensureLenOfTag();
        //平滑混淆状态频数
        this.smoothMatB();

        //这里最后计算A的概率，因为A的平滑需要pi的概率
        this.calcProbB();
        this.calcProbPi();
        this.calcProbA();
        this.smoothMatA();
        this.calcFlag = false;

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
        以下，提供访问概率参数的方法与接口
     */
    //获得指定标注的初始概率
    public abstract double getProbPi(int indexOfTag);

    //获得指定[tag-->word]的混淆概率
    public abstract double getProbB(int indexOfTag, int indexOfWord);

    //获得指定[tag_i-->tag_i+1]的转移概率
    public abstract double getProbA(int... tagIndex);

    //获得指定[tag_i-->tag_i+1]的平滑后的转移概率
    public abstract double getProbSmoothA(int... tagIndex);

    //获得词典
    public DictFactory getDictionary(){
        return this.dictionary;
    }

}
