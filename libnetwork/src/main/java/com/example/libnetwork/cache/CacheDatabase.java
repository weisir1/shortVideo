package com.example.libnetwork.cache;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.libcommon.global.AppGlobals;
// 第一个参数表类型, 第三个参数,会生成一个json文件 包含数据库升级或在创建表的时候所执行的所有sql操作,字段及字段描述
@Database(entities = {Cache.class},version = 1,exportSchema = true)
public abstract class CacheDatabase extends RoomDatabase {
    static {
//       创建内存数据库
//       但是这种数据库的数据只存在与内存中,当进程被杀死时,数据随之丢失
//       Room.inMemoryDatabaseBuilder()
        database = Room.databaseBuilder(AppGlobals.getsApplication(), CacheDatabase.class, "wesir_cache")
                //是否允许在主线程进行查询 默认false
                .allowMainThreadQueries()
                //数据库创建和打开后的回调
//               .addCallback();
                //设置查询的线程
//              .setQueryExecutor()
//              room的日志模式
//                .setJournalMode()
//                数据库升级异常之后的回滚
//                .fallbackToDestructiveMigration()
//                 数据库升级异常之后根据指定版本进行回滚
//                .fallbackToDestructiveMigrationFrom()
//                数据库升级入口 如果
                /*如果当前版本和最新版本之间缺少迁移项，Room 将清除数据库并重新创建，
                因此即使您在 2 个版本之间没有更改，您仍然应该向构建器提供迁移对象*/
//                .addMigrations(CacheDatabase.sMigration)
                .build();
    }
    public static  CacheDatabase get(){
        return database;
    }

    public  abstract  CacheDao getCache();

    static Migration sMigration = new Migration(1, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //修改表名
            database.execSQL("alter table teacher rename to student");
            database.execSQL("alter table teacher add column teacher_age INTEGER not null default 0");

        }
    };
    private static CacheDatabase database;
}
