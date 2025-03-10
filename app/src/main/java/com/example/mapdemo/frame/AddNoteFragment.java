package com.example.mapdemo.frame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private LocationClient mLocationClient;
    boolean isDirect=false;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        User user=userDao.findById(MapApp.getUserID());
        saveTime = view.findViewById(R.id.save_time);
        saveWords=view.findViewById(R.id.save_words);
        note_image=view.findViewById(R.id.note_image);
        floatingActionButton=view.findViewById(R.id.floating_action_button);
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        noteImageUri= String.valueOf(uri);
                        Glide.with(getActivity()).load(uri).into(note_image);

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        if (getArguments() != null) {
            Econtent.setText(getArguments().getString("content"));
            Etitle.setText(getArguments().getString("title"));
            is_new = getArguments().getBoolean("is_new");//判断是否是新添加的笔记
            id = getArguments().getLong("id");
            NoteEntity noteEntity = noteDao.findById(id);
            //Glide.with(getActivity()).load(noteEntity.note_image_uri).into(note_image);
            InputStream inputStream= null;
            try {
                inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(noteEntity.note_image_uri));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
            note_image.setImageBitmap(bitmap);
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
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu=new PopupMenu(getActivity(),view);
                popupMenu.inflate(R.menu.pop_menu);
                popupMenu.show();//点击显示菜单
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int itemId=menuItem.getItemId();
                        if (itemId==R.id.direct_photography){
                            takePicture();
                            isDirect=true;

                        }
                        if (itemId==R.id.choose_from_album){
                            isDirect=false;
                            pickMedia.launch(new PickVisualMediaRequest.Builder()
                                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                    .build());
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
                    if (is_new) {

                        noteDao.insertAll(new NoteEntity(user.getName(), MapApp.getUserID(),user.slogan, content, title, noteImageUri, save_time, user.getAvatar(),longitude,latitude,isDirect));
                    } else {
                        NoteEntity noteEntity = noteDao.findById(id);
                        noteEntity.setContent(content);
                        noteEntity.setTitle(title);
                        if (noteImageUri!=null) {
                            noteEntity.setNote_image_uri(noteImageUri);
                        }
                        noteDao.updateNote(noteEntity);

                    }

                    Toast.makeText(getActivity(), "保存成功！", Toast.LENGTH_LONG).show();
                    fragmentTransaction.remove(AddNoteFragment.this).commit();
                    fragmentManager.popBackStack();

                }
            }
        });
    }
    private void takePicture(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
            doTake();
        }else{
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},1);
        }

    }

    private void doTake() {
        String filename = "test.png"; //自定义的照片名称
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

        if (requestCode==1){
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
}