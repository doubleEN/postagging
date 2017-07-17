package com.rui.stream;

import com.rui.ngram.WordTag;

import java.io.*;

/**
 *
 */
public abstract class WordTagStream {

    protected BufferedReader br;

    /**
     * 分割一个句子得到代表多个[Word tag]的WordTag类型的抽象方法。
     * @param sentence 一行独立的句子。
     * @return AbstractWordTag[]数组。
     */
    public abstract WordTag[] segSentence(String sentence);

    /**
     * 打开流操作
     */
    protected void openReadStream(String corpusPath) {
        try {
            //这里没有显示的关闭fis和isr会有什么影响
            FileInputStream fis = new FileInputStream(corpusPath);
            InputStreamReader isr = new InputStreamReader(fis);
            this.br = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     *  迭代读取每行句子并处理为 AbstractWordTag[]返回的方法
     * @return AbstractWordTag[]数组
     */
    public WordTag[] readSentence() {//????
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
}
