package com.example.libnetwork.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

//@Dao : 真正操作数据库的注解
@Dao
public interface CacheDao {
//  onConflict:  插入数据时发生冲突后的操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save(Cache cache);

    @Query("select * from cache where `key`=:key")
    Cache getCache(String key);

    @Delete
    int delete(Cache cache);

    //冲突时策略
    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(Cache cache);
}
