package com.example.mapdemo;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.room.Room;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.example.mapdemo.Database.AppDatabase;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;

import java.util.List;

public class MapApp extends Application {
    private static AppDatabase db;
    private static int UserID=0;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "test").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.setAgreePrivacy(this,true);
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);

        // 从 SharedPreferences 中读取用户 ID
        SharedPreferences sharedPreferences = getSharedPreferences("spRecord", MODE_PRIVATE);
        UserID = sharedPreferences.getInt("uid", 0);
    }
    public static AppDatabase getAppDb() {
        return db;
    }
    public static int getUserID(){return UserID;}
    public static void setUserID(int uid) {
        UserID = uid;
        // 保存用户 ID 到 SharedPreferences
        SharedPreferences sharedPreferences = getInstance().getSharedPreferences("spRecord", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("uid", uid);
        editor.apply();
    }
    private static MapApp instance;


    public static MapApp getInstance() {
        return instance;
    }


}
