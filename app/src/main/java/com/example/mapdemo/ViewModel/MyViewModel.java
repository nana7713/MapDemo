package com.example.mapdemo.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mapdemo.ApiService;
import com.example.mapdemo.Database.AppDatabase;
import com.example.mapdemo.Database.CommentDao;
import com.example.mapdemo.Database.CommentInfo;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.RetrofitClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Handler;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyViewModel extends androidx.lifecycle.ViewModel {
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<NoteEntity>> userNotesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<NoteEntity>> allNotesLiveData = new MutableLiveData<>();
    private final MutableLiveData<NoteEntity> noteLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<NoteEntity>> poiNoteLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> allUsersLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CommentInfo>> commentLiveData = new MutableLiveData<>();
    private final MutableLiveData<CommentInfo> singleCommentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> commentCountLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CommentInfo>> unsyncedCommentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncStatusLiveData = new MutableLiveData<>();


    public LiveData<List<NoteEntity>> getNotesByUserID() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getNoteByUserID(MapApp.getUserID()).enqueue(new Callback<List<NoteEntity>>() {
            @Override
            public void onResponse(Call<List<NoteEntity>> call, Response<List<NoteEntity>> response) {
                userNotesLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<NoteEntity>> call, Throwable t) {
                userNotesLiveData.setValue(null);
            }
        });
        return userNotesLiveData;
    }
    public LiveData<List<NoteEntity>> getNotesByPoi(String poiID) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getNoteByPoi(poiID).enqueue(new Callback<List<NoteEntity>>() {
            @Override
            public void onResponse(Call<List<NoteEntity>> call, Response<List<NoteEntity>> response) {
                poiNoteLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<NoteEntity>> call, Throwable t) {
                poiNoteLiveData.setValue(null);
            }
        });
        return poiNoteLiveData;
    }
    public LiveData<User> getUserByID() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getUserById(MapApp.getUserID()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User>call, Response<User> response) {
                userLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                userLiveData.setValue(null);
            }
        });
        return userLiveData;
    }
    public LiveData<List<User>> getAllUsers() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>>call, Response<List<User>> response) {
                allUsersLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                allUsersLiveData.setValue(null);
            }
        });
        return allUsersLiveData;
    }
    public  LiveData<List<NoteEntity>> getAllNotes(){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getAllNotes().enqueue(new Callback<List<NoteEntity>>() {
            @Override
            public void onResponse(Call<List<NoteEntity>> call, Response<List<NoteEntity>> response) {
                allNotesLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<NoteEntity>> call, Throwable t) {
                allNotesLiveData.setValue(null);

            }
        });
        return allNotesLiveData;
    }
    public  LiveData<NoteEntity> getNoteByID(long id){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getNoteByID(id).enqueue(new Callback<NoteEntity>() {
            @Override
            public void onResponse(Call<NoteEntity> call, Response<NoteEntity> response) {
                noteLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<NoteEntity> call, Throwable t) {
                noteLiveData.setValue(null);

            }
        });
        return noteLiveData;
    }
    public LiveData<List<CommentInfo>> getCommentsByPostId(long postId){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCommentsByPostId(postId).enqueue(new Callback<List<CommentInfo>>(){
            @Override
            public void onResponse(Call<List<CommentInfo>> call, Response<List<CommentInfo>> response) {
                if (response.isSuccessful()) {
                    commentLiveData.setValue(response.body());
                } else {
                    // 网络失败时不设置值，触发超时回退
                    Log.e("CommentLoad", "网络加载失败: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<CommentInfo>> call, Throwable t) {
                commentLiveData.setValue(null);
            }
        });
        return commentLiveData;
    }
    public LiveData<CommentInfo> getCommentById(long id){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCommentById(id).enqueue(new Callback<CommentInfo>() {
            @Override
            public void onResponse(Call<CommentInfo> call, Response<CommentInfo> response) {
                singleCommentLiveData.setValue(response.body());
            }
            @Override
            public void onFailure(Call<CommentInfo> call, Throwable t) {
                singleCommentLiveData.setValue(null);
            }
        });
        return singleCommentLiveData;
    }
    public LiveData<List<CommentInfo>> getChildComments(long id){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getChildComments(id).enqueue(new Callback<List<CommentInfo>>() {

            @Override
            public void onResponse(Call<List<CommentInfo>> call, Response<List<CommentInfo>> response) {
                commentLiveData.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<CommentInfo>> call, Throwable t) {
                commentLiveData.setValue(null);
            }
        });
        return commentLiveData;
    }
    public LiveData<List<CommentInfo>> getUnsyncedComments(){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getUnsyncedComments().enqueue(new Callback<List<CommentInfo>>() {
            @Override
            public void onResponse(Call<List<CommentInfo>> call, Response<List<CommentInfo>> response) {
                unsyncedCommentsLiveData.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<CommentInfo>> call, Throwable t) {
                unsyncedCommentsLiveData.setValue(null);
            }
        });
        return unsyncedCommentsLiveData;
    }
    public void syncComment(CommentInfo comment,Runnable onSuccess) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        // 构造用于创建的payload：不携带本地自增ID，避免后端冲突
        CommentInfo serverComment = new CommentInfo();
        serverComment.setPost_id(comment.getPost_id());
        serverComment.setUser_id(comment.getUser_id());
        serverComment.setComment_content(comment.getComment_content());
        serverComment.setTimestamp(comment.getTimestamp());
        serverComment.setParentcomment_id(comment.getParentcomment_id());
        serverComment.setUsername(comment.getUsername());
        serverComment.setAvatar(comment.getAvatar());
        Call<CommentInfo> call = apiService.createComment(serverComment);
        call.enqueue(new Callback<CommentInfo>() {
            @Override
            public void onResponse(Call<CommentInfo> call, Response<CommentInfo> response) {
                if (response.isSuccessful()) {
                    // 同步成功，更新本地数据库
                    new Thread(() -> {
                        CommentInfo syncedComment = response.body();
                        CommentDao commentDao = MapApp.getAppDb().commentDao();

                        // 更新本地评论ID
                        if (syncedComment != null) {
                            // 1. 更新所有子评论的父ID
                            commentDao.rebindChildren(comment.getComment_id(), syncedComment.getComment_id());

                            // 2. 删除旧的本地评论
                            commentDao.deleteCommentById(comment.getComment_id());

                            // 3. 插入新的服务器评论
                            syncedComment.setSynced(true);
                            commentDao.insertComment(syncedComment);
                        }

                        syncStatusLiveData.postValue(true);
                        if (onSuccess != null) onSuccess.run();
                    }).start();
                } else {
                    syncStatusLiveData.postValue(false);
                    Log.e("CommentSync", "评论上传失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CommentInfo> call, Throwable t) {
                // 网络错误处理
                syncStatusLiveData.postValue(false);
                Log.e("CommentSync", "网络错误: " + t.getMessage());
            }
        });
    }

    // 批量同步评论
    public void syncCommentsBatch(List<CommentInfo> comments) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<CommentInfo>> call = apiService.syncCommentsBatch(comments);
        call.enqueue(new Callback<List<CommentInfo>>() {
            @Override
            public void onResponse(Call<List<CommentInfo>> call, Response<List<CommentInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 同步成功，更新本地数据库
                    new Thread(() -> {
                        CommentDao commentDao = MapApp.getAppDb().commentDao();
                        for (CommentInfo syncedComment : response.body()) {
                            // 更新本地评论ID（如果需要）
                            CommentInfo localComment = commentDao.getCommentById(syncedComment.getComment_id());
                            if (localComment != null) {
                                // 更新字段（如果需要）
                                localComment.setSynced(true);
                                commentDao.updateComment(localComment);
                            }
                        }
                        syncStatusLiveData.postValue(true);
                    }).start();
                } else {
                    syncStatusLiveData.postValue(false);
                    Log.e("CommentSync", "批量同步失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CommentInfo>> call, Throwable t) {
                // 网络错误处理
                syncStatusLiveData.postValue(false);
                Log.e("CommentSync", "批量同步网络错误: " + t.getMessage());
            }
        });
    }
    public void deleteCommentsBatch(List<Long> commentIds) {
        new Thread(() -> {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            CommentDao commentDao = MapApp.getAppDb().commentDao();

            for (long commentId : commentIds) {
                try {
                    // 1. 从服务器删除
                    Response<Void> response = apiService.deleteComment(commentId).execute();
                    if (response.isSuccessful()) {
                        // 2. 从本地数据库删除
                        commentDao.deleteCommentById(commentId);
                        Log.d("DELETE_COMMENT", "评论删除成功: " + commentId);
                    } else {
                        Log.e("DELETE_COMMENT", "评论删除失败: " + response.code());

                    }
                } catch (IOException e) {
                    Log.e("DELETE_COMMENT", "网络错误: " + e.getMessage());
                }
            }
        }).start();
    }
    public MutableLiveData<List<CommentInfo>> getCommentLiveData() {
        return commentLiveData;
    }
    public MutableLiveData<User> getUserLiveData() {
        return userLiveData;
    }
    public void downloadCommentsFromServer() {
        new Thread(() -> {
            try {
                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                AppDatabase db = MapApp.getAppDb();

                // 获取服务器上的所有评论
                Response<List<CommentInfo>> response = apiService.getAllComments().execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<CommentInfo> serverComments = response.body();
                    int downloadedCount = 0;
                    int skippedCount = 0;

                    // 获取本地所有笔记
                    List<NoteEntity> localNotes = db.noteDao().getAll();
                    // 转换为笔记ID列表
                    List<Long> localNoteIdList = localNotes.stream()
                            .map(NoteEntity::getId)
                            .collect(Collectors.toList());

                    for (CommentInfo comment : serverComments) {
                        // 检查评论对应的笔记是否存在于本地
                        if (localNoteIdList.contains(comment.getPost_id())) {
                            // 检查评论是否已存在
                            CommentInfo existingComment = db.commentDao().getCommentById(comment.getComment_id());

                            if (existingComment == null) {
                                // 插入新评论
                                comment.setSynced(true); // 标记为已同步
                                db.commentDao().insertComment(comment);
                                downloadedCount++;
                            } else {
                                // 更新已存在的评论
                                existingComment.setComment_content(comment.getComment_content());
                                existingComment.setTimestamp(comment.getTimestamp());
                                existingComment.setParentcomment_id(comment.getParentcomment_id());
                                existingComment.setUsername(comment.getUsername());
                                existingComment.setAvatar(comment.getAvatar());
                                existingComment.setSynced(true);
                                db.commentDao().updateComment(existingComment);
                                downloadedCount++;
                            }
                        } else {
                            skippedCount++;
                        }
                    }

                    Log.d("CommentDownload", "下载完成: " + downloadedCount + " 条评论已下载, " + skippedCount + " 条评论已跳过");

                } else {
                    Log.e("CommentDownload", "获取服务器评论失败: " + response.code());
                }
            } catch (IOException e) {
                Log.e("CommentDownload", "下载失败: " + e.getMessage());
            }
        }).start();
    }

}