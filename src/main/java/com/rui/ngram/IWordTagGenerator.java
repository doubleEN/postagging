package com.rui.ngram;

/**
 *
 */
public interface IWordTagGenerator {

    /**
     * 将 [word tag] 分割成word与tag的 抽象方法
     */
    public abstract WordTag separateWordTag(String wordTag);

}
