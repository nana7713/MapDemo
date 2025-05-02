package com.example.mapdemo.frame;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.baidu.mapapi.map.BaiduMap.MAP_TYPE_NORMAL;
import static com.baidu.mapapi.map.BaiduMap.MAP_TYPE_SATELLITE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.PoiTagType;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.MyItem;
import com.example.mapdemo.PoiAdapter;
import com.example.mapdemo.PoiOverlay;
import com.example.mapdemo.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    // 定义 TAG 变量
    private static final String TAG = "MapFragment";
    TextView locationInfo;//用于显示定位信息
    LocationClient mlocationClient;//声明一个LocationClient类型的变量，用于管理百度地图的定位功能
    MapView mMapView;//用于显示百度地图
    BaiduMap mBaiduMap = null;//管理百度地图的功能
    private PoiSearch mPoiSearch = null;// 声明一个私有变量 mPoiSearch，用于存储 PoiSearch 对象
    EditText mInputText;//搜索框
    BDLocation mCurlocation;//位置信息
    private PoiOverlay mPoiOverlay;//用于覆盖物
    private int mCurrentPage = 0; // 当前页码
    ListView listView;
    TextView textV;
    String type = "public";

    private List<NoteEntity> noteList;
    private List<PoiInfo> mAllPoiList = new ArrayList<>();//所有POI数据
    private ClusterManager<MyItem> mClusterManager;


    public MapFragment() {

    }


    public static MapFragment newInstance(String param1, String param2) {

        MapFragment fragment = new MapFragment();
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
        return inflater.inflate(R.layout.map_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SDKInitializer.setAgreePrivacy(getActivity().getApplicationContext(), true); // 确保用户同意隐私政策
        SDKInitializer.initialize(getActivity().getApplicationContext());//百度地图SDK初始化
        //启用全面屏显示
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        //将在前端的东西与后端建立联系（findViewById）
        mMapView = view.findViewById(R.id.bmapView);
        if (getArguments() != null)
            type = getArguments().getString("type");

        //获取地图实例化
        mBaiduMap = mMapView.getMap();
        //设置地图类型为普通视图
        mBaiduMap.setMapType(MAP_TYPE_NORMAL);
        //启用定位图层（显示蓝点）
        mBaiduMap.setMyLocationEnabled(true);
        listView = view.findViewById(R.id.searchResult);
        textV = view.findViewById(R.id.inputText);
        initPoiOverlay();//Poi覆盖物初始化
        //设置地图监听器
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            //当地图被点击时调用
            public void onMapClick(LatLng latLng) {
                mBaiduMap.hideInfoWindow();
            }//隐藏地图上的信息窗口

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
            }
        });
        //  切换地图类型
        ImageButton matype = view.findViewById(R.id.mapTypeBtn);
        //matype.setContentDescription(getString(R.string.map_type_icon_description));//无障碍阅读
        matype.setOnClickListener(new View.OnClickListener() {//设置地图类型切换按钮的点击事件
            @Override
            public void onClick(View v) {
                // 提前定义 fragmentManager 和 fragmentTransaction 变量
                FragmentManager fragmentManager;
                FragmentTransaction fragmentTransaction;
                switch (type) {
                    case "public":
                        fragmentManager = getParentFragmentManager();
                        if (fragmentManager != null) {
                            fragmentTransaction = fragmentManager.beginTransaction();
                            MapFragment mapFragment1 = new MapFragment();
                            Bundle bundle1 = new Bundle();
                            bundle1.putString("type", "private");
                            mapFragment1.setArguments(bundle1);
                            fragmentTransaction.replace(R.id.fragment, mapFragment1, null).commit();
                        }
                        break;
                    case "private":
                        fragmentManager = getParentFragmentManager();
                        if (fragmentManager != null) {
                            fragmentTransaction = fragmentManager.beginTransaction();
                            MapFragment mapFragment2 = new MapFragment();
                            Bundle bundle2 = new Bundle();
                            bundle2.putString("type", "public");
                            mapFragment2.setArguments(bundle2);
                            fragmentTransaction.replace(R.id.fragment, mapFragment2, null).commit();
                            type = "public";
                        }
                        break;
                }
            }
        });

        //定位客户端初始化
        //隐私合规
        LocationClient.setAgreePrivacy(true);
        //创建百度定位客户端
        try {
            mlocationClient = new LocationClient(getActivity().getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //创建一个自定义的位置监听器实例
        MylocationListener mylocationListener = new MylocationListener();
        //自定义的位置监听器注册到百度地图定位客户端
        mlocationClient.registerLocationListener(mylocationListener);


        //定位按钮（回到当前位置）
        ImageButton locationBtn = view.findViewById(R.id.mylocation);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mylocationListener.setAutoLocation(true);//设置自动定位为真
                mlocationClient.start();//启动百度地图SDK中的定位客户端

            }
        });
        //设置地图单击事件监听
        mBaiduMap.setOnMapClickListener(listener);
        //poi检索实例
        mPoiSearch = PoiSearch.newInstance();
        //poi监听器
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
        //获得检索输入框控制
        mInputText = view.findViewById(R.id.inputText);
        //设置mInputText的编辑器动作监听器
        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {//当用户在编辑器中按下搜索按钮时触发
                boolean ret = false;
                //如果动作Id是搜索动作回车
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mCurrentPage = 0;
                    String city = mCurlocation.getCity();//获取当前城市
                    String KeyWord = v.getText().toString();//获取用户输入的关键词
                    ret = mPoiSearch.searchInCity(new PoiCitySearchOption()
                            .city(city)
                            .keyword(KeyWord)
                            .scope(2)
                            .pageCapacity(100)
                            .cityLimit(false)
                            .tag("美食")
                            .tag("旅游地点")
                            .tag("住宿")
                            .pageNum(mCurrentPage));

                    //搜索后隐藏键盘
                    InputMethodManager imum = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);//获取系统的输入法管理器
                    View view = getActivity().getWindow().peekDecorView();//获取当前窗口的根视图
                    if (view != null) {
                        imum.hideSoftInputFromWindow(view.getWindowToken(), 0);//隐藏软键盘
                    }
                }
                return ret;
            }
        });

        //权限请求逻辑

        List<String> permissionList = new ArrayList<>();//ArrayList是List接口的一个具体实现类，基于动态数组的实现，提供了对元素的快速随机访问
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
//        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
//            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        }
//        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
//            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE );
//        }
        if (!permissionList.isEmpty()) {//若permissionList里不为空，需要向用户申请权限
            String[] permissions = permissionList.toArray(new String[0]);//toArray-转换成数组
            ActivityCompat.requestPermissions(getActivity(), permissions, 1);
        } else {
            requestLocation();
        }

        // 设置覆盖物点击监听
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // 直接委托给PoiOverlay处理
                return mPoiOverlay.onMarkerClick(marker);
            }
        });

        // 初始化 ClusterManager
        mClusterManager = new ClusterManager<>(getActivity(), mBaiduMap);


        // 设置地图状态变化监听器（当地图缩放/移动时触发聚合计算）
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);

        // 设置地图标记点击监听器（点击单个标记或聚合点时触发）
        mBaiduMap.setOnMarkerClickListener(mClusterManager);

        //如果是个人地图，显示缩略图
        if (Objects.equals(type, "private")) {
            mBaiduMap.setPoiTagEnable(PoiTagType.All, false);
            noteList = getLocalNote();
            if (noteList != null) {
                addNotesToMap();
            }
        }

    }

    BaiduMap.OnMapClickListener listener = new BaiduMap.OnMapClickListener() {
        /**
         * 地图单击事件回调函数
         *
         * @param point 点击的地理坐标
         */
        @Override
        public void onMapClick(LatLng point) {

        }

        /**
         * 地图内 Poi 单击事件回调函数
         *
         * @param mapPoi 点击的 poi 信息
         */
        @Override
        public void onMapPoiClick(MapPoi mapPoi) {
            Bundle bundle = new Bundle();

            bundle.putString("poiName", mapPoi.getName());
            bundle.putString("poiId", mapPoi.getUid());
            PoiFragment poiFragment = new PoiFragment();
            poiFragment.setArguments(bundle);
            FragmentManager fragmentManager = getParentFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, poiFragment, null).addToBackStack(null).commit();
            }
        }
    };


    private List<NoteEntity> getLocalNote() {
        NoteDao noteDao = MapApp.getAppDb().noteDao();
        int userId = MapApp.getUserID();
        if (userId <= 0) {
            // 用户 ID 无效，给出提示信息
            Toast.makeText(getActivity(), "用户 ID 无效，请重新登录", Toast.LENGTH_LONG).show();
            return null;
        }
        List<NoteEntity> allNote = noteDao.findByUserID(userId);
        if (allNote.size() > 0) {
            return allNote;
        } else {
            Log.d(TAG, "getLocalNote: 没有笔记数据");
            return null;
        }
    }

    //将笔记添加到地图上
    private void addNotesToMap() {

        List<MyItem> items = new ArrayList<>();
        for (NoteEntity note : noteList) {
            if (!Objects.equals(note.getPoiId(), "0")) continue;
            if (note.getLatitude() != 0.0 && note.getLongitude() != 0.0) {

                CoordinateConverter converter = new CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.COMMON); // 设置原始坐标类型（GPS 或 COMMON）

                // 设置原始坐标
                converter.coord(new LatLng(note.getLatitude(), note.getLongitude()));

                // 转换为百度坐标（BD-09）
                LatLng point = converter.convert();


                // 获取笔记中的图片
                BitmapDescriptor icon = getNoteIcon(note);

                Bundle bundle = new Bundle();
                bundle.putLong("note_id", note.getId());

                //点聚合需要自定义一个类用来implements ClusterItem这个点聚合方法的类
                MyItem item = new MyItem(point, bundle);
                item.setBitmapDescriptor(icon);
                items.add(item);
                // 创建MarkerOptions并设置锚点
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(point)
                        .icon(icon)
                        .anchor(0.5f, 1.0f); // 设置锚点为图片底部中心

                // 添加标记到地图
                mBaiduMap.addOverlay(markerOptions);
            }
        }

        mClusterManager.addItems(items);

        //标记点击事件，进入编辑页面
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem item) {
                Bundle bundle = item.getExtraInfo();
                if (bundle != null) {
                    long noteId = bundle.getLong("note_id");
                    openNoteEditFragment(noteId);
                    return true;
                }
                return false;
            }
        });
        //聚合点点击事件，进入聚合点内多点的显示
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                LatLngBounds.Builder builder2 = new LatLngBounds.Builder();

                for (MyItem myItem : items) {
                    builder2.include(myItem.getPosition());
                }

                LatLngBounds latlngBounds = builder2.build();
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(
                        latlngBounds,
                        mMapView.getWidth(),
                        mMapView.getHeight()
                );
                mBaiduMap.animateMapStatus(u);
                return false;
            }
        });
    }

    //获取图片，并将其缩放到指定大小
    private BitmapDescriptor getNoteIcon(NoteEntity note) {
        if (note.getNote_image_uri() != null) {
            try {
                InputStream inputStream = requireActivity().getContentResolver().openInputStream(Uri.parse(note.getNote_image_uri()));
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                // 调整图片大小
                Bitmap resizedBitmap = resizeBitmap(originalBitmap); // 调整为 100x100 像素
                return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding); // 默认图标
    }

    // 调整图片大小，固定宽高为 100
    private Bitmap resizeBitmap(Bitmap originalBitmap) {
        int newWidth = 100;
        int newHeight = 100;
        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
    }

    //进入编辑页面
    private void openNoteEditFragment(long noteId) {
        FragmentManager fragmentManager = getParentFragmentManager();
        if (fragmentManager != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // 若 AddNoteFragment 没有 newInstance 方法，直接创建实例
            AddNoteFragment addNoteFragment = new AddNoteFragment();
            Bundle args = new Bundle();
            args.putLong("id", noteId);
            args.putBoolean("is_new", false);
            NoteEntity note = getNoteById(noteId);
            if (note != null) {
                args.putString("title", note.getTitle());
                args.putString("content", note.getContent());
            }
            addNoteFragment.setArguments(args);

            fragmentTransaction.replace(R.id.fragment, addNoteFragment).addToBackStack(null).commit();
        }
    }

    private NoteEntity getNoteById(long noteId) {
        NoteDao noteDao = MapApp.getAppDb().noteDao();
        return noteDao.findById(noteId);
    }


    OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            // 添加整体结果有效性检查
            if (poiResult == null || poiResult.getAllPoi() == null) {
                //Toast.makeText(MainActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                return;
            }
            // 检查地图控件有效性
            if (mBaiduMap == null || mMapView == null) {
                return;
            }
            if (poiResult != null) {
                //检查是否为第一页的搜索结果
                if (poiResult.getCurrentPageNum() == 0) {
                    mPoiOverlay.removeFromMap();//清除覆盖物
                    mAllPoiList.clear();//清除搜索结果
                }
                //获取所有的POI信息
                List<PoiInfo> poiList = poiResult.getAllPoi();
                //处理分页数据前列表有效性
                if (poiList == null || poiList.isEmpty()) {
                    if (poiResult.getCurrentPageNum() == 0) {
                        Toast.makeText(getActivity(), "未找到相关结果", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "没有更多数据了", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                // 确保mAllPoiList已初始化
                if (mAllPoiList == null) {
                    mAllPoiList = new ArrayList<>();
                }
                // 添加新的POI信息
                mAllPoiList.addAll(poiList);

                // 添加新POI覆盖物
                mPoiOverlay.setData(mAllPoiList); // 自定义构造函数传递所有数据
                mPoiOverlay.addToMap();
                mPoiOverlay.zoomToSpan();
                // 更新适配器
                if (poiResult.getCurrentPageNum() == 0) {
                    // 第一页时创建新适配器
                    PoiAdapter adapter = new PoiAdapter(getActivity(), R.layout.poi_item, mAllPoiList);
                    listView.setAdapter(adapter);
                } else {
                    // 后续页通知数据更新
                    ((PoiAdapter) listView.getAdapter()).notifyDataSetChanged();
                    // 滚动到新数据位置
                    listView.smoothScrollToPosition(mAllPoiList.size() - poiList.size());
                }
                listView.setVisibility(View.VISIBLE);

                // 更新分页控制
                mCurrentPage = poiResult.getCurrentPageNum();

                //当滑动到底部时加载更多搜索结果
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        //当滚动停止时
                        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                            //当滚动到底部时
                            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                                // 添加空指针保护
                                if (poiResult == null) {
                                    return;
                                }
                                // 添加数据有效性校验
                                if (mCurlocation == null || TextUtils.isEmpty(mCurlocation.getCity())) {
                                    Toast.makeText(getActivity(), "定位信息不可用", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                int curPage = poiResult.getCurrentPageNum();
                                int totalPage = poiResult.getTotalPageNum();
                                if (curPage < totalPage) {
                                    poiResult.setCurrentPageNum(curPage + 1);
                                    String city = mCurlocation.getCity();

                                    //if (textV==null) Toast.makeText(getActivity(), "booo", Toast.LENGTH_SHORT).show();
                                    //搜索下一页
                                    String KeyWord = textV.getText().toString();
                                    mPoiSearch.searchInCity(new PoiCitySearchOption().city(city).keyword(KeyWord).pageNum(curPage + 1));
                                } else {
                                    Toast.makeText(getActivity(), "已加载全部数据", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }


                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    }
                });


            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    //处理权限请求结果，当定位权限被授予时开始定位，否则提示用户需要权限

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            // 只检查定位权限是否被授予
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Toast.makeText(getActivity(), "需要定位权限才能使用本程序", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    //权限请求成功后，启动定位
    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "定位权限未授予，无法进行定位", Toast.LENGTH_SHORT).show();
            return;
        }

        initLocation();
        mlocationClient.start();//启动百度地图SDK中的定位客户端

    }

    //设置百度地图定位的各种参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();//配置百度地图定位的各种参数
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式-高精度
        option.setCoorType("bd09ll");//设置百度经纬度坐标
        option.setScanSpan(1000);//定位时间间隔
        option.setOpenGps(true);//打开GPS

        option.setLocationNotify(true);//在定位状态改变时触发通知-由GPS-WIFI
        option.setIgnoreKillProcess(false);//应用进程被杀死时，客户端也会停止工作
        option.SetIgnoreCacheException(false);//不忽略缓存异常
        option.setWifiCacheTimeOut(5 * 60 * 1000);//wifi缓存信息五分钟过期一次，过期后重新获取定位信息
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);//是否需要地址信息，国家、省、市、区、街道等
        mlocationClient.setLocOption(option);//将配置好的选项应用到客户端

    }


    //监听定位结果
    private class MylocationListener extends BDAbstractLocationListener {
        private boolean isFirstLoc = true;
        private boolean autoLocation = false;

        public void setAutoLocation(boolean b) {
            autoLocation = b;
        }

        //根据定位按钮修改
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //mapView 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }
            int type = bdLocation.getLocType();
            //构建MyLocaltionData对象，通过MyLocationData.Builder设置经纬度
            MyLocationData locData = new MyLocationData.Builder()
                    //设置定位精度
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            BaiduMap bmap = mMapView.getMap();//获取地图实例
            bmap.setMyLocationData(locData);//将定位数据设置到地图上
            /**
             *当首次定位或手动发起定位时，要放大地图，便于观察具体的位置
             * LatLng是缩放的中心点，这里注意一定要和上面设置给地图的经纬度一致；
             * MapStatus.Builder 地图状态构造器
             */


            if (isFirstLoc || autoLocation) {
                //将首次定位标志设置为false
                isFirstLoc = false;
                //将手动发起定位标志设置为false
                autoLocation = false;
                //创建Latlng对象，表示当前位置的经纬度


                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                //创建地图状态构造器
                MapStatus.Builder builder = new MapStatus.Builder();
                //设置缩放中心点；缩放比例；
                builder.target(ll).zoom(18.0f);
                //给地图设置状态
                bmap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            mCurlocation = bdLocation;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);//禁用定位图层
        mlocationClient.stop();
    }

    @Override
    public void onResume() {//暂停状态恢复到前台
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
        mMapView.onPause();
    }


    //初始化覆盖物
    private void initPoiOverlay() {
        // 初始化 PoiOverlay
        mPoiOverlay = new PoiOverlay(mBaiduMap, getActivity(), mAllPoiList) {
            @Override
            // 覆写此方法以改变默认点击行为
            public boolean onPoiClick(int index) {
                // 处理点击事件
                if (index < mAllPoiList.size()) {
                    // 获取点击的 PoiInfo
                    PoiInfo poiInfo = mAllPoiList.get(index);
                    // 显示 PoiInfo 的信息窗口
                    showPoiInfoWindow(poiInfo);
                    return true;
                }
                return false;
            }
        };

        //为百度地图设置标记点击监听器
        mBaiduMap.setOnMarkerClickListener(marker -> {
            if (mPoiOverlay.getOverlayOptions().contains(marker)) {//如果mPoiOverlay的覆盖物列表中包含该marker
                return mPoiOverlay.onMarkerClick(marker);//调用mPoiOverlay的onMarkerClick方法
            }
            return false;
        });
    }


    // 显示 PoiInfo 的信息窗口
    private void showPoiInfoWindow(PoiInfo poi) {
        //隐藏当前显示的信息窗口
        mBaiduMap.hideInfoWindow();
        // 加载自定义布局
        View infoWindow = LayoutInflater.from(getActivity()).inflate(R.layout.poi_info_window, null);
        //设置信息窗口的内边距
        infoWindow.setPadding(20, 20, 20, 20);
        //获取布局中的TextView和Button
        TextView title = infoWindow.findViewById(R.id.tv_title);
        TextView address = infoWindow.findViewById(R.id.tv_address);
        Button navigateBtn = infoWindow.findViewById(R.id.btn_navigate);
        //设置TextView的文本为兴趣点名称和地址
        title.setText(poi.getName());
        address.setText(poi.getAddress());
        //计算信息窗口的偏移量
        Point screenPosition = mBaiduMap.getProjection().toScreenLocation(poi.getLocation());
        int yOffset = (screenPosition.y > mMapView.getHeight() / 2) ? -200 : 200;
        // 创建信息窗口
        InfoWindow mInfoWindow = new InfoWindow(infoWindow, poi.getLocation(), -150);

        // 显示信息窗口
        mBaiduMap.showInfoWindow(mInfoWindow);

        // 处理导航按钮点击
        navigateBtn.setOnClickListener(v -> {
            if (poi.getLocation() != null) {
                PoiAdapter.startNavigation(
                        getActivity(),
                        poi.getLocation().latitude,
                        poi.getLocation().longitude,
                        poi.getName()
                );
                mBaiduMap.hideInfoWindow(); // 隐藏信息窗口
            }
        });
    }
}