package com.rui.validation.validationResult;

import com.rui.evaluation.WordPOSMeasure;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;
import com.rui.util.GlobalParas;
import com.rui.validation.CrossValidation;
import com.rui.validation.ModelScore;
import com.rui.validation.NGram;

import static com.rui.util.GlobalParas.logger;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 穷举调参。
 */
public class GridSearch {
    public static void main(String[] args) throws Exception {
        int[] holdoutRatio = new int[1000];
        for (int i=1;i<1001;++i) {
            holdoutRatio[i-1]=i*2;
        }
        GridSearch gridSearch = new GridSearch(new PeopleDailyWordTagStream("/home/jx_m/桌面/PoS/corpus/199801_format.txt", "utf-8"), 10, new NGram[]{NGram.BiGram}, holdoutRatio, new int[]{GlobalParas.UNK_MAXPROB},"/home/jx_m/IdeaProjects/tags/src/main/java/com/rui/validation/validationResult/result_1.csv",false);
        gridSearch.score();
    }

    /**
     * 读入特定形式的语料
     */
    private WordTagStream stream;

    /**
     * 交叉验证折数
     */
    private int fold;

    /**
     * 标明使用的n-gram
     */
    private NGram[] nGrams;

    /**
     * 留存数据比例
     */
    private int[] holdOutRatios;

    /**
     * 未登录词处理方式
     */
    private int[] unkHandles;

    /**
     * 结果输出流
     */
    private BufferedWriter bw;

    /**
     * 输出是否覆盖原文件
     */
    private boolean append;

    public GridSearch(WordTagStream wordTagStream, int fold, NGram[] nGrams, int[] holdOutRatios, int[] unkHandles, String resultPath, boolean append)throws Exception {
        this.fold = fold;
        this.stream = wordTagStream;
        this.nGrams = nGrams;
        this.holdOutRatios = holdOutRatios;
        this.unkHandles = unkHandles;
        this.append=append;
        this.bw = new BufferedWriter(new FileWriter(resultPath,append));
    }

    private void setHead()throws IOException{
        if (this.append) {
            return;
        }
        bw.write("PrecisionScore,PrecisionScoreIV,PrecisionScoreOOV,PrecisionScore,N-Gram,CV_Fold,HoldOutRatio,UNK_Handle");
        bw.newLine();
        bw.flush();
    }

    /**
     * 网格搜索，线程重构
     */
    public void score() throws IOException {
        int count=0;
        int sum=this.nGrams.length*this.holdOutRatios.length*this.unkHandles.length;
        this.setHead();
        for (int i = 0; i < nGrams.length; ++i) {
            for (int j = 0; j < holdOutRatios.length; ++j) {
                for (int k = 0; k < this.unkHandles.length; ++k) {
                    ModelScore modelScore = new CrossValidation(this.stream, this.fold, this.nGrams[i], this.holdOutRatios[j], this.unkHandles[k]);
                    try {
                        modelScore.toScore();
                    } catch (Exception e) {
                        bw.write(null + "," + null + "," + null + "," + null + "," + null + "," + null + "," + null + "," + null);
                        bw.newLine();
                        continue;
                    }
                    WordPOSMeasure measure = modelScore.getScores();
                    bw.write(measure.getPrecisionScore() + "," + measure.getPrecisionScoreIV() + "," + measure.getPrecisionScoreOOV() + "," + measure.getSentenceAccuracy()+",");
                    bw.write(this.nGrams[i].toString() + ","+this.fold+"," + this.holdOutRatios[j] + "," + GlobalParas.getUnkHandle(this.unkHandles[k]));
                    bw.newLine();
                    bw.flush();
                    ++count;
                    logger.info("测试进度： "+count+"/"+sum);
                }
            }
        }
    }
}
