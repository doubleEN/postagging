package com.rui.ngram;

/**
 *
 */
public class PeopleDailyNewsWordTagGenerator implements IWordTagGenerator {

    public PeopleDailyNewsWordTagGenerator() {
    }

    /**
     * 将 [word tag] 分割成word与tag的 抽象方法
     * 以“/”连接Word与tag的形式
     */
    @Override
    public WordTag separateWordTag(String wordTag) {
        String[] wordAndTag = wordTag.split("/");
        if (wordAndTag.length != 2) {
            System.err.println("word-tag 长度不为2");
        }
        return new WordTag(wordAndTag[0].trim(), wordAndTag[1].trim());
    }
}
