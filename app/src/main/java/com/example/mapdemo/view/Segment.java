package com.example.mapdemo.view;

import android.graphics.RectF;

public class Segment {
    private int id;
    private float percentage;
    private RectF bbox;  // 格式：[x1, y1, x2, y2]
    private float[] center;
    private String analysis;

    // 构造器1：接收[x1, y1, x2, y2]格式
    public Segment(int id, float percentage, float[] bbox, float[] center) {
        this.id = id;
        this.percentage = percentage;

        // 直接使用x1,y1,x2,y2格式
        this.bbox = new RectF(bbox[0], bbox[1], bbox[2], bbox[3]);
        this.center = center;

        // 验证bbox合理性
        if (bbox[2] < bbox[0] || bbox[3] < bbox[1]) {
            throw new IllegalArgumentException("Invalid bbox coordinates");
        }
    }

    // 构造器2：接收单独的坐标
    public Segment(int id, float percentage, float x1, float y1, float x2, float y2, float[] center) {
        this.id = id;
        this.percentage = percentage;
        this.bbox = new RectF(x1, y1, x2, y2);
        this.center = center;
    }

    public int getId() { return id; }
    public float getPercentage() { return percentage; }
    public RectF getBbox() { return bbox; }
    public float[] getCenter() { return center; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    /**
     * 获取bbox的字符串表示
     */
    public String getBboxString() {
        return String.format("[%.1f, %.1f, %.1f, %.1f]",
                bbox.left, bbox.top, bbox.right, bbox.bottom);
    }

    /**
     * 验证bbox是否在合理范围内
     */
    public boolean isValidBbox(int maxWidth, int maxHeight) {
        return bbox.left >= 0 && bbox.top >= 0 &&
                bbox.right <= maxWidth && bbox.bottom <= maxHeight &&
                bbox.width() > 0 && bbox.height() > 0;
    }
}