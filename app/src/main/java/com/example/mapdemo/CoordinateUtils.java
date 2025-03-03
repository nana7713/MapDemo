package com.example.mapdemo;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

public class CoordinateUtils {
    /**
     * 将百度地图坐标（BD09）转换为火星坐标（GCJ02）
     *
     * @param source 要转换的百度地图坐标
     * @return 转换后的火星坐标
     */
    public static LatLng convertBD09ToGCJ02(LatLng source) {
        //创建一个坐标转换器
        CoordinateConverter converter = new CoordinateConverter();
        //设置源坐标类型为百度地图坐标（BD09）
        converter.from(CoordinateConverter.CoordType.BD09LL);
        //设置坐标转换器的源坐标
        converter.coord(source);
        //开始坐标转换
        return converter.convert();
    }
}
