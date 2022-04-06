package com.example.libnetwork.cache;

import android.renderscript.Sampler;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.Relation;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;

// 数据库中会根据此对象建立对应表
@Entity(tableName = "cache"
        //外键
//        foreignKeys = {@ForeignKey(entity = User.class, parentColumns = "id", childColumns = "key", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.SET_DEFAULT)}
        )
public class Cache implements Serializable {

//    @Ignore  //表示在创建数据库表时不会被映射成字段了
@PrimaryKey
@NotNull  public String key;


//@PrimaryKey(autoGenerate = true)   设置自增长
//    public String id;


    //因为每个接口返回的数据不同,转为二进制更方便
//    @ColumnInfo(name = "_data")
    public byte[] data;

    //    当cache字段被映射成表时,user中的字段都会被映射到cache表当中
//    @Embedded
//    public User user;

//    @Relation(entity = User.class,parentColumn = "id",entityColumn = )
//    public User mUser;


//  typeConverter类型转换集合 存有多个typeConverter
//    @TypeConverters(value = DateConverter.class)
//    public Date mDate;   //当date字段存入数据库时就会被转换成long类型
}
