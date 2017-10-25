package com.rui.validation;

import com.rui.dictionary.DictFactory;
import com.rui.evaluation.WordPOSMeasure;
import com.rui.model.HMM;
import com.rui.model.HMM1st;
import com.rui.model.HMM2nd;
import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.parameter.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

public class CrossValidation implements ModelScore {

    public static void main(String[] args) throws Exception {
        ModelScore modelScore = new CrossValidation(new PeopleDailyWordTagStream("/home/jx_m/桌面/PoS/corpus/199801_format.txt", "utf-8"), 8, NGram.TriGram);
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
    private int fold;

    /**
     * 映射词典
     */
    private HashSet<String> wordDict;

    /**
     * @param wordTagStream 包含特点语料路径的语料读取流
     * @param fold          交叉验证折数
     * @param nGram         语法参数
     */
    public CrossValidation(WordTagStream wordTagStream, int fold, NGram nGram) {
        this.stream = wordTagStream;
        this.fold = fold;
        this.nGram = nGram;
        this.measure = new WordPOSMeasure();
    }

    @Override
    public void toScore() throws IOException {
        for (int i = 0; i < this.fold; ++i) {
            this.tagger = this.getTagger(i);
            this.stream.openReadStream();
            this.measure.mergeInto(this.estimate(i));
            this.stream.openReadStream();
        }
    }

    /**
     * 获得指定训练集上训练的隐藏状态标注器
     *
     * @param taggerNO 代表训练集的编号
     * @return 隐藏状态标注器
     */
    private Tagger getTagger(int taggerNO) throws IOException {
        WordTag[] wts = null;
        Tagger tagger = null;
        Random random = new Random(11);

        AbstractParas paras = null;
        DictFactory dictFactory=new DictFactory();
        HMM hmm = null;

        int num = 0;
        while ((wts = this.stream.readSentence()) != null) {
            if (num % this.fold != taggerNO) {
                //语料不能直接放入内存
                dictFactory.addIndex(wts);
            }
            ++num;
        }

        if (this.nGram == NGram.BiGram) {
            paras = new BigramParas(dictFactory);
            hmm = new HMM1st(paras);
        } else if (this.nGram == NGram.TriGram) {
            paras = new TrigramParas(dictFactory);
            hmm = new HMM2nd(paras);
        }
        num = 0;
        this.stream.openReadStream();
        while ((wts = this.stream.readSentence()) != null) {
            if (num % this.fold != taggerNO) {
                //语料不能直接放入内存
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
        tagger = new Tagger(hmm);
        this.wordDict = dictFactory.getWordSet();
        return tagger;
    }

    /**
     * 指定验证集，进行一次交叉验证，并返回评估值
     *
     * @return 一折验证的评分
     */
    private WordPOSMeasure estimate(int taggerNo) throws IOException {
        WordPOSMeasure posMeasure = new WordPOSMeasure(this.wordDict);
        WordTag[] wts = null;
        int num = 0;
        String[] predictTags = null;


        while ((wts = this.stream.readSentence()) != null) {
            if (num % this.fold == taggerNo) {
                //验证语料不能直接放入内存
                this.getTagOfValidation(wts);
                String[]words=this.unknownSentence.trim().split("\\s+");
                WordTag[] predict = this.tagger.tag(this.unknownSentence);
                predictTags = new String[predict.length];
                for (int j = 0; j < predict.length; ++j) {
                    predictTags[j] = predict[j].getTag();
                }
                posMeasure.updateScores(words, this.expectedTags,predictTags);
            }
            ++num;
        }
        return posMeasure;
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

    /**
     * @return 返回此次验证评分
     */
    public WordPOSMeasure getScores() {
        return this.measure;
    }

}
