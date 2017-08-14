package com.rui.stream;

import com.rui.wordtag.WordTag;

import java.io.*;

/**
 *  迭代读取语料输入流接口
 */
public abstract class WordTagStream {

    protected String corpusPath;

    /**
     * 辅助输入流
     */
    protected BufferedReader br;

    /**
     * 分割一个句子得到一个WordTag数组
     */
    public abstract WordTag[] segSentence(String sentence);

    /**
     * 打开流操作
     */
    public void openReadStream(String corpusPath) {
        try {
            //这里没有显式的关闭fis和isr会有什么影响
            FileInputStream fis = new FileInputStream(corpusPath);
            InputStreamReader isr = new InputStreamReader(fis);
            this.br = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void openReadStream() {
        try {
            //这里没有显式的关闭fis和isr会有什么影响
            FileInputStream fis = new FileInputStream(this.corpusPath);
            InputStreamReader isr = new InputStreamReader(fis);
            this.br = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 输入流迭代读取行的方法
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

    public String getCorpusPath() {
        return corpusPath;
    }
}
