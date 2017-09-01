package com.rui.stream;

import com.rui.wordtag.WordTag;

import java.io.FileNotFoundException;
import java.io.IOException;

import static com.rui.util.GlobalParas.logger;

/**
 * 读取人民日报词性标注语料库的输入流
 */
public class PeopleDailyWordTagStream extends WordTagStream {

    public PeopleDailyWordTagStream() {
    }

    /**
     * @param corpusPath 语料路径
     */
    public PeopleDailyWordTagStream(String corpusPath) throws ClassNotFoundException,FileNotFoundException{
        this.corpusPath=corpusPath;
        this.openReadStream(corpusPath);
    }

    /**
     * 人民日报语料库分割的主要方式
     * @param sentence 人民日报语料库中的一行句子
     * @return 一行句子对应的[word/tag]数组
     */
    @Override
    public WordTag[] segSentence(String sentence) {
        String[] wordTags = sentence.split("\\s+");
        WordTag[] wt = new WordTag[wordTags.length];

        for (int i = 0; i < wordTags.length; i++) {

            String[] wordAndTag = wordTags[i].trim().split("/");
            if (wordAndTag.length != 2) {
                logger.severe(WordTag.class.getName()+"不合法。");
                System.exit(1);
            }
            wt[i] = new WordTag(wordAndTag[0], wordAndTag[1]);
        }
        return wt;
    }
}
