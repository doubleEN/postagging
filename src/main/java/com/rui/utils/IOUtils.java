package com.rui.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Some common methods.
 */
public class IOUtils {

    public static void writeCorpusToFile(Map<Integer, String> corpus, String outPath) {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(outPath);
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);
            Iterator<Map.Entry<Integer, String>> iterEntry = corpus.entrySet().iterator();
            int flag = 0;
            while (iterEntry.hasNext()) {
                bw.write(iterEntry.next().getValue().trim());
                bw.newLine();
                ++flag;
                if (flag == 500) {
                    bw.flush();
                    flag = 0;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
                osw.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<Integer, String> readCorpusFromFile(String corpusPath) {
        Map<Integer, String> corpus = new HashMap<Integer, String>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(corpusPath);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);

            String line;
            Integer id = 1;
            while ((line = br.readLine()) != null) {
                if (!line.trim().equals("")){
                    corpus.put(id, line.trim());
                    ++id;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return corpus;
    }
}
