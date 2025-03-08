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
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加新字段（允许为空）
            database.execSQL("ALTER TABLE note ADD COLUMN latitude REAL");
            database.execSQL("ALTER TABLE note ADD COLUMN longitude REAL");
        }
    };

    // 在构建数据库时添加迁移策略
    public static AppDatabase getDatabase(Context context) {
        return Room.databaseBuilder(
                        context,
                        AppDatabase.class,
                        "your_database_name"
                )
                .addMigrations(MIGRATION_1_2) // 添加迁移策略
                .build();
    }
}