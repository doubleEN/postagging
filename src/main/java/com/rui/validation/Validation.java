package com.rui.validation;

import com.rui.dictionary.DictFactory;
import com.rui.evaluation.*;
import com.rui.model.*;
import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.parameter.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;
import com.sun.org.apache.xpath.internal.operations.Mod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 一次验证评估，按比例划分语料。
 */
public class Validation implements ModelScore {

    public static void main(String[] args) throws Exception {
        ModelScore modelScore = new Validation(new PeopleDailyWordTagStream("/home/jx_m/桌面/PoS/corpus/199801_format.txt", "utf-8"), 0.1, NGram.BiGram);
        modelScore.toScore();
        System.out.println(modelScore.getScores().toString());
    }

    /**
     * 标明使用的n-gram
     */
    private NGram nGram;

    /**
     * 生成的标注器
     */
    private Tagger tagger;

    /**
     * 读入特定形式的语料
     */
    private WordTagStream stream;

    /**
     * 每一折交叉验证中，生成的验证语料
     */
    private String unknownSentence;

    /**
     * 验证语料的正确标注
     */
    private String[] expectedTags;

    /**
     * 评估器
     */
    private WordPOSMeasure measure;

    /**
     * 折数
     */
    private double ratio;

    /**
     * 词典
     */
    private HashSet<String> dict;

    public Validation(WordTagStream wordTagStream, double ratio, NGram nGram) {
        this.stream = wordTagStream;
        this.ratio = ratio;
        this.nGram = nGram;
    }

    @Override
    public void toScore() throws IOException {
        this.getTagger();
        this.stream.openReadStream();
        this.estimate();
    }

    /**
     * 通过验证集获得隐藏状态标注器
     */
    private void getTagger() throws IOException {
        DictFactory dictFactory=new DictFactory();

        WordTag[] wts = null;
        Random random = new Random(11);
        int fold = (int) (1 / this.ratio);
        int num = 0;

        //第一次扫描
        while ((wts = this.stream.readSentence()) != null) {
            //在1000中取指定比例样本
            if (num % fold != 0) {
                dictFactory.addIndex(wts);
            }
            ++num;
        }

        AbstractParas paras = null;
        HMM hmm = null;
        if (this.nGram == NGram.BiGram) {
            paras = new BigramParas(dictFactory);
            hmm = new HMM1st(paras);
        } else if (this.nGram == NGram.TriGram) {
            paras = new TrigramParas(dictFactory);
            hmm = new HMM2nd(paras);
        }
        this.stream.openReadStream();
        num = 0;
        //会初始化计数矩阵
        while ((wts = this.stream.readSentence()) != null) {
            //在1000中取指定比例样本
            if (num % fold != 0) {
                int randNum = random.nextInt(1000);
                if (randNum == 1) {
                    paras.addHoldOut(wts);
                } else {
                    paras.addCorpus(wts);
                }
            }
            ++num;
        }
        paras.calcProbs();
        this.measure = new WordPOSMeasure(dictFactory.getWordSet());
        this.tagger = new Tagger(hmm);
    }

    /**
     * 指定验证集，进行一次交叉验证，并返回评估值
     *
     * @return 验证评分
     */
    private void estimate() throws IOException {
        WordTag[] wts = null;

        String[] predictTags = null;
        int fold = (int) (1 / this.ratio);
        int num = 0;

        while ((wts = this.stream.readSentence()) != null) {
            if (num % fold == 0) {
                this.getTagOfValidation(wts);
                WordTag[] predict = this.tagger.tag(this.unknownSentence);
                String[] words = new String[predict.length];
                predictTags = new String[predict.length];
                for (int j = 0; j < predict.length; ++j) {
                    predictTags[j] = predict[j].getTag();
                    words[j] = predict[j].getWord();
                }
                this.measure.updateScores(words, this.expectedTags, predictTags);
            }
            ++num;
        }
    }


    /**
     * 分割验证集观察状态和隐藏状态
     *
     * @param wts 带标注的句子
     */
    private void getTagOfValidation(WordTag[] wts) {
        String sentence = "";
        this.expectedTags = new String[wts.length];
        for (int j = 0; j < wts.length; ++j) {
            this.expectedTags[j] = wts[j].getTag();
            sentence = sentence + wts[j].getWord() + " ";
        }
        this.unknownSentence = sentence.trim();
    }

    @Override
    public WordPOSMeasure getScores() {
        return measure;
    }
}
