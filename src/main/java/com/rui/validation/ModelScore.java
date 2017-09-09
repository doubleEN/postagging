package com.rui.validation;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 模型评估接口。
 */
public interface ModelScore {

    /**
     * 模型评估的执行方法
     */
    void toScore() throws FileNotFoundException,IOException;

    /**
     * 返回模型的评分
     */
    double[] getScores();

}
