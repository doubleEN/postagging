package com.rui.POSTagger;

import com.rui.evaluation.Estimator;
import com.rui.evaluation.PreciseIV;
import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.model.SecondOrderHMM;
import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.parameter.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;
import com.rui.tagger.Tagger;
import com.rui.validation.CrossValidation;
import com.rui.validation.ModelScore;
import com.rui.validation.NGram;
import com.rui.validation.Validation;
import com.rui.wordtag.WordTag;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

/**
 * 汉语词性标注工具类
 */
public class POSTaggerFactory {

    public static void main(String[] args) {
//
//        AbstractParas paras = new BigramParas(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt"), 44, 50000);
//        HMM hmm = new FirstOrderHMM(paras);
//        hmm.writeHMM("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/BiGram.bin");
//
//        AbstractParas paras2 = new TrigramParas(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt"), 44, 50000);
//        HMM hmm2 = new SecondOrderHMM(paras);
//        hmm.writeHMM("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/TriGram.bin");
//        WordTag[] wts2 = POSTaggerFactory.tag2Gram("学习 自然 语言 处理 ， 实现 台湾 统一 。");
//
//        System.out.println(Arrays.toString(wts2));
        double score=POSTaggerFactory.scoreOnValidation(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt"),0.01,NGram.TriGram,new PreciseIV());
        System.out.println(score);

    }

    public static double scoreOnValidation(WordTagStream stream, double ratio, NGram nGram, Estimator estimator) {
        ModelScore crossValidation = new Validation(stream, ratio, nGram, estimator);
        crossValidation.toScore();
        return crossValidation.getScore();
    }

    public static double crossValidation(WordTagStream stream, int fold, NGram nGram, Estimator estimator) {
        ModelScore crossValidation = new CrossValidation(stream, fold, nGram, estimator);
        crossValidation.toScore();
        return crossValidation.getScore();
    }

    //指定语料生成标注器
    public static void writeHMM(WordTagStream stream, NGram nGram, String writePath) {

        AbstractParas paras = null;
        HMM hmm = null;
        if (nGram == NGram.BiGram) {
            paras = new BigramParas(stream);
            hmm = new FirstOrderHMM(paras);
        } else if (nGram == NGram.TriGram) {
            paras = new TrigramParas(stream);
            hmm = new SecondOrderHMM(paras);
        }

        hmm.writeHMM(writePath);
    }

    //指定语料生成标注器
    public static Tagger buildTagger(WordTagStream stream, NGram nGram) {
        Tagger tagger = null;

        AbstractParas paras = null;
        HMM hmm = null;
        if (nGram == NGram.BiGram) {
            paras = new BigramParas(stream);
            hmm = new FirstOrderHMM(paras);
            tagger = new Tagger(hmm);
        } else if (nGram == NGram.TriGram) {
            paras = new TrigramParas(stream);
            hmm = new SecondOrderHMM(paras);
            tagger = new Tagger(hmm);
        }

        return tagger;
    }

    //从指定模型中生成标注器
    public static Tagger buildTagger(String HMMPath) {
        return new Tagger(HMMPath);
    }

    //2-gram标注
    public static WordTag[] tag2Gram(String sentence) {

        Properties pro = new Properties();
        Tagger tagger = null;
        InputStreamReader propertiesPath = null;
        try {
            propertiesPath = new InputStreamReader(new FileInputStream("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/tag.properties"), "UTF-8");
            pro.load(propertiesPath);
            String BiGram = (String) pro.get("BiGram");
            tagger = new Tagger(BiGram);

            propertiesPath.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                propertiesPath.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tagger.tag(sentence);
    }

    //3-gram标注
    public static WordTag[] tag3Gram(String sentence) {
        Properties pro = new Properties();
        Tagger tagger = null;
        InputStreamReader propertiesPath = null;
        try {
            propertiesPath = new InputStreamReader(new FileInputStream("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/tag.properties"), "UTF-8");
            pro.load(propertiesPath);
            String TriGram = (String) pro.get("TriGram");
            tagger = new Tagger(TriGram);

            propertiesPath.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                propertiesPath.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tagger.tag(sentence);
    }
}
