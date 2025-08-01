package com.example.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mapdemo.frame.MapFragment;
import com.example.mapdemo.frame.FindFragment;
import com.example.mapdemo.frame.LoginFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout ll_home, ll_find, ll_mine;
    private ImageView iv_home, iv_find, iv_mine;
    private TextView tv_home, tv_find, tv_mine;
    FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        showNoticeDialog();


    }
    /**
     * 重写 dispatchTouchEvent 方法，用于处理触摸事件。
     * 当检测到触摸事件为 ACTION_DOWN（按下）时，判断触摸位置是否在搜索结果列表之外。
     * 如果在列表之外，则隐藏搜索结果列表。
     *
     * @param ev 触摸事件对象，包含触摸事件的详细信息，如触摸位置、动作类型等。
     * @return 返回调用父类的 dispatchTouchEvent 方法的结果，以确保触摸事件能正常分发。
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ListView listView = findViewById(R.id.searchResult);
        //此处如果不添加判空条件 在点击下方导航栏会导致闪退 报错原因是为空的listView去调用方法 findViewBYId只有在视图加载后才能找到控件，因此切换其他fragment后为null
        if(listView!=null) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {

                int[] location = new int[2];
                listView.getLocationOnScreen(location);
                int left = location[0];
                int top = location[1];
                int right = left + listView.getWidth();
                int bottom = top + listView.getHeight();
                float x = ev.getRawX();//获取触摸事件的原始x坐标
                float y = ev.getRawY();//获取触摸事件的原始y坐标
                if (x < left || x > right || y < top || y > bottom) {
                    // 点击搜索结果列表之外区域，隐藏搜索结果列表
                    listView.setVisibility(View.GONE);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    private void initEvent() {
        //添加Fragment
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, MapFragment.class, null).commit();
        ll_home.setOnClickListener(this);
        ll_find.setOnClickListener(this);
        ll_mine.setOnClickListener(this); //设定点击监听器为MainActivity对象 监听函数可以直接写成公共方法
        iv_home.setSelected(true);
        tv_home.setTextColor(getResources().getColor(R.color.selected));

    }

    private void initView() {
        this.ll_home = findViewById(R.id.ll_home);
        this.ll_find = findViewById(R.id.ll_find);
        this.ll_mine = findViewById(R.id.ll_mine);
        this.iv_home = findViewById(R.id.iv_home);
        this.iv_find = findViewById(R.id.iv_find);
        this.iv_mine = findViewById(R.id.iv_mine);
        this.tv_home = findViewById(R.id.tv_home);
        this.tv_find = findViewById(R.id.tv_find);
        this.tv_mine = findViewById(R.id.tv_mine);
    }
/*private void resetBottomState(){
    iv_home.setSelected(false);
    tv_home.setTextColor(getResources().getColor(R.color.grey));
    iv_find.setSelected(false);
    tv_find.setTextColor(getResources().getColor(R.color.grey));
    iv_mine.setSelected(false);
    tv_mine.setTextColor(getResources().getColor(R.color.grey));
}*/
    @Override
    public void onClick(View view) {//监听函数 实现点击导航栏切换fragment
        int id = view.getId();
        if (id == R.id.ll_home&&!iv_home.isSelected()) {
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, MapFragment.class, null).commit();
            iv_home.setSelected(true);
            tv_home.setTextColor(getResources().getColor(R.color.selected));
            iv_find.setSelected(false);
            tv_find.setTextColor(getResources().getColor(R.color.grey));
            iv_mine.setSelected(false);
            tv_mine.setTextColor(getResources().getColor(R.color.grey));
        } else if (id == R.id.ll_find&&!iv_find.isSelected()) {
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, FindFragment.class, null).commit();
            iv_find.setSelected(true);
            tv_find.setTextColor(getResources().getColor(R.color.selected));
            iv_home.setSelected(false);
            tv_home.setTextColor(getResources().getColor(R.color.grey));
            iv_mine.setSelected(false);
            tv_mine.setTextColor(getResources().getColor(R.color.grey));
        } else if (id == R.id.ll_mine&&!iv_mine.isSelected()) {
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, LoginFragment.class, null).commit();
            iv_mine.setSelected(true);
            tv_mine.setTextColor(getResources().getColor(R.color.selected));
            iv_home.setSelected(false);
            tv_home.setTextColor(getResources().getColor(R.color.grey));
            iv_find.setSelected(false);
            tv_find.setTextColor(getResources().getColor(R.color.grey));
        }
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    // 在Activity的onCreate()中调用
    private void showNoticeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("用户须知")
                .setMessage(
                        "    由于优化问题，图片传输速度很慢，查看图片需要耐心等待\n" +
                        "    左下角分别为切换地图和重新定位按钮\n" +
                        "    切换地图是指切换为显示已登录用户创建的笔记的缩略图的模式\n" +
                        "    点击poi点(地图上的文字)可以添加笔记,有红色标记的poi点存有笔记\n"+
                        "    缩略图暂时只显示在“我的笔记”添加的笔记，不支持poi点笔记\n"+
                        "    右下角为快速添加笔记，笔记以当前所在位置记录经纬度\n"
                )
                .setPositiveButton("我知道了", (dialog, which) -> {
                    // 点击确认后操作
                    dialog.dismiss();
                })
                .setCancelable(false) // 禁止点击外部关闭
                .show();
    }
}
