package com.example.mapdemo.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mapdemo.ApiService;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyViewModel extends androidx.lifecycle.ViewModel {
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<NoteEntity>> userNotesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<NoteEntity>> allNotesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<NoteEntity>> noteLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<NoteEntity>> poiNoteLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> allUsersLiveData = new MutableLiveData<>();

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
    public LiveData<List<NoteEntity>> getNotesByPoi() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getNoteByPoi(MapApp.getUserID()).enqueue(new Callback<List<NoteEntity>>() {
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
//    public LiveData<User> getUserByID() {
//
//        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
//        apiService.getUserById(MapApp.getUserID()).enqueue(new Callback<User>() {
//            @Override
//            public void onResponse(Call<User>call, Response<User> response) {
//                userLiveData.setValue(response.body());
//            }
//
//            @Override
//            public void onFailure(Call<User> call, Throwable t) {
//                userLiveData.setValue(null);
//            }
//        });
//        return userLiveData;
//    }
public LiveData<User> getUserByID() {
    // 创建LiveData
    MutableLiveData<User> result = new MutableLiveData<>();

    new Thread(() -> {
        // 首先尝试从本地数据库获取
        User localUser = MapApp.getAppDb().userDao().findById(MapApp.getUserID());
        if (localUser != null) {
            result.postValue(localUser);
        }

        // 然后尝试从网络获取
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getUserById(MapApp.getUserID()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User serverUser = response.body();
                    result.postValue(serverUser);

                    // 保存到本地数据库
                    new Thread(() -> {
                        MapApp.getAppDb().userDao().insertAll(serverUser);
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // 网络失败时不处理，使用本地数据
            }
        });
    }).start();

    return result;
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
    public  LiveData<List<NoteEntity>> getNoteByID(int id){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getNoteByID(id).enqueue(new Callback<List<NoteEntity>>() {
            @Override
            public void onResponse(Call<List<NoteEntity>> call, Response<List<NoteEntity>> response) {
                noteLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<NoteEntity>> call, Throwable t) {
                noteLiveData.setValue(null);

            }
        });
        return noteLiveData;
    }
    public  LiveData<List<NoteEntity>> getNoteByPoi(int poiID){
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



}