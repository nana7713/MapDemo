package com.example.mapdemo.frame;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddNoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNoteFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FloatingActionButton floatingActionButton;
    private ImageButton closeButton;
    private ImageButton saveButton;
    private String title;
    private TextView saveTime;
    private String content;
    private TextView saveWords;
    private EditText Etitle;
    private String save_time;
    private EditText Econtent;
    private ImageView note_image;
    private String noteImageUri;
    NoteDao noteDao = MapApp.getAppDb().noteDao();
    UserDao userDao=MapApp.getAppDb().userDao();
    private FragmentManager fragmentManager;
    private boolean is_new = true;
    private long id;
    ActivityResultLauncher<Intent> pickMedia;


    // 新增权限请求码
    private static final int MEDIA_LOCATION_PERMISSION_CODE = 2021;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Double currentLatitude;
    private Double currentLongitude;
    private Uri pendingImageUri; // 用于保存等待权限处理的图片URI
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    handleImageWithPermission(pendingImageUri);
                } else {
                    showToast("需要位置权限来获取图片位置信息");
                    // 即使没有权限仍然加载图片，只是不处理位置信息
                    loadImageWithoutLocation(pendingImageUri);
                }
            }
    );

    public AddNoteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddNoteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddNoteFragment newInstance(String param1, String param2) {
        AddNoteFragment fragment = new AddNoteFragment();
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
        return inflater.inflate(R.layout.fragment_add_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Econtent = view.findViewById(R.id.content);
        Etitle = view.findViewById(R.id.title);
        closeButton = view.findViewById(R.id.close_button);
        saveButton = view.findViewById(R.id.save_button);
        User user=userDao.findById(MapApp.getUserID());
        saveTime = view.findViewById(R.id.save_time);
        saveWords=view.findViewById(R.id.save_words);
        note_image=view.findViewById(R.id.note_image);
        floatingActionButton=view.findViewById(R.id.floating_action_button);
        pickMedia = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.

            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                handleSelectedImageWithWorkaround(uri);
            }
                });
        if (getArguments() != null) {
            Econtent.setText(getArguments().getString("content"));
            Etitle.setText(getArguments().getString("title"));
            is_new = getArguments().getBoolean("is_new");//判断是否是新添加的笔记
            id = getArguments().getLong("id");
            NoteEntity noteEntity = noteDao.findById(id);
            Glide.with(getActivity()).load(noteEntity.note_image_uri).into(note_image);
        }
        if (Econtent.getText() != null) {
            saveWords.setText(getString(R.string.note_words,Econtent.getText().toString().trim().length()+""));
        }

        save_time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
        saveTime.setText(save_time);
        Econtent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveWords.setText(getString(R.string.note_words,editable.toString().trim().length()+""));

            }
        });

                floatingActionButton.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    pickMedia.launch(intent);
                });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentTransaction.remove(AddNoteFragment.this).commit();
                fragmentManager.popBackStack();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content = Econtent.getText().toString().trim();
                title = Etitle.getText().toString().trim();

                if (content.isEmpty()) {
                    Toast.makeText(getActivity(), "内容不能为空", Toast.LENGTH_LONG).show();
                } else {
                    save_time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
                    saveTime.setText(save_time);
                    User user = userDao.findById(MapApp.getUserID());
                    if (is_new) {
                        NoteEntity newNote = new NoteEntity(
                                user.getName(),
                                MapApp.getUserID(),
                                user.slogan,
                                content,
                                title,
                                noteImageUri,
                                save_time,
                                user.getAvatar()
                        );

                        if (currentLatitude != null && currentLatitude != 0.0
                                && currentLongitude != null && currentLongitude != 0.0) {
                            newNote.setLatitude(currentLatitude);
                            newNote.setLongitude(currentLongitude);
                            Log.d("DatabaseDebug", "新笔记经纬度: " + newNote.getLatitude() + ", " + newNote.getLongitude());
                        }
                        noteDao.insertAll(newNote);

                        //noteDao.insertAll(new NoteEntity(user.getName(), MapApp.getUserID(),user.slogan, content, title, noteImageUri, save_time, user.getAvatar()));
                    } else {
                        NoteEntity noteEntity = noteDao.findById(id);
                        noteEntity.setContent(content);
                        noteEntity.setTitle(title);
                        if (noteImageUri!=null) {
                            noteEntity.setNote_image_uri(noteImageUri);
                        }

                        noteDao.updateNote(noteEntity);

                        //Log.d("DatabaseDebug", "更新笔记经纬度: " + noteEntity.getLatitude() + ", " + noteEntity.getLongitude());
                    }

                    Toast.makeText(getActivity(), "保存成功！", Toast.LENGTH_LONG).show();
                    fragmentTransaction.remove(AddNoteFragment.this).commit();
                    fragmentManager.popBackStack();

                }
            }
        });

    }
private void handleSelectedImage(Uri uri) {
    loadImageWithLocation(uri);
    try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri)) {
        if (inputStream == null) return;

        ExifInterface exif = new ExifInterface(inputStream);
        String latStr = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String lngStr = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String lngRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

        Log.d("EXIF_DEBUG", "原始数据: LAT=" + latStr + " " + latRef + ", LNG=" + lngStr + " " + lngRef);

        if (latStr == null || latRef == null || lngStr == null || lngRef == null) {
            Log.d("EXIF_DEBUG", "缺少必要的EXIF GPS标签");
            resetCoordinates();
            showToast("图片未包含有效位置信息");
            return;
        }

        // 检查是否为无效的零值
        if (isZeroDMS(latStr) || isZeroDMS(lngStr)) {
            Log.d("EXIF_DEBUG", "拦截到无效的零值坐标");
            resetCoordinates();
            showToast("图片包含无效的零坐标");
            return;
        }

        double latitude = convertToDegree(latStr, latRef);
        double longitude = convertToDegree(lngStr, lngRef);

        if (!isValidCoordinate(latitude, longitude)) {
            Log.d("EXIF_DEBUG", "转换后的坐标越界");
            resetCoordinates();
            showToast("坐标值超出合理范围");
            return;
        }

        currentLatitude = latitude;
        currentLongitude = longitude;
        Log.d("EXIF_DEBUG", "转换后的坐标: " + currentLatitude + ", " + currentLongitude);
        showLocationConfirmationDialog();

    } catch (IOException | SecurityException e) {
        Log.e("EXIF_DEBUG", "处理失败: " + e.getMessage());
        resetCoordinates();
        showToast("位置信息解析失败");
    }
}

    // 新增辅助方法
    private boolean isZeroDMS(String dms) {
        return dms != null && dms.equals("0/1,0/1,0/1");
    }

    private boolean isValidCoordinate(double lat, double lng) {
        return (lat >= -90.0 && lat <= 90.0) && (lng >= -180.0 && lng <= 180.0);
    }

    private void resetCoordinates() {
        currentLatitude = null;
        currentLongitude = null;
    }


    // 显示位置确认对话框（独立方法）
    private void showLocationConfirmationDialog() {
        if (getActivity() == null || getActivity().isFinishing()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("确认位置")
                .setMessage(String.format("检测到位置：\n纬度: %.6f\n经度: %.6f",
                        currentLatitude, currentLongitude))
                .setPositiveButton("正确", null)
                .setNegativeButton("重新选择", (d, w) -> {
                    // 启动地图选择时保存当前坐标作为备选
                   Double lastKnownLatitude = currentLatitude;
                   Double lastKnownLongitude = currentLongitude;
                    //showMapPicker();
                })
                .show();
    }


    private double convertToDegree(String stringDMS, String ref) {
        try {
            String[] dmsParts = stringDMS.split(",", 3);
            if (dmsParts.length != 3) {
                Log.e("CONVERT_DEGREE", "无效的度分秒格式: " + stringDMS);
                return 0.0;
            }

            double degrees = parseRational(dmsParts[0]);
            double minutes = parseRational(dmsParts[1]);
            double seconds = parseRational(dmsParts[2]);

            double result = degrees + (minutes / 60.0) + (seconds / 3600.0);
            return ("S".equals(ref) || "W".equals(ref)) ? -result : result;
        } catch (Exception e) {
            Log.e("CONVERT_DEGREE", "转换错误: " + e.getMessage());
            return 0.0;
        }
    }

    private double parseRational(String rational) {
        String[] parts = rational.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("无效的数值格式: " + rational);
        }
        double numerator = Double.parseDouble(parts[0]);
        double denominator = Double.parseDouble(parts[1]);
        return numerator / denominator;
    }


    private void checkAndRequestLocationPermission(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (hasMediaLocationPermission()) {
                handleImageWithPermission(uri);
            } else {
                showPermissionExplanationDialog(uri);
            }
        } else {
            // Android 9及以下版本不需要ACCESS_MEDIA_LOCATION权限
            handleImageWithPermission(uri);
        }
    }

    // 新增权限检查辅助方法
    private boolean hasMediaLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // 新增权限解释对话框
    private void showPermissionExplanationDialog(Uri uri) {
        new AlertDialog.Builder(requireContext())
                .setTitle("需要位置权限")
                .setMessage("为了读取图片中的位置信息，需要您授予媒体位置权限")
                .setPositiveButton("授予权限", (dialog, which) -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_MEDIA_LOCATION);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    showToast("将无法获取图片位置信息");
                    loadImageWithoutLocation(uri);
                })
                .show();
    }

    // 新增带权限处理的图片处理方法
    private void handleImageWithPermission(Uri uri) {
        try {
            // 获取持久化读取权限
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            // 加载图片
            loadImageWithLocation(uri);

            // 处理位置信息
            handleSelectedImage(uri);
        } catch (SecurityException e) {
            Log.e("Permission", "Security Exception: " + e.getMessage());
            showToast("无法访问该图片");
        }
    }

    // 新增图片加载方法（无位置处理）
    private void loadImageWithoutLocation(Uri uri) {
        noteImageUri = uri.toString();
        Glide.with(requireActivity())
                .load(uri)

                .into(note_image);
    }

    // 修改后的图片加载方法（含位置处理）
    private void loadImageWithLocation(Uri uri) {
        noteImageUri = uri.toString();
        Glide.with(requireActivity())
                .load(uri)

                .into(note_image);
    }
    private void showToast(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show()
        );

    }
    private void handleSelectedImageWithWorkaround(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 特殊处理Android 10+的原始文件访问
            try {
                InputStream input = requireContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                if (input != null) input.close();

                // 尝试通过MediaStore获取原始路径
                String[] projection = {MediaStore.Images.ImageColumns.DATA};
                Cursor cursor = requireContext().getContentResolver().query(
                        uri, projection, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    if (new File(filePath).exists()) {
                        ExifInterface exif = new ExifInterface(filePath);
                        processRealExif(exif);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e("EXIF_WORKAROUND", "Failed to get original EXIF", e);
            }
        }

        // 降级处理
        handleSelectedImage(uri);
    }
    private void processRealExif(ExifInterface exif) {
        // 提取EXIF数据
        String latStr = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String lngStr = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String lngRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

        Log.d("PROCESS_EXIF", "处理原始EXIF数据: "
                + latStr + "/" + latRef + ", "
                + lngStr + "/" + lngRef);

        // 验证数据有效性
        if (latStr == null || latRef == null || lngStr == null || lngRef == null) {
            Log.w("EXIF_WARNING", "EXIF信息不完整");
            resetCoordinates();
            return;
        }

        // 检查零值
        if (isZeroDMS(latStr) || isZeroDMS(lngStr)) {
            Log.d("EXIF_DEBUG", "拦截系统生成的零值坐标");
            resetCoordinates();
            showToast("图片元数据损坏");
            return;
        }

        try {
            // 转换坐标
            currentLatitude = convertToDegree(latStr, latRef);
            currentLongitude = convertToDegree(lngStr, lngRef);

            // 验证坐标范围
            if (!isValidCoordinate(currentLatitude, currentLongitude)) {
                Log.d("EXIF_DEBUG", "坐标越界: "
                        + currentLatitude + ", " + currentLongitude);
                resetCoordinates();
                return;
            }

            Log.i("EXIF_SUCCESS", "成功获取坐标: "
                    + currentLatitude + ", " + currentLongitude);
            showLocationConfirmationDialog();

        } catch (NumberFormatException e) {
            Log.e("EXIF_ERROR", "坐标转换失败: " + e.getMessage());
            resetCoordinates();
            showToast("位置格式错误");
        }
    }

}