package com.rui.stream;

import com.rui.ngram.AbstractWordTag;

import java.io.*;

/**
 *
 */
public abstract class WordTagStream {

    protected BufferedReader br;

    protected FileInputStream fis;

    protected InputStreamReader isr;

    protected String corpusPath;

    public abstract AbstractWordTag[] segSentence(String sentence);

    public void openReadStream() {
        try {
            this.fis = new FileInputStream(this.corpusPath);
            this.isr = new InputStreamReader(fis);
            this.br = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public AbstractWordTag[] readLine() {
        String line = null;
        try {
            line = this.br.readLine();
            if (line == null || line.trim().equals("")) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return this.segSentence(line.trim());
    }

    public void close() {
        try {

            this.br.close();
            this.isr.close();
            this.fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
