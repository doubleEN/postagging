package com.rui.validation;

import com.rui.evaluation.WordPOSMeasure;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 模型评估接口。
 */
public interface ModelScore {

    /**
     * 模型评估的执行方法
     */
    void toScore() throws Exception;

    /**
     * 返回模型的评分
     */
    WordPOSMeasure getScores();

}
