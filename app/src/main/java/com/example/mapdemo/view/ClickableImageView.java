package com.example.mapdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;
import java.util.List;

public class ClickableImageView extends AppCompatImageView {
    private List<com.example.mapdemo.view.Segment> segments = new ArrayList<>();
    private OnSegmentClickListener segmentClickListener;

    public ClickableImageView(Context context) {
        super(context);
        init();
    }

    public ClickableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && segmentClickListener != null) {
                    Segment clickedSegment = findSegmentAt(event.getX(), event.getY());
                    if (clickedSegment != null) {
                        segmentClickListener.onSegmentClick(clickedSegment);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
        invalidate();
    }

    private Segment findSegmentAt(float x, float y) {
        for (Segment segment : segments) {
            if (isPointInBBox(x, y, segment.getBbox())) {
                return segment;
            }
        }
        return null;
    }

    private boolean isPointInBBox(float x, float y, RectF bbox) {
        return x >= bbox.left && x <= bbox.right &&
                y >= bbox.top && y <= bbox.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSegments(canvas);
    }

    private void drawSegments(Canvas canvas) {
        if (segments.isEmpty()) return;

        Paint highlightPaint = new Paint();
        highlightPaint.setColor(Color.argb(50, 255, 255, 0));
        highlightPaint.setStyle(Paint.Style.FILL);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.YELLOW);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);

        for (Segment segment : segments) {
            canvas.drawRect(segment.getBbox(), highlightPaint);
            canvas.drawRect(segment.getBbox(), borderPaint);
        }
    }

    public interface OnSegmentClickListener {
        void onSegmentClick(Segment segment);
    }

    public void setOnSegmentClickListener(OnSegmentClickListener listener) {
        this.segmentClickListener = listener;
    }
}