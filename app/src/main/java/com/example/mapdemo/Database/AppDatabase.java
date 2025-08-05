package com.example.mapdemo.Database;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {User.class,NoteEntity.class,CommentInfo.class}, version = 17,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract NoteDao noteDao();
    public abstract CommentDao commentDao();

}