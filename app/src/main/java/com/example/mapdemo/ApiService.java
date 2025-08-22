package com.example.mapdemo;

import com.example.mapdemo.Database.CommentInfo;
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
import retrofit2.http.Query;

public interface ApiService {
    @POST("notes/save")
    Call<NoteEntity> insert(@Body NoteEntity noteEntity);
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

    @POST("Comment")
    Call<CommentInfo> createComment(@Body CommentInfo comment);

    // 根据ID获取评论
    @GET("Comment/{id}")
    Call<CommentInfo> getCommentById(@Path("id") long id);

    // 获取指定帖子的所有评论
    @GET("Comment/post/{postId}")
    Call<List<CommentInfo>> getCommentsByPostId(@Path("postId") long postId);

    // 获取指定父评论的子评论
    @GET("Comment/parent/{parentId}")
    Call<List<CommentInfo>> getChildComments(@Path("parentId") long parentId);

    // 更新评论
    @PUT("Comment/{id}")
    Call<CommentInfo> updateComment(
            @Path("id") long id,
            @Body CommentInfo comment
    );
    // 删除评论
    @DELETE("Comment/{id}")
    Call<Void> deleteComment(@Path("id") long id);
    // 获取帖子的评论数量
    @GET("Comment/count/{postId}")
    Call<Integer> getCommentCount(@Path("postId") long postId);
    // 获取未同步的评论
    @GET("Comment/unsynced")
    Call<List<CommentInfo>> getUnsyncedComments();

    // 标记评论为已同步
    @PUT("Comment/sync/{id}")
    Call<Void> markCommentAsSynced(@Path("id") long id);
    // 批量同步评论
    @POST("Comment/sync/batch")
    Call<List<CommentInfo>> syncCommentsBatch(@Body List<CommentInfo> comments);
    @GET("Comment/all")
    Call<List<CommentInfo>> getAllComments();
    @Multipart
    @POST("user/upload/avatar")
    Call<ImageUploadResponse> uploadAvatar(
            @Part("userId") RequestBody userId,
            @Part MultipartBody.Part file
    );

}