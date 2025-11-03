package com.example.mapdemo.frame;

import android.annotation.SuppressLint;
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
                    loadNoteImage(noteEntity.note_image_uri);
                } else {
                    this.noteEntity = noteDao.findById(id);
                    loadNoteImage(this.noteEntity.note_image_uri);
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
            Glide.with(requireContext())
                    .load(imgUrl)
                    .into(note_image);
            // 显示分析按钮（有图片时才显示）
            analyzeButton.setVisibility(View.VISIBLE);
            note_image.setVisibility(View.VISIBLE);
        } else {
            note_image.setImageDrawable(null);
            analyzeButton.setVisibility(View.GONE);
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
                .connectTimeout(30, TimeUnit.SECONDS)  // 连接超时 30秒
                .readTimeout(60, TimeUnit.SECONDS)     // 读取超时 60秒
                .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时 30秒
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
                String overallAnalysis = json.getString("overallAnalysis");
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

        for (int i = 0; i < segmentsArray.length(); i++) {
            JSONObject segmentJson = segmentsArray.getJSONObject(i);
            JSONArray bboxArray = segmentJson.getJSONArray("bbox");

            float[] bbox = {
                    (float) bboxArray.getDouble(0),
                    (float) bboxArray.getDouble(1),
                    (float) bboxArray.getDouble(2),
                    (float) bboxArray.getDouble(3)
            };

            JSONArray centerArray = segmentJson.getJSONArray("center");
            float[] center = {
                    (float) centerArray.getDouble(0),
                    (float) centerArray.getDouble(1)
            };

            Segment segment = new Segment(
                    segmentJson.getInt("id"),
                    (float) segmentJson.getDouble("percentage"),
                    bbox,
                    center
            );
            segments.add(segment);
        }
    }

    private void showSegmentAnalysis(Segment segment) {
        if (!isAnalysisCompleted) {
            Toast.makeText(getContext(), "请先进行图片分析", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("分析区域中...");

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("image_url", noteEntity.getNote_image_thumbnail_uri());
            requestBody.put("segment_id", segment.getId());

            JSONArray bboxArray = new JSONArray();
            android.graphics.RectF bbox = segment.getBbox();
            bboxArray.put(bbox.left);
            bboxArray.put(bbox.top);
            bboxArray.put(bbox.width());
            bboxArray.put(bbox.height());
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
                requireActivity().runOnUiThread(() -> {
                    hideLoading();
                    try {
                        JSONObject json = new JSONObject(result);
                        if (json.getBoolean("success")) {
                            String segmentAnalysis = json.getString("segmentAnalysis");
                            showSegmentAnalysisDialog(segment, segmentAnalysis);
                        } else {
                            Toast.makeText(getContext(), "区域分析失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "数据解析失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getContext(), "分析完成！点击图片中的黄色区域查看详情", Toast.LENGTH_LONG).show();
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