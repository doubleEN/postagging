package com.rui.evaluation;

import com.rui.dictionary.DictFactory;

/**
 * 评估接口。
 */
public interface Estimator {
    /**
     * 对预测结果进行度量
     * @param dict 映射词典
     * @param unknownSentences 未知句子
     * @param predictedTags 预测序列
     * @param expectedTags 期望序列
     */
    void eval(DictFactory dict, String unknownSentences, String[] predictedTags, String[] expectedTags);

    /**
     * 格式化打印输出预测序列和期望序列
     * @param unknownSentence 未知句子
     * @param predictedTags 预测序列
     * @param expectedTags 期望序列
     */
    void printTagging(String unknownSentence, String[] predictedTags, String[] expectedTags);

    /**
     * 返回全局度量结果
     */
    double getResult();

    /**
     * 重置评估参数
     */
    void reset();
}
