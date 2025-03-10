package com.example.mapdemo.Database;


import androidx.room.Database;

import androidx.room.RoomDatabase;


@Database(entities = {User.class,NoteEntity.class}, version = 9)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract NoteDao noteDao();

}