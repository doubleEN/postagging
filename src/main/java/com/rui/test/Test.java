package com.rui.test;

import java.util.Arrays;

/**
 *
 */
public class Test {
    public static void main(String[] args) {
        int[][] arr1 = new int[][]{new int[]{1,3},new int[]{4,4}};

        int[][] arr2 = Arrays.copyOf(arr1, arr1.length+2);//增加的部分初始化为默认值

        System.out.println();
        System.out.println(arr2.length);
    }
}
