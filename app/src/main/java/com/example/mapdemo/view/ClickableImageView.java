package com.example.mapdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClickableImageView extends AppCompatImageView {
    private List<com.example.mapdemo.view.Segment> segments = new ArrayList<>();
    private OnSegmentClickListener segmentClickListener;
    private RectF lastDrawableRect = new RectF(); // 存储上次计算的显示区域

    public ClickableImageView(Context context) {
        super(context);
        init();
    }

    public ClickableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScaleType(ScaleType.CENTER_CROP); // 确保设置为CENTER_CROP

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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 每次布局变化时重新计算显示区域
        updateDrawableRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSegments(canvas);
    }

    private void updateDrawableRect() {
        lastDrawableRect = calculateDrawableRect();
    }

    private RectF calculateDrawableRect() {
        if (getDrawable() == null || getWidth() == 0 || getHeight() == 0) {
            return new RectF(0, 0, getWidth(), getHeight());
        }

        int viewWidth = getWidth();      // View在屏幕上的宽度：1096px
        int viewHeight = getHeight();    // View在屏幕上的高度：983px
        int imgWidth = getDrawable().getIntrinsicWidth();   // 图片实际宽度：320px
        int imgHeight = getDrawable().getIntrinsicHeight(); // 图片实际高度：400px

        Log.d("RECT_CALC", String.format(
                "View尺寸: %dx%d, 图片实际尺寸: %dx%d, 拉伸比例: X=%.3f, Y=%.3f",
                viewWidth, viewHeight, imgWidth, imgHeight,
                (float)viewWidth/imgWidth, (float)viewHeight/imgHeight
        ));

        // 计算CENTER_CROP的缩放比例
        float viewRatio = (float) viewWidth / viewHeight;
        float imgRatio = (float) imgWidth / imgHeight;

        float scale;
        float scaledWidth, scaledHeight;
        float left, top;

        if (imgRatio > viewRatio) {
            // 图片更宽，按高度缩放
            scale = (float) viewHeight / imgHeight;
            scaledHeight = viewHeight;
            scaledWidth = imgWidth * scale;
            left = (viewWidth - scaledWidth) / 2;
            top = 0;
        } else {
            // 图片更高，按宽度缩放
            scale = (float) viewWidth / imgWidth;
            scaledWidth = viewWidth;
            scaledHeight = imgHeight * scale;
            left = 0;
            top = (viewHeight - scaledHeight) / 2;
        }

        RectF rect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Log.d("RECT_CALC", String.format(
                "显示区域: [%.1f,%.1f,%.1f,%.1f] 最终缩放比例: %.3f",
                rect.left, rect.top, rect.right, rect.bottom, scale
        ));

        return rect;
    }

    /**
     * 绘制优化后的区域 - 只绘制中心部分，减少重叠
     */
    private void drawSegments(Canvas canvas) {
        if (segments.isEmpty()) return;

        // 按面积排序，先绘制大区域，再绘制小区域（小区域在上层）
        List<Segment> sortedSegments = new ArrayList<>(segments);
        sortedSegments.sort((s1, s2) -> {
            float area1 = s1.getBbox().width() * s1.getBbox().height();
            float area2 = s2.getBbox().width() * s2.getBbox().height();
            return Float.compare(area2, area1); // 从大到小
        });

        Paint highlightPaint = new Paint();
        highlightPaint.setColor(Color.argb(80, 255, 255, 0)); // 半透明
        highlightPaint.setStyle(Paint.Style.FILL);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.YELLOW);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);

        Paint centerPaint = new Paint();
        centerPaint.setColor(Color.argb(150, 255, 100, 100)); // 中心区域更明显
        centerPaint.setStyle(Paint.Style.FILL);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(24);

        for (Segment segment : sortedSegments) {
            RectF serverBbox = segment.getBbox();
            RectF screenBbox = convertServerToScreenCoords(serverBbox);

            // 1. 绘制整个bbox（半透明）
            canvas.drawRect(screenBbox, highlightPaint);
            canvas.drawRect(screenBbox, borderPaint);

            // 2. 绘制中心区域（更突出，用于点击）
            float centerWidth = screenBbox.width() * 0.6f; // 只取中心60%
            float centerHeight = screenBbox.height() * 0.6f;
            float centerLeft = screenBbox.left + (screenBbox.width() - centerWidth) / 2;
            float centerTop = screenBbox.top + (screenBbox.height() - centerHeight) / 2;

            RectF centerRect = new RectF(centerLeft, centerTop,
                    centerLeft + centerWidth, centerTop + centerHeight);

            canvas.drawRect(centerRect, centerPaint);

            // 3. 绘制ID和百分比（在中心区域显示）
            canvas.drawText("ID:" + segment.getId(),
                    centerRect.left + 10, centerRect.top + 30, textPaint);
            canvas.drawText(String.format("%.0f%%", segment.getPercentage()),
                    centerRect.left + 10, centerRect.top + 60, textPaint);
        }
    }

    /**
     * 改进点击检测 - 优先检测中心区域
     */
    private Segment findSegmentAt(float screenX, float screenY) {
        float[] serverCoords = convertScreenToServerCoords(screenX, screenY);
        if (serverCoords == null) return null;

        float serverX = serverCoords[0];
        float serverY = serverCoords[1];

        List<Segment> sortedSegments = new ArrayList<>(segments);
        // 按面积从小到大排序（优先点击小区域）
        sortedSegments.sort(Comparator.comparingDouble(s ->
                s.getBbox().width() * s.getBbox().height()
        ));

        for (Segment segment : sortedSegments) {
            RectF serverBbox = segment.getBbox();

            // 检查是否在中心区域（60%范围内）
            float centerWidth = serverBbox.width() * 0.6f;
            float centerHeight = serverBbox.height() * 0.6f;
            float centerLeft = serverBbox.left + (serverBbox.width() - centerWidth) / 2;
            float centerTop = serverBbox.top + (serverBbox.height() - centerHeight) / 2;

            RectF centerRect = new RectF(centerLeft, centerTop,
                    centerLeft + centerWidth, centerTop + centerHeight);

            if (centerRect.contains(serverX, serverY)) {
                Log.d("CLICK", "点击在区域 " + segment.getId() + " 的中心区域");
                return segment;
            }

            // 如果不在中心区域，检查整个bbox（但优先级低）
            if (serverBbox.contains(serverX, serverY)) {
                Log.d("CLICK", "点击在区域 " + segment.getId() + " 的边缘区域");
                return segment;
            }
        }

        return null;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
        invalidate();

        // 记录设置的所有区域
        if (segments != null && !segments.isEmpty()) {
            Log.d("SEGMENTS_SET", "=== 设置区域列表 ===");
            for (Segment seg : segments) {
                RectF bbox = seg.getBbox();
                Log.d("SEGMENTS_SET", String.format(
                        "ID: %d, 占比: %.1f%%, bbox: [%.1f,%.1f,%.1f,%.1f]",
                        seg.getId(), seg.getPercentage(),
                        bbox.left, bbox.top, bbox.right, bbox.bottom
                ));
            }
        }
    }


    /**
     * 将服务器坐标（基于320x400）转换为屏幕坐标
     */
    private RectF convertServerToScreenCoords(RectF serverBbox) {
        RectF drawableRect = calculateDrawableRect();

        int imgWidth = getDrawable().getIntrinsicWidth();
        int imgHeight = getDrawable().getIntrinsicHeight();

        float scaleX = drawableRect.width() / imgWidth;
        float scaleY = drawableRect.height() / imgHeight;

        float pixelLeft = serverBbox.left * imgWidth;
        float pixelTop = serverBbox.top * imgHeight;
        float pixelRight = serverBbox.right * imgWidth;
        float pixelBottom = serverBbox.bottom * imgHeight;

        return new RectF(
                drawableRect.left + pixelLeft * scaleX,
                drawableRect.top + pixelTop * scaleY,
                drawableRect.left + pixelRight * scaleX,
                drawableRect.top + pixelBottom * scaleY
        );
    }

    /**
     * 将屏幕坐标转换为服务器坐标
     */
    private float[] convertScreenToServerCoords(float screenX, float screenY) {
        RectF drawableRect = calculateDrawableRect();

        if (!drawableRect.contains(screenX, screenY)) {
            return null;
        }

        int imgWidth = getDrawable().getIntrinsicWidth();
        int imgHeight = getDrawable().getIntrinsicHeight();

        float scaleX = drawableRect.width() / imgWidth;
        float scaleY = drawableRect.height() / imgHeight;

        float pixelX = (screenX - drawableRect.left) / scaleX;
        float pixelY = (screenY - drawableRect.top) / scaleY;

        float normalizedX = pixelX / imgWidth;
        float normalizedY = pixelY / imgHeight;

        normalizedX = Math.max(0, Math.min(normalizedX, 1));
        normalizedY = Math.max(0, Math.min(normalizedY, 1));

        return new float[]{normalizedX, normalizedY};
    }

    public interface OnSegmentClickListener {
        void onSegmentClick(Segment segment);
    }

    public void setOnSegmentClickListener(OnSegmentClickListener listener) {
        this.segmentClickListener = listener;
    }
}