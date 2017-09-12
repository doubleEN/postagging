package com.rui.model;

public class HMM1st extends HMM{

    public int[][] decode(String sentence, int k) {
        return new int[0][];
    }

    protected void forward(String sentence, int ranking) {

    }

    protected int[] backTrack(int ranking, int... lastIndexs) {
        return new int[0];
    }
}
