package com.rui.validation;

/**
 * 模型评估接口。
 */
public interface ModelScore {

    /**
     * 模型评估的执行方法
     */
    void toScore();

    /**
     * 返回模型的评分
     */
    double getScore();

}
