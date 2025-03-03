package com.example.mapdemo.frame;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ListView mine_list;
    private List<String> mStringList;
    private ArrayAdapter<String> mArrayAdapter;
    private FragmentManager fragmentManager;
    private TextView userName,place,age,gender;
    private ImageView avatar;
    UserDao userDao=MapApp.getAppDb().userDao();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment5.
     */
    // TODO: Rename and change types and number of parameters
    public static MyPageFragment newInstance(String param1, String param2) {
        MyPageFragment fragment = new MyPageFragment();
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
        return inflater.inflate(R.layout.fragment_5, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mine_list = view.findViewById(R.id.mine_list);
        userName=view.findViewById(R.id.tv_nickname);
        avatar=view.findViewById(R.id.iv_picture);
        age=view.findViewById(R.id.age);
        place=view.findViewById(R.id.place);
        gender=view.findViewById(R.id.gender);
        User user= userDao.findById(MapApp.getUserID());
        userName.setText(user.getName());
        age.setText(user.getAge());
        place.setText(user.getPlace());
        gender.setText(user.getGender());
        Glide.with(getActivity()).load(user.getAvatar()).into(avatar);


        mStringList = new ArrayList<>();
        mStringList.add("我的笔记");
        mStringList.add("我的相册");
        mStringList.add("设置");
        mArrayAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, mStringList);
        mine_list.setAdapter(mArrayAdapter);
        mine_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {//我的笔记
                    fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentManager.popBackStack();
                    fragmentTransaction.replace(R.id.fragment, FragmentNote.class, null).addToBackStack(null).commit();

                }
                if (i==2){//设置
                    fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentManager.popBackStack();
                    fragmentTransaction.replace(R.id.fragment, SetFragment.class, null).addToBackStack(null).commit();

                }
            }

        });
    }
}