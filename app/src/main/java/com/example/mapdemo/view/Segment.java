package com.example.mapdemo.view;

import android.graphics.RectF;

public class Segment {
    private int id;
    private float percentage;
    private RectF bbox;
    private float[] center;
    private String analysis;

    public Segment(int id, float percentage, float[] bbox, float[] center) {
        this.id = id;
        this.percentage = percentage;
        this.bbox = new RectF(bbox[0], bbox[1], bbox[0] + bbox[2], bbox[1] + bbox[3]);
        this.center = center;
    }

    public int getId() { return id; }
    public float getPercentage() { return percentage; }
    public RectF getBbox() { return bbox; }
    public float[] getCenter() { return center; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
}