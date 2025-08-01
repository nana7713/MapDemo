package com.example.mapdemo.Database;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Query("select*from users")
    List<User> getAll();
    @Update
    public void updateUser(User... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void  insertAll(User... users);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void  insert(User user);

    @Query("select * from users where uid like :id")
    User findById(int id);
    @Query("select * from users where account like :name")
    List<User> findByName(String name);

    @Query("SELECT * FROM users")
    public User[] loadAllUsers();

    @Delete
    void delete(User user);
    @Query("SELECT * FROM users WHERE account = :account")
    User getUserByAccount(String account);
}
