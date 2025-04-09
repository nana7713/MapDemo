package com.example.mapdemo;

import android.widget.Button;
import android.widget.Toast;

import com.example.mapdemo.Database.NoteEntity;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {
    @POST("notes/insert")
    Call<Void> insert(@Body NoteEntity noteEntity);

    @PUT("notes/update")
    Call<Void> update(@Body NoteEntity noteEntity);
}