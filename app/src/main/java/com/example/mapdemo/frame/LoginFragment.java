package com.example.mapdemo.frame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapdemo.ApiService;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginFragment extends Fragment {
    UserDao userDao = MapApp.getAppDb().userDao();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Button login;
    private TextView register;
    private FragmentManager fragmentManager;
    //private List<User> users = userDao.getAll();
    private List<User> users;
    private String mParam1;
    boolean isFromMap = false;
    private String mParam2;
    private CheckBox rememberPassword, autoLogin;
    EditText Eaccount, Epassword;


    public LoginFragment() {
        // Required empty public constructor
    }


    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Eaccount = view.findViewById(R.id.account);
        Epassword = view.findViewById(R.id.password);
        login = view.findViewById(R.id.login);
        rememberPassword = view.findViewById(R.id.remember_password);
        autoLogin = view.findViewById(R.id.auto_login);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // 调用上传笔记的方法
        Call<List<User>> call = apiService.getAllUsers();
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    users = response.body();
                }

            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.d("debug", "获取失败");

            }
        });

        if (getArguments() != null)
            isFromMap = getArguments().getBoolean("isFromMap");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("spRecord", Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            Eaccount.setText(sharedPreferences.getString("account", ""));
            if (sharedPreferences.getBoolean("remember", false)) {
                rememberPassword.setChecked(true);
                Epassword.setText(sharedPreferences.getString("password", ""));
            }
            if (sharedPreferences.getBoolean("auto", false)) {
                MapApp.setUserID(sharedPreferences.getInt("uid", 0));//自动登录需要设置uid 否则会导致直接跳转到的主页出现空指针异常

                fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, MyPageFragment.class, null).commit();
            }
        }
        rememberPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    autoLogin.setChecked(false);
                }
            }
        });
        autoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rememberPassword.setChecked(true);
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = Eaccount.getText().toString();
                String password = Epassword.getText().toString();
                if (users.size() == 0)
                    Toast.makeText(getActivity(), "不存在该用户", Toast.LENGTH_LONG).show();
                for (int i = 0; i < users.size(); i++) {
                    if (TextUtils.equals(users.get(i).account, account)) {
                        if (TextUtils.equals(users.get(i).password, password)) {
                            int uid = users.get(i).getUid();
                            MapApp.setUserID(uid); // 保存用户 ID
                            Toast.makeText(getActivity(), "登录成功！", Toast.LENGTH_LONG).show();
                            fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            if (isFromMap) {
                                fragmentTransaction.replace(R.id.fragment, MapFragment.class, null).commit();//这样会导致fragment栈内有两个MapFragment
                            } else {
                                fragmentTransaction.replace(R.id.fragment, MyPageFragment.class, null).commit();
                            }
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("spRecord", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("uid", users.get(i).getUid());
                            editor.putString("account", account);
                            editor.putString("password", password);

                            if (rememberPassword.isChecked()) {
                                editor.putBoolean("remember", true);
                            } else {
                                editor.putBoolean("remember", false);
                            }
                            if (autoLogin.isChecked()) {
                                editor.putBoolean("auto", true);
                            }
                            editor.apply();

                        } else {
                            Toast.makeText(getActivity(), "密码错误", Toast.LENGTH_LONG).show();
                        }

                    }


                }


            }
        });
        register = view.findViewById(R.id.jumpToRegister);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager = getFragmentManager();//定义写在全局会导致程序崩溃
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, RegisterFragment.class, null).commit();
            }
        });
    }
}