package com.example.mapdemo.frame;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mapdemo.ApiService;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.RetrofitClient;
import com.example.mapdemo.ViewModel.MyViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    List<User> users;
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
        return inflater.inflate(R.layout.fragment_register, container, false);
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
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        User user=new User(account,password);

        MyViewModel viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        // 观察 LiveData
        viewModel.getAllUsers().observe(getViewLifecycleOwner(), users1 -> {//bug:数据库里必须先至少有一个用户数据才能继续执行
            if (users1 != null && users1.size() > 0) {
                users=users1;
                boolean is_exist=false;
                for (int i=0;i<users.size();i++){
                    if (user.account.equals(users.get(i).account)){
                        Toast.makeText(getActivity(), "用户已存在！", Toast.LENGTH_LONG).show();
                        is_exist=true;
                    }

                }
                if (!is_exist){
                    // 调用上传笔记的方法
                    Call<User> call2 = apiService.insertUser(user);
                    call2.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call2, Response<User> response) {
                            if (response.isSuccessful()) {
                                User serverUser = response.body();
                                userDao.insertAll(serverUser);
                                Log.d("API", "HTTP 成功，状态码: " + response.code());
                                Log.d("API", "响应头: " + response.headers());
                                // 检查是否是真正的成功（如 204 No Content）
                                if (response.code() == 204) {
                                    Log.w("API", "服务器返回 204，可能未实际保存数据");
                                }
                                Log.d("RegisterFragment", "用户注册成功");
                            } else {
                                Log.e("RegisterFragment", "用户注册失败：" + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call2, Throwable t) {

                            Log.e("RegisterFragment", "网络错误：" + t.getMessage());

                        }
                    });
                    Toast.makeText(getActivity(),"注册成功",Toast.LENGTH_LONG).show();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, LoginFragment.class, null).commit();
                }


            } else {
               users=userDao.getAll();
                boolean is_exist=false;
                for (int i=0;i<users.size();i++){
                    if (user.account.equals(users.get(i).account)){
                        Toast.makeText(getActivity(), "用户已存在！", Toast.LENGTH_LONG).show();
                        is_exist=true;
                    }

                }
                if (!is_exist) {
                    userDao.insertAll(user);
                }
            }
        });







    }
    }
//没有加入用户已存在审查功能，数据库会存在相同用户


