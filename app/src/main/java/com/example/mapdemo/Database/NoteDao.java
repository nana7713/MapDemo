package com.example.mapdemo.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("select*from note")
    List<NoteEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(NoteEntity... noteEntities);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(NoteEntity noteEntity);

    @Update
    public void updateNote(NoteEntity... noteEntities);

    @Query("select * from note where user_name like :name")
    List<NoteEntity> findByUserName(String name);
    @Query("select * from note where id like :id")
    NoteEntity findById(long id);
    @Query("select * from note where user_id like :uid")
    List<NoteEntity> findByUserID(int uid);
    @Query("select * from note where poi_id like :poi_id")
    List<NoteEntity> findByPoiID(String poi_id);

    @Query("SELECT * FROM note")
    public NoteEntity[] loadAllNotes();

    @Delete
    void delete(NoteEntity noteEntity);
    @Query("delete from note where id=:noteID")
    void deleteById(long noteID);

}
