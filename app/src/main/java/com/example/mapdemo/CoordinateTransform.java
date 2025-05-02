package com.example.mapdemo;

public class CoordinateTransform {
    private static final double PI = 3.1415926535897932384626;
    private static final double A = 6378245.0;          // 克拉索夫斯基椭球长半轴
    private static final double EE = 0.00669342162296594323; // 第一偏心率平方

    // WGS84转GCJ02坐标系（精确算法）
    public static LatLng wgs84ToGcj02(double latitude, double longitude) {
        // 验证输入有效性
        if (!isValidCoordinate(latitude, longitude)) {
            // 抛出异常或返回错误值
            throw new IllegalArgumentException("Invalid coordinates: "
                    + latitude + ", " + longitude);
        }

        // 境外坐标不偏移
        if (isOutOfChina(latitude, longitude)) {
            return new LatLng(latitude, longitude);
        }

        // 核心偏移算法
        double[] delta = calculateDelta(latitude, longitude);
        double gcjLat = latitude + delta[0];
        double gcjLng = longitude + delta[1];


        return applyPrecisionCorrection(gcjLat, gcjLng);
    }

    // 坐标有效性验证
    private static boolean isValidCoordinate(double lat, double lng) {
        return (lat >= -90.0 && lat <= 90.0) &&
                (lng >= -180.0 && lng <= 180.0);
    }

    // 判断是否在中国境外
    private static boolean isOutOfChina(double lat, double lng) {
        return (lng < 72.004 || lng > 137.8347) ||
                (lat < 0.8293 || lat > 55.8271);
    }

    // 核心偏移计算（私有方法）
    private static double[] calculateDelta(double lat, double lng) {
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLon(lng - 105.0, lat - 35.0);

        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);

        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);

        return new double[]{dLat, dLng};
    }

    // 纬度偏移计算（私有方法）
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    // 经度偏移计算（私有方法）
    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    // 精度修正（可选）
    private static LatLng applyPrecisionCorrection(double lat, double lng) {
        // 根据实际测量数据添加修正系数（示例值，需根据实测调整）
        double latCorrection = 0.0000002;
        double lngCorrection = 0.0000003;

        return new LatLng(
                Math.round(lat / latCorrection) * latCorrection,
                Math.round(lng / lngCorrection) * lngCorrection
        );
    }


    public static class LatLng {
        public final double latitude;
        public final double longitude;

        public LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
