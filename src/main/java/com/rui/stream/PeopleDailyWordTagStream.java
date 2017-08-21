package com.rui.stream;

import com.rui.wordtag.WordTag;
import static com.rui.util.GlobalParas.logger;

/**
 * 迭代读取人民日报标注语料库的输入流
 */
public class PeopleDailyWordTagStream extends WordTagStream {

    public PeopleDailyWordTagStream(String corpusPath) {
        this.corpusPath=corpusPath;
        this.openReadStream(corpusPath);
    }

    /**
     * 用空白符分割得到多个[wordtag]，用[/]分割[wordtag]的到word和tag。
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
