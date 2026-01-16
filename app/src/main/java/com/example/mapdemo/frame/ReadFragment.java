package com.example.mapdemo.frame;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.ViewModel.MyViewModel;
import com.example.mapdemo.view.ClickableImageView;
import com.example.mapdemo.view.Segment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReadFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ImageButton closeButton;
    private String title;
    private TextView saveTime;
    private String content;
    private TextView saveWords;
    private TextView Etitle;
    private String save_time;
    private TextView Econtent;
    private ClickableImageView note_image; // 改为可点击的ImageView
    private Button analyzeButton; // 新增分析按钮
    private TextView analysisResult; // 新增分析结果显示
    private String noteImageUri;
    private double latitude, longitude;
    private long id;
    NoteEntity noteEntity;
    private FragmentManager fragmentManager;
    NoteDao noteDao = MapApp.getAppDb().noteDao();

    private List<Segment> segments = new ArrayList<>();
    private boolean isAnalysisCompleted = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReadFragment() {
        // Required empty public constructor
    }

    public static ReadFragment newInstance(String param1, String param2) {
        ReadFragment fragment = new ReadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_read, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewListeners();
        loadNoteData();
    }

    @SuppressLint("WrongViewCast")
    private void initViews(View view) {
        Econtent = view.findViewById(R.id.content);
        Etitle = view.findViewById(R.id.title);
        closeButton = view.findViewById(R.id.close_button);
        saveTime = view.findViewById(R.id.save_time);
        saveWords = view.findViewById(R.id.save_words);
        note_image = view.findViewById(R.id.note_image); // 确保布局中使用ClickableImageView
        analyzeButton = view.findViewById(R.id.analyze_button); // 新增的分析按钮
        analysisResult = view.findViewById(R.id.analysis_result); // 新增的分析结果TextView

        fragmentManager = getFragmentManager();
    }

    private void setupViewListeners() {
        // 关闭按钮
        closeButton.setOnClickListener(v -> closeFragment());

        // 分析按钮
        analyzeButton.setOnClickListener(v -> startImageAnalysis());

        // 图片区域点击监听
        note_image.setOnSegmentClickListener(segment -> showSegmentAnalysis(segment));

        // 文字变化监听
        Econtent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                saveWords.setText(getString(R.string.note_words, editable.toString().trim().length() + ""));
            }
        });
    }

    private void loadNoteData() {
        if (getArguments() != null && getArguments().getString("content") != null) {
            Econtent.setText(getArguments().getString("content"));
            Etitle.setText(getArguments().getString("title"));
            id = getArguments().getLong("id");

            MyViewModel viewModel = new ViewModelProvider(ReadFragment.this).get(MyViewModel.class);
            viewModel.getNoteByID(id).observe(getViewLifecycleOwner(), noteEntity -> {
                if (noteEntity != null) {
                    this.noteEntity = noteEntity;
                    loadNoteImage(noteEntity.getNote_image_thumbnail_uri());
                } else {
                    this.noteEntity = noteDao.findById(id);
                    loadNoteImage(this.noteEntity.getNote_image_thumbnail_uri());
                }
            });
        }

        if (Econtent.getText() != null) {
            saveWords.setText(getString(R.string.note_words, Econtent.getText().toString().trim().length() + ""));
        }

        save_time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
        saveTime.setText(save_time);
    }

    private void loadNoteImage(String imgUrl) {
        if (imgUrl != null && !imgUrl.trim().isEmpty()) {
            note_image.setClickable(true);
            note_image.setFocusable(true);
            note_image.setEnabled(true);

            Log.d("IMAGE_URL", "请求加载图片URL: " + imgUrl);
            Log.d("IMAGE_URL", "是否是缩略图: " + imgUrl.contains("thumbnail"));

            Glide.with(requireContext())
                    .load(imgUrl)
                    .override(Target.SIZE_ORIGINAL)  // 加载原始尺寸
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            Log.e("IMAGE_LOAD", "图片加载失败: " + (e != null ? e.getMessage() : "未知错误"));
                            Log.e("IMAGE_LOAD", "请求的model: " + model);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            // 图片加载完成后记录尺寸信息
                            Log.d("IMAGE_LOAD", "图片加载完成: ");
                            Log.d("IMAGE_LOAD", "  实际尺寸=" + resource.getIntrinsicWidth() + "x" + resource.getIntrinsicHeight());
                            Log.d("IMAGE_LOAD", "  View尺寸=" + note_image.getWidth() + "x" + note_image.getHeight());
                            Log.d("IMAGE_LOAD", "  数据源=" + dataSource);
                            Log.d("IMAGE_LOAD", "  请求的model=" + model);

                            return false;
                        }
                    })
                    .into(note_image);

            analyzeButton.setVisibility(View.VISIBLE);
            note_image.setVisibility(View.VISIBLE);
        }
    }

    private void startImageAnalysis() {
        if (noteEntity == null || noteEntity.getNote_image_thumbnail_uri() == null) {
            Toast.makeText(getContext(), "无法分析：图片不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("正在分析图片...");

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("image_url", noteEntity.getNote_image_thumbnail_uri());
        } catch (JSONException e) {
            e.printStackTrace();
            hideLoading();
            return;
        }
        // 创建带超时设置的 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)  // 连接超时 30秒
                .readTimeout(100, TimeUnit.SECONDS)     // 读取超时 60秒
                .writeTimeout(50, TimeUnit.SECONDS)    // 写入超时 30秒
                .build();

        Request request = new Request.Builder()
                .url("http://114.215.208.155:8080/api/ai/analyze/overall") // 你的服务器地址
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                requireActivity().runOnUiThread(() -> handleAnalysisResult(result));
            }

            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    hideLoading();
                    Log.d("TEST", "分析失败: " + e.getMessage());
                    Toast.makeText(getContext(), "分析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleAnalysisResult(String result) {
        hideLoading();
        Log.d("TEST", "服务器返回原始数据: " + result);

        try {
            JSONObject json = new JSONObject(result);
            if (json.getBoolean("success")) {
                // 解析区域数据
                parseSegmentsData(json.getJSONArray("segments"));

                // 显示整体分析结果
                String overallAnalysis = json.getString("analysis");
                analysisResult.setText(overallAnalysis);
                analysisResult.setVisibility(View.VISIBLE);

                // 设置可点击区域
                note_image.setSegments(segments);
                isAnalysisCompleted = true;

                // 显示提示
                showSegmentClickHint();

            } else {
                Toast.makeText(getContext(), "分析失败: " + json.getString("error"), Toast.LENGTH_SHORT).show();
                Log.d("TEST", "分析失败: " + json.getString("error"));
            }
        } catch (JSONException e) {
            Toast.makeText(getContext(), "数据解析失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseSegmentsData(JSONArray segmentsArray) throws JSONException {
        segments.clear();

        Log.d("PARSE_DEBUG", "开始解析区域数据，数量: " + segmentsArray.length());

        for (int i = 0; i < segmentsArray.length(); i++) {
            JSONObject segmentJson = segmentsArray.getJSONObject(i);
            int segmentId = segmentJson.getInt("id");

            // 解析bbox - 格式是[x1, y1, x2, y2]
            JSONArray bboxArray = segmentJson.getJSONArray("bbox");

            // 验证bbox数组长度
            if (bboxArray.length() != 4) {
                Log.e("PARSE_ERROR", "区域 " + segmentId + " bbox长度错误: " + bboxArray.length());
                continue;
            }

            float[] bbox = new float[4];
            for (int j = 0; j < 4; j++) {
                bbox[j] = (float) bboxArray.getDouble(j);
            }

            // 记录原始bbox
            Log.d("PARSE_DEBUG", String.format(
                    "解析区域 %d: 原始bbox [%.1f, %.1f, %.1f, %.1f]",
                    segmentId, bbox[0], bbox[1], bbox[2], bbox[3]
            ));

            // 解析center
            JSONArray centerArray = segmentJson.getJSONArray("center");
            float[] center = new float[2];
            center[0] = (float) centerArray.getDouble(0);
            center[1] = (float) centerArray.getDouble(1);

            float percentage = (float) segmentJson.getDouble("percentage");

            try {
                Segment segment = new Segment(
                        segmentId,
                        percentage,
                        bbox,
                        center
                );

                segments.add(segment);

                // 验证bbox合理性
                Log.d("PARSE_DEBUG", String.format(
                        "区域 %d 解析成功: bbox=%s, 中心=[%.1f,%.1f], 占比=%.1f%%",
                        segmentId, segment.getBboxString(),
                        center[0], center[1], percentage
                ));

            } catch (IllegalArgumentException e) {
                Log.e("PARSE_ERROR", "区域 " + segmentId + " 创建失败: " + e.getMessage());
            }
        }

        Log.d("PARSE_DEBUG", "解析完成，共 " + segments.size() + " 个区域");
    }

    private void showSegmentAnalysis(Segment segment) {
        if (!isAnalysisCompleted) {
            Toast.makeText(getContext(), "请先进行图片分析", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "开始分析区域 " + segment.getId() + "...", Toast.LENGTH_SHORT).show();
        showLoading("分析区域中...");

        JSONObject requestBody = new JSONObject();
        try {

            requestBody.put("image_url", noteEntity.getNote_image_thumbnail_uri());
            requestBody.put("segment_id", segment.getId());

            JSONArray bboxArray = new JSONArray();
            android.graphics.RectF bbox = segment.getBbox();
            bboxArray.put(bbox.left);
            bboxArray.put(bbox.top);
            bboxArray.put(bbox.right);
            bboxArray.put(bbox.bottom);
            requestBody.put("bbox", bboxArray);

        } catch (JSONException e) {
            e.printStackTrace();
            hideLoading();
            return;
        }
        // 创建带超时设置的 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // 连接超时 30秒
                .readTimeout(60, TimeUnit.SECONDS)     // 读取超时 60秒
                .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时 30秒
                .build();
        Request request = new Request.Builder()
                .url("http://114.215.208.155:8080/api/ai/analyze/segment")
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();

                // 添加详细日志
                Log.d("NETWORK_DEBUG", "HTTP状态码: " + response.code());
                Log.d("NETWORK_DEBUG", "响应头: " + response.headers());
                Log.d("NETWORK_DEBUG", "原始响应数据: " + result);
                Log.d("NETWORK_DEBUG", "响应数据长度: " + (result != null ? result.length() : 0));

                requireActivity().runOnUiThread(() -> {
                    hideLoading();
                    try {
                        // 检查响应是否为空
                        if (result == null || result.trim().isEmpty()) {
                            Log.e("NETWORK_DEBUG", "响应数据为空");
                            Toast.makeText(getContext(), "服务器返回空数据", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d("NETWORK_DEBUG", "开始解析JSON...");
                        JSONObject json = new JSONObject(result);

                        // 打印所有JSON键
                        Log.d("NETWORK_DEBUG", "JSON键列表:");
                        Iterator<String> keys = json.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            Object value = json.get(key);
                            Log.d("NETWORK_DEBUG", "Key: " + key + ", Value类型: " + value.getClass().getSimpleName() + ", Value: " + value);
                        }

                        // 检查success字段
                        if (json.has("success")) {
                            boolean success = json.getBoolean("success");
                            Log.d("NETWORK_DEBUG", "success字段值: " + success);

                            if (success) {
                                // 检查analysis字段
                                if (json.has("analysis")) {
                                    String segmentAnalysis = json.getString("analysis");
                                    Log.d("NETWORK_DEBUG", "analysis字段内容: " + segmentAnalysis);
                                    showSegmentAnalysisDialog(segment, segmentAnalysis);
                                } else {
                                    Log.e("NETWORK_DEBUG", "JSON中没有analysis字段");
                                    Toast.makeText(getContext(), "响应缺少analysis字段", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String error = json.has("error") ? json.getString("error") : "未知错误";
                                Log.e("NETWORK_DEBUG", "服务器返回错误: " + error);
                                Toast.makeText(getContext(), "区域分析失败: " + error, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("NETWORK_DEBUG", "JSON中没有success字段");
                            Toast.makeText(getContext(), "响应格式错误", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        Log.e("NETWORK_DEBUG", "JSON解析异常: " + e.getMessage());
                        Log.e("NETWORK_DEBUG", "异常堆栈: ", e);
                        Toast.makeText(getContext(), "数据解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("NETWORK_DEBUG", "其他异常: " + e.getMessage());
                        Toast.makeText(getContext(), "处理响应失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    hideLoading();
                    Log.e("NETWORK_DEBUG", "网络请求失败: " + e.getMessage());
                    Toast.makeText(getContext(), "网络请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showSegmentAnalysisDialog(Segment segment, String analysis) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("区域分析 (占比 " + segment.getPercentage() + "%)")
                .setMessage(analysis)
                .setPositiveButton("确定", null)
                .show();
    }

    private void showSegmentClickHint() {
        Toast.makeText(getContext(), "分析完成！点击图片中的红色区域查看详情", Toast.LENGTH_LONG).show();
    }

    private void showLoading(String message) {
        // 这里可以添加加载对话框
        analyzeButton.setEnabled(false);
        Econtent.setText("分析中...");
    }

    private void hideLoading() {
        analyzeButton.setEnabled(true);
        Econtent.setText("一键分析");
    }

    private void closeFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(ReadFragment.this).commit();
        fragmentManager.popBackStack();
    }
}