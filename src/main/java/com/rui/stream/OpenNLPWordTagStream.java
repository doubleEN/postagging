package com.rui.stream;

import com.rui.wordtag.WordTag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.rui.util.GlobalParas.logger;

/**
 * 读取openNLP语料处理格式的人民日报词性标注语料库
 */
public class OpenNLPWordTagStream extends WordTagStream {

    public OpenNLPWordTagStream() {
    }

    /**
     * @param corpusPath 语料路径
     * @param encoding 字符编码方式
     */
    public OpenNLPWordTagStream(String corpusPath, String encoding) throws UnsupportedEncodingException, ClassNotFoundException, FileNotFoundException {
        this.corpusPath = corpusPath;
        this.encoding = encoding;
        this.openReadStream();
    }

    /**
     * openNLP语料处理格式的人民日报语料库分割的主要方式
     * @param sentence 人民日报语料库中的一行句子
     * @return 一行句子对应的[word/tag]数组
     */
    @Override
    public WordTag[] segSentence(String sentence) throws IOException{
        String[] wordTags = sentence.split("\\s+");
        WordTag[] wt = new WordTag[wordTags.length-1];

        //原始语料每句话以固特殊格式的日期开头，去掉开头，避免对无意义且占据大量空间的word进行概率计算
        for (int i = 0; i < wordTags.length-1; i++) {
            String[] wordAndTag = wordTags[i+1].trim().split("_");
            if (wordAndTag.length != 2) {
                logger.severe(WordTag.class.getName() + "不合法。");
                throw new IOException("不能有效分割单词标注序列："+wordTags[i].toString()+"，默认分割符为_");
            }
            wt[i] = new WordTag(wordAndTag[0], wordAndTag[1]);
        }
        return wt;
    }
}
