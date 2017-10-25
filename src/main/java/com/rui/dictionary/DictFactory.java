package com.rui.dictionary;


import com.rui.stream.WordTagStream;
import com.rui.wordtag.WordTag;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * 标注和词的[映射词典]，每个词或标注都对应一个id，id在各自集合内唯一。
 */
public class DictFactory implements Serializable {

    public DictFactory() {
    }

    /**
     * tag-->id
     */
    private Map<String, Integer> tagId = new HashMap<String, Integer>();
    /**
     * word-->id
     */
    private Map<String, Integer> wordId = new HashMap<String, Integer>();
    /**
     * id-->tag
     */
    private Map<Integer, String> tagDict = new HashMap<Integer, String>();
    /**
     * id-->word
     */
    private Map<Integer, String> wordDict = new HashMap<Integer, String>();

    /**
     * 标注集的大小
     */
    public int getSizeOfTags() {
        return this.tagId.size();
    }

    /**
     * 词的数量
     */
    public int getSizeOfWords() {
        return this.wordId.size();
    }

    /**
     * 标注的编号
     */
    public Integer getTagId(String tag) {
        return this.tagId.get(tag);
    }

    /**
     * 词的编号
     */
    public Integer getWordId(String word) {
        return this.wordId.get(word);

    }

    /**
     * 获得给定编号的标注
     */
    public String getTag(Integer tagId) {
        return this.tagDict.get(tagId);
    }

    /**
     * 获得给定编号的词
     */
    public String getWord(Integer wordId) {
        return this.wordDict.get(wordId);
    }

    /**
     * 给新的[词/标注]编号
     */
    public void addIndex(WordTag[] wts) {
        for (WordTag wt : wts) {
            String word = wt.getWord();
            String tag = wt.getTag();
            if (!this.tagId.containsKey(tag)) {
                this.tagId.put(tag, this.tagId.size());
                this.tagDict.put(this.tagDict.size(), tag);
            }
            if (!this.wordId.containsKey(word)) {
                this.wordId.put(word, this.wordId.size());
                this.wordDict.put(this.wordDict.size(), word);
            }

        }

    }

    public void addWord(String word) {
        if (!this.wordId.containsKey(word)) {
            this.wordId.put(word, this.wordId.size());
            this.wordDict.put(this.wordDict.size(), word);
        }
    }

    /**
     * 获得所有标注集的字符串数组
     */
    public String[] getTagSet() {
        Set<String> tagSet = this.tagId.keySet();
        return (String[]) tagSet.toArray(new String[0]);
    }

    /**
     * 由词的映射词典获得关于词的词典
     */
    public HashSet<String> getWordSet(){
        HashSet<String> wordSet = new HashSet<>();

        Set<Map.Entry<String,Integer>> entrySet=this.wordId.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            wordSet.add(entry.getKey());
        }
        return wordSet;
    }

    /**
     * 从语料库中加载得到[映射词典]
     * @param stream 指定特点语料库的读取流
     * @return [映射词典]
     */
    public static DictFactory generateDict(WordTagStream stream) throws IOException {
        WordTag[] wts;
        DictFactory dictFactory = new DictFactory();
        while ((wts = stream.readSentence()) != null) {
            dictFactory.addIndex(wts);
        }
        stream.close();
        return dictFactory;
    }

}