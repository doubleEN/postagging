package com.rui.statistic;

import com.rui.dictionary.DictFactory;
import com.rui.ngram.WordTag;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;

import java.util.Arrays;
import java.util.Map;

/**
 *
 */
public class BigramParasCount extends AbstractParasCount {


    public static void main(String[] args) {
        AbstractParasCount stats = new BigramParasCount();

        WordTag[] wordTags = new WordTag[]{
                new WordTag("我","n"),
                new WordTag("爱","v"),
                new WordTag("nlp","fn")
        };
        System.out.println(Arrays.toString(wordTags));
        stats.countParas("/home/mjx/桌面/PoS/test/testSet2.txt");

        int[][][] as = stats.getNumMatA();
        int[][] bs = stats.getNumMatB();
        int[] pi = stats.getNumPi();

        System.out.println("A:");
        for (int[] a : as[0]) {
            System.out.println(Arrays.toString(a));
        }
        System.out.println("B:");
        for (int[] b : bs) {
            System.out.println(Arrays.toString(b));
        }

        System.out.println("PI:"+Arrays.toString(pi));

        System.out.println(stats.getDictionary().getTagDict().size());
        System.out.println(stats.getDictionary().getTagId().size());
        System.out.println(stats.getDictionary().getWordDict().size());
        System.out.println(stats.getDictionary().getWordId().size());

        System.out.println(stats.getNumMatA()[0].length+":"+stats.getNumMatA()[0][0].length);
        System.out.println(stats.getNumMatB().length+":"+stats.getNumMatB()[0].length);
        System.out.println(stats.getNumPi().length);
        //PI:[184764, 236810, 74829, 34473, 173047, 20680, 41370, 24244]
    }

    public BigramParasCount() {
        //At the beginning,the size of array is ensured according to the corpus.
        this.dictionary=new DictFactory();
        this.numMatA = new int[1][1][1];//the size of tag set is 44.
        this.numMatB = new int[1][1];//the size of word set is 55310.
        this.numPi = new int[1];
    }

    public BigramParasCount(int tagNum,int wordNum) {
        //At the beginning,the size of array is ensured according to the corpus.
        this.dictionary=new DictFactory();
        this.numMatA = new int[1][tagNum][tagNum];//the size of tag set is 44.
        this.numMatB = new int[tagNum][wordNum];//the size of word set is 55310.
        this.numPi = new int[tagNum];
    }

    @Override
    protected WordTagStream openStream(String corpusPath) {
        return new PeopleDailyWordTagStream(corpusPath);
    }

    //para A
    @Override
    protected void countMatA(String[] tags) {
        if (this.dictionary.getTagId().size() > this.numMatA[0].length) {
            this.reBuildA();
        }
        Map<String, Integer> tagId = this.dictionary.getTagId();
        //后减处理，利于循环结束和单个标注的处理
        for (int i = 1; i < tags.length; i++) {
            this.numMatA[0][tagId.get(tags[i - 1])][tagId.get(tags[i])]++;
        }

    }

    //para B
    @Override
    protected void countMatB(String[] words, String[] tags) {
        if (words.length != tags.length) {
            System.err.println("词组，标注长度不匹配。");
        }
        if (this.dictionary.getTagId().size() > this.numMatB.length || this.dictionary.getWordId().size() > this.numMatB[0].length) {
            this.reBuildB();
        }
        Map<String, Integer> tagId = this.dictionary.getTagId();
        Map<String, Integer> wordId = this.dictionary.getWordId();

        for (int i = 0; i < words.length; i++) {
            this.numMatB[tagId.get(tags[i])][wordId.get(words[i])]++;
        }
    }
    //para pi
    @Override
    protected void countPi(String[] tags) {
        if (this.dictionary.getTagId().size() > this.numPi.length) {
            this.reBuildPi();
        }
        Map<String, Integer> tagId = this.dictionary.getTagId();
        for (String tag : tags) {
            this.numPi[tagId.get(tag)]++;
        }

    }
}

