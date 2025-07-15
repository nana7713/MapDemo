package com.example.mapdemo;

import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;

import java.io.File;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @POST("notes/save")
    Call<Void> insert(@Body NoteEntity noteEntity);
    @Multipart
    @POST("notes/upload/image")
    Call<ImageUploadResponse> uploadImage(
            @Part("noteId") RequestBody noteId,
            @Part MultipartBody.Part file
    );

    @PUT("notes/{id}")
    Call<Void> updateNote(@Path ("id")long id,@Body NoteEntity noteEntity);
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
    Call<NoteEntity> getNoteByID(@Path("id")long id);
    @GET("notes/poi/{poi_id}")
    Call<List<NoteEntity>> getNoteByPoi(@Path("poi_id")String poi_id);
    @DELETE("notes/deletenote/{id}")
    Call<Void> deleteNote(@Path("id") Long id);

}