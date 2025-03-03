package com.example.mapdemo;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;

import java.net.URLEncoder;
import java.util.List;

public class PoiAdapter extends ArrayAdapter<PoiInfo> {
    private int resourceId;//布局资源ID
    private Context mContext;

    //构造函数
    public PoiAdapter(@NonNull Context context,int resource,@NonNull List<PoiInfo > objects){
        super(context, resource, objects);
        resourceId =resource;
        mContext=context;
    }
    /**
     * 获取列表项的视图，用于在列表中显示每个兴趣点（POI）的信息。
     * 该方法会根据列表项的位置加载对应的布局文件，并设置布局中控件的内容。
     * 同时，为导航按钮设置点击事件，点击后会启动导航功能。
     *
     * @param position    当前列表项的位置。
     * @param convertView 可复用的视图对象，如果为 null 则需要重新加载布局。
     * @param parent      列表项的父视图组。
     * @return 返回设置好内容的列表项视图。
     */
    @NonNull
    @Override
    public View getView(int position,@NonNull View convertView,@NonNull  ViewGroup parent) {
        //获取当前位置的兴趣点信息
        PoiInfo poi = getItem(position);
        //加载布局文件
        View view = LayoutInflater.from(mContext).inflate(resourceId, null);
        //获取布局中的控件
        TextView name = view.findViewById(R.id.poiname);
        TextView address = view.findViewById(R.id.poiaddress);
        Button goBtn = view.findViewById(R.id.goBtn);
        //设置控件内容
        name.setText(poi.getName());
        address.setText(poi.getAddress());
        // 设置导航按钮点击事件
        goBtn.setOnClickListener(v -> {
            if (poi.getLocation() != null) {
                double latitude = poi.getLocation().latitude;
                double longitude = poi.getLocation().longitude;
                String poiName = poi.getName();
                //启动导航功能
                startNavigation(getContext(), latitude, longitude, poiName);
            }
        });

        return view;
    }


 //开启导航
public static void startNavigation(Context context, double latitude, double longitude, String name) {
    //创建百度地图坐标对象
    LatLng bd09LatLng = new LatLng(latitude, longitude);
    //将百度地图坐标转换为高德地图坐标
    LatLng gcj02LatLng = CoordinateUtils.convertBD09ToGCJ02(bd09LatLng);
    //显示地图应用选择器
    showMapAppChooser(context, gcj02LatLng, bd09LatLng, name);
}
    /**
     * 显示地图应用选择器，让用户选择使用哪个地图应用进行导航
     *
     * @param context 上下文对象，用于创建对话框
     * @param gcj02   高德地图坐标，用于在高德地图中导航
     * @param bd09    百度地图坐标，用于在百度地图中导航
     * @param name    目的地名称，用于在导航应用中显示
     */

    private static void showMapAppChooser(Context context, LatLng gcj02, LatLng bd09, String name) {
        //创建地图应用列表
        String[] maps = {"高德地图", "百度地图（推荐）", "腾讯地图"};
        //创建对话框
        new AlertDialog.Builder(context)
                //创建对话的标题
                .setTitle("选择导航应用")
                //设置对话框选项
                .setItems(maps, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            launchAmap(context, gcj02, name);//高德地图
                            break;
                        case 1:
                            launchBaidu(context, bd09, name);//百度地图
                            break;
                        case 2:
                            launchTencent(context, gcj02, name);//腾讯地图
                            break;
                    }
                })
                .show();
    }

    //高德地图
    private static void launchAmap(Context context, LatLng gcj02, String name) {
        try {
            //对目的地名称进行URI编码
            String encodedName = URLEncoder.encode(name, "UTF-8");
            // 修改为路线规划URI，支持多交通方式选择
            //"androidamap://route?"发起导航的应用程序名称，route指的是路线规划
            //"sourceApplication="发起导航的应用程序名称
            String uri = "androidamap://route?" +
                    "sourceApplication=" + context.getPackageName() +
                    "&dname=" + encodedName +
                    "&dlat=" + gcj02.latitude +
                    "&dlon=" + gcj02.longitude +
                    "&dev=0";  // 不指定t参数以展示所有选项

           //创建意图 ACTION_VIEW 用于打开指定的URI，Uri.parse(uri)将URI字符串解析为Uri对象
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            //检查应用是否安装
            if (isAppInstalled(context, intent)) {
                context.startActivity(intent);//启动导航应用
            } else {
                //显示安装提示对话框
                showInstallDialog(context, "com.autonavi.minimap");
            }
        } catch (Exception e) {
            Toast.makeText(context, "启动高德地图失败", Toast.LENGTH_SHORT).show();
        }
    }
//百度地图
    private static void launchBaidu(Context context, LatLng bd09, String name) {
        try {
            String encodedName = URLEncoder.encode(name, "UTF-8");
            String uri = "bdapp://map/direction?destination=latlng:" + bd09.latitude +
                    "," + bd09.longitude + "|name:" + encodedName + "&mode=driving&coord_type=bd09ll";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (isAppInstalled(context, intent)) {
                context.startActivity(intent);
            } else {
                showInstallDialog(context, "com.baidu.BaiduMap");
            }
        } catch (Exception e) {
            Toast.makeText(context, "启动百度地图失败", Toast.LENGTH_SHORT).show();
        }
    }
//腾讯地图
    private static void launchTencent(Context context, LatLng gcj02, String name) {
        try {
            String encodedName = URLEncoder.encode(name, "UTF-8");
            String uri = "qqmap://map/routeplan?type=drive&to=" + encodedName +
                    "&tocoord=" + gcj02.latitude + "," + gcj02.longitude;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (isAppInstalled(context, intent)) {
                context.startActivity(intent);
            } else {
                showInstallDialog(context, "com.tencent.map");
            }
        } catch (Exception e) {
            Toast.makeText(context, "启动腾讯地图失败", Toast.LENGTH_SHORT).show();
        }
    }
//检查应用是否安装
    private static boolean isAppInstalled(Context context, Intent intent) {
        return intent.resolveActivity(context.getPackageManager()) != null;//检查本机上是否有处理intent的Activity
    }
//显示安装提示对话框
    private static void showInstallDialog(Context context, String packageName) {
        new AlertDialog.Builder(context)
                .setMessage("该应用未安装，是否前往安装？")
                .setPositiveButton("安装", (d, w) -> openAppStore(context, packageName))
                .setNegativeButton("取消", null)
                .show();
    }
//打开应用商店
    private static void openAppStore(Context context, String packageName) {
        try {
            //创建应用商店的URI
            Uri uri = Uri.parse("market://details?id=" + packageName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            //添加标志，确保在新任务中启动Activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //启动应用商店
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Uri webUri = Uri.parse("https://app.mi.com/detail/" + packageName);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
        }
    }
}
