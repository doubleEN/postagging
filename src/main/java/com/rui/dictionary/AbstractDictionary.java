package com.rui.dictionary;

import com.rui.ngram.WordTag;

import java.util.HashMap;
import java.util.Map;

/**
 * Handle the corpus to get some dictionaries.
 */
public abstract class AbstractDictionary {

    protected Map<String, Integer> tagId = new HashMap<String, Integer>();

    protected Map<String, Integer> wordId = new HashMap<String, Integer>();

    protected Map<Integer, String> tagDict = new HashMap<Integer, String>();

    protected Map<Integer, String> wordDict = new HashMap<Integer, String>();

    /**
     * Create two map from corpus stored by map.
     */
    public abstract void addIndex(WordTag[]wts);

    public Map<String, Integer> getTagId() {
        return tagId;
    }

    public Map<String, Integer> getWordId() {
        return wordId;
    }

    public Map<Integer, String> getTagDict() {
        return tagDict;
    }

    public Map<Integer, String> getWordDict() {
        return wordDict;
    }

}
