package com.rui.stream;

import com.rui.wordtag.WordTag;

import java.io.*;

/**
 *  读取文本语料库的接口
 */
public abstract class WordTagStream {

    /**
     * 语料库路径
     */
    protected String corpusPath;

    /**
     * 辅助读取语料库的输入流
     */
    protected BufferedReader br;

    /**
     * 分割一个句子得到一个WordTag数组
     */
    public abstract WordTag[] segSentence(String sentence);

    /**
     * 打开指定的语料库
     * @param corpusPath 指定的语料库路径
     */
    public void openReadStream(String corpusPath) {
        try {
            FileInputStream fis = new FileInputStream(corpusPath);
            InputStreamReader isr = new InputStreamReader(fis);
            this.br = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     *打开构造器中传入的的语料库
     */
    public void openReadStream() {
        try {
            FileInputStream fis = new FileInputStream(this.corpusPath);
            InputStreamReader isr = new InputStreamReader(fis);
            this.br = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 输入流迭代读取每行语料的方法
     */
    public WordTag[] readSentence() {
        String line = null;
        try {
            line = this.br.readLine();
            if (line == null) {
                return null;
            }
            if (line.trim().equals("")) {
                return this.readSentence();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return this.segSentence(line.trim());
    }

    /**
     * 关闭流的方法
     */
    public void close() {
        try {

            this.br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 返回构造器传入的语料的路径
     */
    public String getCorpusPath() {
        return corpusPath;
    }
}
