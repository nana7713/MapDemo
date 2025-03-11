package com.example.mapdemo.Database;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {User.class,NoteEntity.class}, version = 9)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract NoteDao noteDao();

    // 定义迁移策略
    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {//定义一个处理版本升级的Migration对象
//通过匿名内部类，在实例化的过程中重写migrate方法在数据库表中新增经纬度两行
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {//当前数据库实例不能为空
            // 添加新字段经纬度
            database.execSQL("ALTER TABLE note ADD COLUMN latitude REAL");
            database.execSQL("ALTER TABLE note ADD COLUMN longitude REAL");
        }
    };

    // 在构建数据库时添加迁移策略
    public static AppDatabase getDatabase(Context context) {
        return Room.databaseBuilder(//构建器方法，用于创建RoomDatabase实例
                        context,
                        AppDatabase.class,//指定数据库类
                        "mapdemo_database"//指定数据库名称
                )
                .addMigrations(MIGRATION_8_9) // 添加迁移策略
                .build();
    }
}