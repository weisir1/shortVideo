package com.example.libnetwork.cache;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    /**
     *
     * @param date 将date类型字段转为long类型传入数据库
     */
    @TypeConverter
    public static Long date2Long(Date date){
        return  date.getTime();
    }

    /**
     *
     * @param data 数据库返回的long类型时间转换为date类型
     */
    @TypeConverter
    public static Date long2Date(Long data){
        return new Date(data);
    }
}
