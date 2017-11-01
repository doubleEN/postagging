package com.rui.app;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class CorpusSplit {

    private static void usage()
    {
        System.out.println(CorpusSplit.class.getName() + " -split <corpusFile> [percent] [encoding]" + " | -sample <corpusFile> trainCount testCount [encoding]");
    }

    /**
     * 将词性标注语料分隔成训练语料和测试语料
     *
     * 从训练语料中取一部分作为测试语料
     *
     * @param source
     *            标注语料
     * @param percent
     *            作为测试语料的百分比，剩余用作训练语料
     *
     */
    public static void split(String source, int percent, String encoding) throws IOException
    {
        String trainFile = "train-pos.txt";
        String testFile = "test-pos.txt";

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(source), encoding));
        String sentence = null;
        PrintWriter train = new PrintWriter(new OutputStreamWriter(new FileOutputStream(trainFile), encoding));
        PrintWriter test = new PrintWriter(new OutputStreamWriter(new FileOutputStream(testFile), encoding));
        int n = 0;
        while ((sentence = in.readLine()) != null)
        {
            n++;
            if (n % percent == 0)
                test.append(sentence + "\n");
            else
                train.append(sentence + "\n");
        }

        train.close();
        test.close();
        in.close();
    }

    /**
     * 采样生成训练和测试语料
     *
     * @param source 源训练语料
     * @param trainCount 新训练语料采样句子数
     * @param testCount 测试语料采样句子数
     * @param encoding
     * @throws IOException
     */
    public static void sample(String source, int trainCount, int testCount, String encoding) throws IOException
    {
        String trainFile = "train-pos.txt";
        String testFile = "test-pos.txt";

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(source), encoding));
        String sentence = null;
        PrintWriter train = new PrintWriter(new OutputStreamWriter(new FileOutputStream(trainFile), encoding));
        PrintWriter test = new PrintWriter(new OutputStreamWriter(new FileOutputStream(testFile), encoding));
        int n = 0;
        ArrayList<String> sentences = new ArrayList<String>();
        while ((sentence = in.readLine()) != null)
        {
            if (sentence.length() > 0)
            {
                sentences.add(sentence);
                n++;
            }
        }

        in.close();

        Random r = new Random();
        boolean[] occupied = new boolean[n];
        for (int i = 0; i < n; i++)
            occupied[i] = false;

        int cnt = 0;
        while (cnt < trainCount)
        {
            int idx = r.nextInt(n);
            if (!occupied[idx])
            {
                train.println(sentences.get(idx));
                occupied[idx] = true;

                cnt++;
            }
        }

        train.close();

        cnt = 0;
        while (cnt < testCount)
        {
            int idx = r.nextInt(n);
            if (!occupied[idx])
            {
                test.println(sentences.get(idx));
                occupied[idx] = true;

                cnt++;
            }
        }

        test.close();

    }

    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            usage();
            return;
        }

        if (args[0].equals("-split"))
        {
            String source = args[1];
            int percent = 10;
            String encoding = "GBK";

            if (args.length > 2)
                percent = Integer.parseInt(args[2]);

            if (args.length > 3)
                encoding = args[3];

            split(source, percent, encoding);
        }
        else if(args[0].equals("-sample"))
        {
            String source = args[1];
            String encoding = "GBK";
            int trainCount = Integer.parseInt(args[2]);
            int testCount = Integer.parseInt(args[3]);

            if (args.length > 4)
                encoding = args[4];

            sample(source, trainCount, testCount, encoding);
        }
    }

}
