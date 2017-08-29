package com.rui.wordtag;

/**
 * word/tag类。
 */
public class WordTag {

    private String word;
    private String tag;

    public WordTag(String word, String tag) {
        this.word = word;
        this.tag = tag;
    }

    /**
     * @return word/tag中word的字符串形式
     */
    public String getWord() {
        return word;
    }

    /**
     * @return word/tag中tag的字符串形式
     */
    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return this.word + "/" + this.tag;
    }

}
