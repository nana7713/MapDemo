package com.example.mapdemo;

import android.os.Bundle;

import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.model.LatLng;

/**
 * 每个Marker点，包含Marker点坐标、图标以及额外信息
 */
public class MyItem implements ClusterItem {
    private final LatLng mPosition; // 点
    private Bundle buns; // 额外信息
    private BitmapDescriptor icon; // 图标

    public MyItem(LatLng latLng) {
        mPosition = latLng;
    }

    public MyItem(LatLng latLng, Bundle bun) {
        mPosition = latLng;
        buns = bun;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public Bundle getExtraInfo() {
        return buns;
    }

    @Override
    public BitmapDescriptor getBitmapDescriptor() {
        return icon;
    }

    // 设置图标的方法
    public void setBitmapDescriptor(BitmapDescriptor icon) {
        this.icon = icon;
    }
}