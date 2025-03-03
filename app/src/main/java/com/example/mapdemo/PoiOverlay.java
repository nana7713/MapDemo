package com.example.mapdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示poi的overly
 */
public class PoiOverlay extends OverlayManager {

    private Context mContext; // 新增 Context 成员变量
    private static final int MAX_POI_SIZE = 10;

    private PoiResult mPoiResult = null;
    private List<PoiInfo> mPoiList;
    /**
     * 构造函数
     * 
     * @param baiduMap
     *            该 PoiOverlay 引用的 BaiduMap 对象
     */
    public PoiOverlay(BaiduMap baiduMap,Context context,List<PoiInfo> poiList) {
        super(baiduMap);
        mContext=context;
        mPoiList=poiList;
    }

    /**
     * 设置POI数据
     * 
     * @param poiResult
     *            设置POI数据
     */
    public void setData(List<PoiInfo> poiResult) {
        mPoiList = poiResult;
    }
    public void setData(PoiResult poiResult) {
        this.mPoiResult = poiResult;
    }



    //为每个POI创建一个MakerOptions对象，并添加到OverlayOptions列表中，最后返回OverlayOptions列表。用于在地图上显示标记
    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        //初始化列表
        List<OverlayOptions> optionsList = new ArrayList<>();
        //遍历POI列表
        for (int i=0;i<mPoiList.size();i++)
        {
            PoiInfo poi=mPoiList.get(i);
            if (poi.getLocation() == null) continue;

            //创建Bundle并设置index，在不同组件之间传递数据
            Bundle bundle=new Bundle();
            bundle.putInt("index",i);

            // 使用 mContext 访问资源
            Bitmap originalBitmap = BitmapFactory.decodeResource(
                    mContext.getResources(), // 通过 mContext 调用 getResources()
                    R.drawable.icon_gcoding    // 确保资源存在
            );

            // 缩放图片（示例：50x50 像素）
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    80,
                    80,
                    false
            );

            // 创建图标
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);

            // 添加标记
            optionsList.add(new MarkerOptions()
                    .position(poi.getLocation())
                    .icon(icon)
                    .title(poi.getName())
                    .extraInfo(bundle) // 添加extraInfo
            );
        }
        return optionsList;

    }

    /**
     * 获取该 PoiOverlay 的 poi数据
     *
     * @return
     */
    public PoiResult getPoiResult() {
        return mPoiResult;
    }

    /**
     * 覆写此方法以改变默认点击行为
     * 
     * @param i
     *            被点击的poi在
     *            {@link PoiResult#getAllPoi()} 中的索引
     * @return
     */
    public boolean onPoiClick(int i) {
        if (i >= 0 && i < mPoiList.size()) {
            // 处理点击事件
            return true;
        }
        return false;
    }

    /**
     * 重写标记点击事件处理方法，用于处理地图上标记的点击事件。
     * 当用户点击地图上的标记时，此方法会被调用。
     *
     * @param marker 被点击的标记对象，包含了标记的相关信息。
     * @return 如果点击事件被成功处理，返回 true；否则返回 false。
     */
    @Override
    public final boolean onMarkerClick(Marker marker) {
        // 检查标记是否在Overlay列表中
        // 如果在，调用onPoiClick方法处理点击事件
        //如果不在列表中，说明该标记不是由当前PoiOverlay创建的，返回false
        if (!mOverlayList.contains(marker)) {
            return false;
        }
        //检查标记是否携带了额外信息
        //如果携带了额外信息，说明该标记是由当前PoiOverlay创建的
        //从额外信息中获取index，调用onPoiClick方法处理点击事件
        if (marker.getExtraInfo() != null) {
            return onPoiClick(marker.getExtraInfo().getInt("index"));
        }
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        // TODO Auto-generated method stub
        return false;
    }

}
