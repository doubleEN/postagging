package com.rui.dictionary;


import com.rui.ngram.WordTag;

import java.util.*;

/**
 * Handle the corpus to get two categories of dictionaries.
 * Id begins with "0".
 */
public class DictFactory {
    protected Map<String, Integer> tagId = new HashMap<String, Integer>();

    protected Map<String, Integer> wordId = new HashMap<String, Integer>();

    protected Map<Integer, String> tagDict = new HashMap<Integer, String>();

    protected Map<Integer, String> wordDict = new HashMap<Integer, String>();

    /**
     * Create two map from corpus stored by map.
     */

    public int getSizeOfTags(){
        return this.tagId.size();
    }
    public int getSizeOfWords(){
        return this.wordId.size();
    }

    public Integer getTagId(String tag) {
        return this.tagId.get(tag);
    }

    public Integer getWordId(String word) {
        return this.wordId.get(word);

    }

    public String getTag(Integer tagId) {
        return this.tagDict.get(tagId);

    }

    public String getWord(Integer wordId) {
        return this.wordDict.get(wordId);
    }

    public DictFactory() {
    }

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
}