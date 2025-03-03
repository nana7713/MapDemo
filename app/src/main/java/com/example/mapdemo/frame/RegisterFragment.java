package com.example.mapdemo.frame;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Button button;
    private EditText Eaccount,Epassword,Epassword_again;
    private String account,password,password_again;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    UserDao userDao= MapApp.getAppDb().userDao();
    public RegisterFragment() {
        // Required empty public constructor
    }


    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
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
        return inflater.inflate(R.layout.fragment_4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button=view.findViewById(R.id.login);
        Eaccount=view.findViewById(R.id.account);
        Epassword=view.findViewById(R.id.password);
        Epassword_again=view.findViewById(R.id.password_again);

        button.setOnClickListener(this);
        }

    @Override
    public void onClick(View view) {

        account=Eaccount.getText().toString();
        password=Epassword.getText().toString();
        password_again=Epassword_again.getText().toString();

        if (TextUtils.isEmpty(account)){
            Toast.makeText(getActivity(),"请输入账号",Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(getActivity(),"请输入密码",Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password_again)) {
            Toast.makeText(getActivity(),"请再次输入密码",Toast.LENGTH_LONG).show();
            return;
        }
        if (!TextUtils.equals(password,password_again)){
            Toast.makeText(getActivity(), "两次输入密码不一致",Toast.LENGTH_LONG).show();
            return;
        }
        User user=new User(account,password);
        userDao.insertAll(user);
        Toast.makeText(getActivity(),"注册成功",Toast.LENGTH_LONG).show();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, LoginFragment.class, null).commit();
    }
    }
//没有加入用户已存在审查功能，数据库会存在相同用户


