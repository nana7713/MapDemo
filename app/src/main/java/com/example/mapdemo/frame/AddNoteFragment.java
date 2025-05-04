package com.example.mapdemo.frame;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.example.mapdemo.ApiService;
import com.example.mapdemo.CoordinateTransform;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.RetrofitClient;
import com.example.mapdemo.ViewModel.MyViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private double latitude,longitude;
    private Uri imageUri;
    NoteDao noteDao = MapApp.getAppDb().noteDao();
    UserDao userDao=MapApp.getAppDb().userDao();
    private FragmentManager fragmentManager;
    private boolean is_new = true;
    private long id;
    private String poiId;
    ActivityResultLauncher<Intent> pickMedia;
    private double exifLatitude=0.0;
    private double exifLongitude=0.0;
    User[] user = new User[1];
    NoteEntity noteEntity;



    // 新增权限请求码
    private static final int MEDIA_LOCATION_PERMISSION_CODE = 2021;
    private LocationClient mLocationClient;
    boolean isDirect=false;
    //private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Double currentLatitude;
    private Double currentLongitude;
    private Uri pendingImageUri; // 用于保存等待权限处理的图片URI
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(//注册一个活动结果的启动器，用于处理活动的结果，包含活动契约和回调函数
            new ActivityResultContracts.RequestPermission(),//建立了一个ResquestPermission类型的活动契约
            isGranted -> {
                if (isGranted) {
                    handleImageWithPermission(pendingImageUri);// 处理带有权限的图片
                } else {
                    showToast("需要位置权限来获取图片位置信息");
                    // 即使没有权限仍然加载图片，只是不处理位置信息
                    loadImageWithoutLocation(pendingImageUri);// 处理没有权限的图片
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
        try {//在添加笔记界面被创建的同时初始化定位服务，准备获取直接拍照时的经纬度，后续可以考虑将定位服务建立在底层Activity
            mLocationClient = new LocationClient(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.start();
        MyLocationListener myLocationListener = new MyLocationListener();
        //自定义的位置监听器注册到百度地图定位客户端
        mLocationClient.registerLocationListener(myLocationListener);
        super.onViewCreated(view, savedInstanceState);
        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Econtent = view.findViewById(R.id.content);
        Etitle = view.findViewById(R.id.title);
        closeButton = view.findViewById(R.id.close_button);
        saveButton = view.findViewById(R.id.save_button);
        saveButton.setEnabled(false);
        //User user=userDao.findById(MapApp.getUserID());
        saveTime = view.findViewById(R.id.save_time);
        saveWords=view.findViewById(R.id.save_words);
        note_image=view.findViewById(R.id.note_image);
        floatingActionButton=view.findViewById(R.id.floating_action_button);
        MyViewModel viewModel = new ViewModelProvider(AddNoteFragment.this).get(MyViewModel.class);

        // 观察 LiveData
        viewModel.getUserByID().observe(getViewLifecycleOwner(), user1 -> {
            if (user1 != null) {
                user[0] =user1;
                saveButton.setEnabled(true);
            }else{
                user[0]=userDao.findById(MapApp.getUserID());
                saveButton.setEnabled(true);
            }

        });
        pickMedia = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.

            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {//活动成功完成并活动返回的数据不为空，result.getData()返回一个Intent对象，包含了用户选择的媒体项的信息
                Uri uri = result.getData().getData();//获取用户选择的媒体项的URI
                try {
                    // 获取持久化读取权限
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    handleSelectedImageWithWorkaround(uri);
                } catch (SecurityException e) {
                    Log.e("Permission", "Error taking persistable uri permission", e);
                }
                // handleSelectedImageWithWorkaround(uri);//处理带有位置信息的图片
            }
        });
        if (getArguments() != null&&getArguments().getString("poiId")!=null){
            Toast.makeText(getActivity(),"test",Toast.LENGTH_LONG).show();
            poiId=getArguments().getString("poiId");
        }
        if (getArguments() != null&&getArguments().getString("content")!=null) {
            Econtent.setText(getArguments().getString("content"));
            Etitle.setText(getArguments().getString("title"));
            is_new = getArguments().getBoolean("is_new");//判断是否是新添加的笔记
            id = getArguments().getLong("id");

            viewModel.getNoteByID(id).observe(getViewLifecycleOwner(), noteEntity -> {
                if (noteEntity!=null) {
                    this.noteEntity=noteEntity;
                    if (noteEntity.note_image_uri != null) {
                        InputStream inputStream = null;
                        try {
                            inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(noteEntity.note_image_uri));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        note_image.setImageBitmap(bitmap);
                    }

                }
                else {
                    this.noteEntity=noteDao.findById(id);
                    //如果网络错误就获取本地数据库并渲染图片
                    //if (noteEntity.note_image_uri != null) {
                    //                InputStream inputStream = null;
                    //                try {
                    //                    inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(noteEntity.note_image_uri));
                    //                } catch (FileNotFoundException e) {
                    //                    throw new RuntimeException(e);
                    //                }
                    //                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    //                note_image.setImageBitmap(bitmap);
                    //            }
                }
            });

            //Glide.with(getActivity()).load(noteEntity.note_image_uri).into(note_image);

        }
        if (Econtent.getText() != null) {
            saveWords.setText(getString(R.string.note_words,Econtent.getText().toString().trim().length()+""));
        }

        save_time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
        saveTime.setText(save_time);
        Econtent.addTextChangedListener(new TextWatcher() {//监听字数变化
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
        //点击时间监听采用了Lambada表达式
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                popupMenu.inflate(R.menu.pop_menu);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int itemId = menuItem.getItemId();
                        if (itemId == R.id.direct_photography) {
                            takePicture();
                            isDirect = true;
                            return true;
                        } else if (itemId == R.id.choose_from_album) {
                            isDirect = false;
                            // 使用 Intent 打开系统文件选择器
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            pickMedia.launch(intent);
                            return true;
                        }
                        return AddNoteFragment.super.onOptionsItemSelected(menuItem);
                    }
                });
            }
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


                    //User user = userDao.findById(MapApp.getUserID());//获取当前用户
                    double finalLat;
                    double finalLng;
                    if(exifLatitude==0&&exifLongitude==0)
                    {
                        finalLat=latitude;
                        finalLng=longitude;
                    }
                    else
                    {
                        finalLat=exifLatitude;
                        finalLng=exifLongitude;
                    }
                    if (is_new) {
                        NoteEntity newNote;
                        if (poiId==null){
                         newNote = new NoteEntity(
                                user[0].getName(),
                                user[0].getUid(),
                                user[0].slogan,// 新增的字段
                                content,//输入的笔记内容
                                title,//标题
                                noteImageUri,//用户选择图片的uri
                                save_time,//保存时间
                                user[0].getAvatar(),//头像
                                finalLng,
                                finalLat,
                                isDirect,
                                "0"
                        );}else {
                             newNote = new NoteEntity(
                                    user[0].getName(),
                                    user[0].getUid(),
                                    user[0].slogan,// 新增的字段
                                    content,//输入的笔记内容
                                    title,//标题
                                    noteImageUri,//用户选择图片的uri
                                    save_time,//保存时间
                                    user[0].getAvatar(),//头像
                                    longitude,
                                    latitude,
                                    isDirect,
                                    poiId);
                        }


                        Log.d("Debug", "插入数据库前的 longitude: " + newNote.longitude);
                        if ( latitude != 0.0 && longitude != 0.0&& !Double.isNaN(latitude)&&!Double.isNaN(longitude)) {
                            Log.d("DatabaseDebug", "新笔记经纬度: " + newNote.getLatitude() + ", " + newNote.getLongitude());
                        }

                        User user = userDao.findById(newNote.user_id);
                        if (user == null) {
                            throw new IllegalArgumentException("User ID " + newNote.user_id + " 不存在！");
                        }
                        noteDao.insertAll(newNote);
                        // 创建 Retrofit 服务实例
                        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

                        // 调用上传笔记的方法
                        Call<Void> call = apiService.insert(newNote);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d("API", "HTTP 成功，状态码: " + response.code());
                                    Log.d("API", "响应头: " + response.headers());
                                    // 检查是否是真正的成功（如 204 No Content）
                                    if (response.code() == 204) {
                                        Log.w("API", "服务器返回 204，可能未实际保存数据");
                                    }
                                    Log.d("RegisterFragment", "笔记上传成功");
                                } else {
                                    Log.e("RegisterFragment", "笔记上传失败：" + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                                Log.e("RegisterFragment", "网络错误：" + t.getMessage());

                            }
                        });
                        //noteDao.insertAll(new NoteEntity(user.getName(), MapApp.getUserID(),user.slogan, content, title, noteImageUri, save_time, user.getAvatar()));

                    }
                    else {
                        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                        NoteEntity noteEntity = noteDao.findById(id);

                        noteEntity.setContent(content);
                        noteEntity.setTitle(title);
                        if (noteImageUri!=null) {
                            noteEntity.setNote_image_uri(noteImageUri);
                        }
                        noteDao.updateNote(noteEntity);


                        // 调用上传笔记的方法
                        Call<Void> call = apiService.insert(noteEntity);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    //原来这里使用Toast作为提示，但是由于网络请求异步执行，可能稍慢于页面的切换，而Toast的显示是依附于页面的，因此在这里使用Toast可能会因为请求完成时页面已经销毁切换导致闪退
                                } else {

                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                            }
                        });
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

            double wgsLat = convertToDegree(latStr, latRef);
            double wgsLng = convertToDegree(lngStr, lngRef);
            Log.d("EXIF_DEBUG", "转换后的坐标: " + wgsLat  + ", " + wgsLng );
            if (isValidCoordinate(wgsLat , wgsLng)) {

//                Log.d("EXIF_DEBUG", "转换后的坐标越界");
//                resetCoordinates();
//                showToast("坐标值超出合理范围");
//                return;
                CoordinateTransform.LatLng gcj02Point = CoordinateTransform.wgs84ToGcj02(wgsLat, wgsLng);
//                LatLng bdPoint=convertToBaiduCoord(gcj02Point.latitude,gcj02Point.longitude );
//                exifLatitude=bdPoint.latitude;
//                exifLongitude=bdPoint.longitude;
                exifLatitude = gcj02Point .latitude;
                exifLongitude = gcj02Point.longitude;
            }
            // 新增百度坐标系验证
            if (!isValidBaiduCoordinate(exifLatitude, exifLongitude)) {
                Log.e("COORD", "转换后坐标超出百度有效范围");
                resetCoordinates();
                return;
            }


            showLocationConfirmationDialog();

        } catch (IOException | SecurityException e) {
            Log.e("EXIF_DEBUG", "处理失败: " + e.getMessage());
            resetCoordinates();
            showToast("位置信息解析失败");
        }
    }

    // 新增百度坐标系验证（中国大致范围）
    private boolean isValidBaiduCoordinate(double lat, double lng) {
        return (lat >= 18.11 && lat <= 53.33) &&
                (lng >= 73.66 && lng <= 135.05);
    }
    private boolean isZeroDMS(String dms) {
        return dms != null && dms.equals("0/1,0/1,0/1");
    }

    private boolean isValidCoordinate(double lat, double lng) {
        return (lat >= -90.0 && lat <= 90.0) && (lng >= -180.0 && lng <= 180.0);
    }

    private void resetCoordinates() {
        exifLongitude  = 0; //默认值设置为多少还有待商榷 设为NaN会导致数据库出现NOT NULL constraint failed 原因不明
        exifLatitude  = 0;
    }


    // 显示位置确认对话框（独立方法）
    private void showLocationConfirmationDialog() {
        if (getActivity() == null || getActivity().isFinishing()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("确认位置")
                .setMessage(String.format("检测到位置：\n纬度: %.6f\n经度: %.6f",
                        exifLatitude, exifLongitude))
                .setPositiveButton("确认", null)
                .show();
    }


    private double convertToDegree(String stringDMS, String ref) {
        try {
            String normalized=stringDMS.replaceAll(" ",",");
            String[] dmsParts = normalized.split(",", 3);
            if (dmsParts.length != 3) {
               // Log.e("CONVERT_DEGREE", "无效的度分秒格式: " + stringDMS);
                throw new IllegalArgumentException("Invaild DMS format:"+stringDMS );
                //return 0.0;
            }
            // 调用 parseRational 方法将度、分、秒部分的字符串转换为对应的小数值
            double degrees = parseRational(dmsParts[0]);
            double minutes = parseRational(dmsParts[1]);
            double seconds = parseRational(dmsParts[2]);

            double result = degrees + (minutes / 60.0) + (seconds / 3600.0);
            return ("S".equals(ref) || "W".equals(ref)) ? -result : result;//如果是南纬或西经，结果取负数
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
        // 解析分子和分母
        double numerator = Double.parseDouble(parts[0]);
        double denominator = Double.parseDouble(parts[1]);
        return numerator / denominator;
    }

    //检查媒体位置权限位置状态
    private void checkAndRequestLocationPermission(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (hasMediaLocationPermission()) {//如果已经有了媒体位置权限
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
    private void takePicture(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
            doTake();
        }else{
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},1);
        }

    }

    private void doTake() {
        String filename = save_time; //自定义的照片名称
        File outputImage = new File(getActivity().getExternalCacheDir(),filename);  //拍照后照片存储路径
        try {if (outputImage.exists()){

            outputImage.delete();
        }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            //图片的url
            imageUri = FileProvider.getUriForFile(getActivity(), "com.example.takephoto.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //跳转界面到系统自带的拍照界面
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");  //调用手机拍照功能其实就是启动一个activity
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);  //指定图片存放位置，指定后，在onActivityResult里得到的Data将为null
        startActivityForResult(intent, 1);  //开启相机
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1&&resultCode==Activity.RESULT_OK ){
            exifLatitude=0.0;
            exifLongitude=0.0;
            if (resultCode==-1){
                InputStream inputStream= null;
                try {
                    inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                Bitmap bitmap=BitmapFactory.decodeStream(inputStream);
                note_image.setImageBitmap(bitmap);
                noteImageUri= String.valueOf(imageUri);
                //Glide.with(getActivity()).load(imageUri).into(note_image); 使用glide会导致再次拍照时只会显示第一次的拍照结果（可能是缓存问题）
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1){
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                doTake();
            }else{
                Toast.makeText(getActivity(),"你没有获得摄像头权限~",Toast.LENGTH_LONG).show();
            }
        }
    }
    public class MyLocationListener extends BDAbstractLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            latitude = location.getLatitude();    //获取纬度信息
            longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f

            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
        }
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
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION//指定权限标志，表示授予读取权限
            );

            // 加载图片
            noteImageUri = uri.toString();//将Uri对象转换为字符串形式，并将其赋值给noteImageUri变量
            loadImageWithLocation(uri);

            // 处理位置信息
            handleSelectedImage(uri);
        } catch (SecurityException e) {
            Log.e("Permission", "Security Exception: " + e.getMessage());
            showToast("无法访问该图片");
            noteImageUri = null; // 避免存储无效URI
        }
    }

    // 新增图片加载方法
    private void loadImageWithoutLocation(Uri uri) {
        noteImageUri = uri.toString();
        Glide.with(requireActivity())//使用Glide库加载图片
                .load(uri)//指定要加载的图片的URI

                .into(note_image);//指定要将图片加载到的ImageView控件
    }


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
                // 尝试直接获取EXIF数据
                InputStream input = requireContext().getContentResolver().openInputStream(uri);//打开一个输入流，用于读取指定URI指向的媒体内容
                Bitmap bitmap = BitmapFactory.decodeStream(input);//使用BitmapFactory类的decodeStream方法将输入流解码为Bitmap对象
                if (input != null) input.close();

                // 尝试通过MediaStore获取原始路径
                String[] projection = {MediaStore.Images.ImageColumns.DATA};//定义一个字符串数组projection，用于指定要查询的列名
                Cursor cursor = requireContext().getContentResolver().query(
                        uri, projection, null, null, null);//使用getContentResolver()方法获取ContentResolver对象，然后调用query()方法查询指定的URI，返回一个Cursor对象
                //uri所选图片的uri,projection：一个字符串数组，指定要查询的列。在这个例子中，projection 只包含一列，即 MediaStore.Images.ImageColumns.DATA，它代表图片的文件路径。

                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);//获取指定列的索引
                    String filePath = cursor.getString(columnIndex);//使用getColumnIndexOrThrow()方法获取指定列的索引，然后使用getString()方法获取该列的值
                    cursor.close();

                    if (filePath != null && !filePath.isEmpty()) {
                        ExifInterface exif = new ExifInterface(filePath);//使用ExifInterface类的构造函数创建一个ExifInterface对象，该对象用于读取和写入EXIF数据
                        processRealExif(exif);//使用processRealExif()方法处理ExifInterface对象，该方法用于提取和处理EXIF数据
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e("EXIF_WORKAROUND", "Failed to get original EXIF", e);
            }
        }

        // 前面没成功就进行降级处理
        handleSelectedImage(uri);
    }
    private void processRealExif(ExifInterface exif) {
        // 提取EXIF数据
        String latStr = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);//纬度值
        String latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);//纬度参考信息
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
            double wgsLat = convertToDegree(latStr, latRef);
            double wgsLng = convertToDegree(lngStr, lngRef);

            // 验证坐标范围
            if (isValidCoordinate(wgsLat,wgsLng)) {
                CoordinateTransform.LatLng gcj02Point = CoordinateTransform.wgs84ToGcj02(wgsLat, wgsLng);
//                LatLng bdPoint = convertToBaiduCoord(gcj02Point.latitude,gcj02Point.longitude );
//                exifLatitude = bdPoint.latitude;
//                exifLongitude = bdPoint.longitude;
                exifLatitude = gcj02Point .latitude;
               exifLongitude = gcj02Point.longitude;
            }
            else{
                Log.d("EXIF_DEBUG", "坐标越界: "
                        + exifLatitude + ", " + exifLongitude);
                resetCoordinates();
                return;
            }
            // 验证坐标范围
            if (!isValidBaiduCoordinate(exifLatitude, exifLongitude)) {
                Log.e("COORD", "转换后坐标超出有效范围");
                resetCoordinates();
                return;
            }
            Log.i("EXIF_SUCCESS", "成功获取坐标: "
                    + exifLatitude + ", " + exifLongitude);
            showLocationConfirmationDialog();

        } catch (NumberFormatException e) {
            Log.e("EXIF_ERROR", "坐标转换失败: " + e.getMessage());
            resetCoordinates();
            showToast("位置格式错误");
        }
    }
//    // 增强坐标转换方法
//    private LatLng convertToBaiduCoord(double gcLat, double gcLng) {
//        try {
//
//            CoordinateConverter converter = new CoordinateConverter()
//                    .from(CoordinateConverter.CoordType.GPS)
//                    .coord(new LatLng(gcLat, gcLng));
//
//            LatLng result = converter.convert();
//
//            return result;
////            // 添加手动校准（根据实测数据调整）
////            double calibratedLat = result.latitude-0.005611361846;  // 纬度校准值
////          double calibratedLng = result.longitude - 0.00668795556; // 经度校准值
////            return new LatLng(calibratedLat, calibratedLng);
//        } catch (Exception e) {
//            Log.e("COORD_CONVERT", "坐标转换异常: " + e.getMessage());
//            return new LatLng(0, 0);
//        }
//    }

}