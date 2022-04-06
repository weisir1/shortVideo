package com.example.shortvideo.utils;

public class StringConvert {
    public static String convertFeedUgc(int count){
        if (count < 10000){
            return String.valueOf(count);
        }else{
            return  count/10000 +"万";
        }
    }
}
