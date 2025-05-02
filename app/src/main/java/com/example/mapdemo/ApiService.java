package com.example.mapdemo;

import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("notes/save")
    Call<Void> insert(@Body NoteEntity noteEntity);

    @PUT("notes/update")
    Call<Void> updateNote(@Body NoteEntity noteEntity);
    @PUT("user/updateUsers")
    Call<Void> updateUsers(@Body User... user);
    @POST("user")
    Call<User> insertUser(@Body User user);
    @GET("user/{id}")
    Call<User> getUserById(@Path ("id")int id);
    @GET("user/getAllUsers")
    Call<List<User>> getAllUsers();
    @GET("notes/user/{user_id}")
    Call<List<NoteEntity>>getNoteByUserID(@Path("user_id")int userId);
    @GET("notes")
    Call<List<NoteEntity>> getAllNotes();
    @GET("notes/{id}")
    Call<List<NoteEntity>> getNoteByID(@Path("id")int id);
    @GET("notes//poi/{poi_id}")
    Call<List<NoteEntity>> getNoteByPoi(@Path("poi_id")int poiID);

}