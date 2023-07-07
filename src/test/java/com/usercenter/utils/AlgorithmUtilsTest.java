package com.usercenter.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class AlgorithmUtilsTest {

    @Test
    public void test() {
        String word1 = "小明很聪明";
        String word2 = "小明不聪明";
        String word3 = "小明非常聪明";
        System.out.println(AlgorithmUtils.minDistance(word1, word2));
        System.out.println(AlgorithmUtils.minDistance(word1, word3));
        System.out.println(AlgorithmUtils.minDistance(word2, word3));
    }

    @Test
    public void testCompareTags() {
        List<String> tagList1 = Arrays.asList("Java", "大一", "男");
        List<String> tagList2 = Arrays.asList("Java", "大二", "男");
        List<String> tagList3 = Arrays.asList("Python", "大二", "女");
        System.out.println(AlgorithmUtils.minDistance(tagList1, tagList2));
        System.out.println(AlgorithmUtils.minDistance(tagList1, tagList3));
    }

}