package com.example.mapdemo.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mapdemo.ApiService;
import com.example.mapdemo.Database.CommentDao;
import com.example.mapdemo.Database.CommentInfo;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.RetrofitClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

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
//    public LiveData<Integer> getCommentCount(){
//        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
//        apiService.getCommentCount(MapApp.getUserID()).enqueue(new Callback<Integer>() {
//
//
//            @Override
//            public void onResponse(Call<Integer> call, Response<Integer> response) {
//                commentCountLiveData.setValue(response.body());
//            }
//            @Override
//            public void onFailure(Call<Integer> call, Throwable t) {
//                commentCountLiveData.setValue(null);
//            }
//        });
//        return commentCountLiveData;
//    }
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
        Call<CommentInfo> call = apiService.createComment(comment);
        call.enqueue(new Callback<CommentInfo>() {
            @Override
            public void onResponse(Call<CommentInfo> call, Response<CommentInfo> response) {
                if (response.isSuccessful()) {
                    // 同步成功，更新本地数据库
                    new Thread(() -> {
                        CommentInfo syncedComment = response.body();
                        if (syncedComment != null) {
                            // 更新评论ID
                            if (syncedComment.getComment_id() != comment.getComment_id()) {
                                comment.setComment_id(syncedComment.getComment_id());
                            }
                            // 标记为已同步
                            comment.setSynced(true);
                            MapApp.getAppDb().commentDao().updateComment(comment);
                        }
                        syncStatusLiveData.postValue(true);
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
                        // TODO: 添加重试逻辑
                    }
                } catch (IOException e) {
                    Log.e("DELETE_COMMENT", "网络错误: " + e.getMessage());
                    // TODO: 添加重试逻辑
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

}