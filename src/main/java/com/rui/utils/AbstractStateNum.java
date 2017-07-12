package com.rui.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handle the corpus to get some dictionaries.
 */
public abstract class AbstractStateNum {

    protected String corpusPath;

    protected Map<Integer, String> corpus;

    protected Map<String, Integer> tagId = new HashMap<String, Integer>();

    protected Map<String, Integer> wordId = new HashMap<String, Integer>();

    protected Map<Integer, String> tagDict = new HashMap<Integer, String>();

    protected Map<Integer, String> wordDict = new HashMap<Integer, String>();

    /**
     * Create two map from corpus stored by map.
     */
    public abstract void createIndex();

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
