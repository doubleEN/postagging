package com.rui.wordtag;

/**
 * word/tag类。
 */
public class WordTag {

    /**
     * 单词字符串
     */
    private String word;

    /**
     * 标注字符串
     */
    private String tag;

    /**
     * @param word 合法单词字符串
     * @param tag 合法标注字符串
     */
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

    /**
     * @return WordTag的格式化字符串
     */
    @Override
    public String toString() {
        return this.word + "/" + this.tag;
    }

}
