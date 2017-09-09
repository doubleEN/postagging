package com.rui.util;

import com.rui.dictionary.DictFactory;
import com.rui.stream.WordTagStream;
import com.rui.wordtag.WordTag;

import java.io.IOException;

/**
 * 通用的全局方法。
 */
public class GlobalMethods {

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
