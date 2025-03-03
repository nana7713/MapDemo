package com.example.mapdemo.frame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button loginOut,save;
    private RadioButton male,female;
    private EditText Eage,Eplace,Eusername,Eslogan;
    private String age,place,username,slogan;
    private ImageView avatar;
    private FragmentManager fragmentManager;
    UserDao userDao=MapApp.getAppDb().userDao();
    private FloatingActionButton floatingActionButton;
    private String avatarUri;

    public SetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetFragment newInstance(String param1, String param2) {
        SetFragment fragment = new SetFragment();
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
        return inflater.inflate(R.layout.fragment_set, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Eplace=view.findViewById(R.id.place);
        Eusername=view.findViewById(R.id.userName);
        Eage=view.findViewById(R.id.age);
        Eslogan=view.findViewById(R.id.slogan);
        male=view.findViewById(R.id.male);

        female=view.findViewById(R.id.female);
        save=view.findViewById(R.id.save);
        floatingActionButton=view.findViewById(R.id.floating_action_button);
        avatar=view.findViewById(R.id.avatar);
        loginOut=view.findViewById(R.id.login_out);
        User user=userDao.findById(MapApp.getUserID());
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        Glide.with(getActivity()).load(uri).into(avatar);
                        avatarUri= String.valueOf(uri);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
        loginOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences= getActivity().getSharedPreferences("spRecord", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("auto",false);
                editor.apply();
                fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, LoginFragment.class, null).commit();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                place=Eplace.getText().toString().trim();
                age=Eage.getText().toString().trim();
                slogan=Eslogan.getText().toString().trim();
                username=Eusername.getText().toString().trim();
                if (!TextUtils.isEmpty(username))
                    user.setName(username);
                if (!TextUtils.isEmpty(place))
                    user.setPlace(place);
                if (!TextUtils.isEmpty(age))
                    user.setAge(age);
                if (male.isChecked())
                    user.setGender("男");
                if (female.isChecked())
                    user.setGender("女");
                if (!TextUtils.isEmpty(slogan))
                    user.setSlogan(slogan);
                if (!TextUtils.isEmpty(avatarUri))
                    user.setAvatar(avatarUri);
                userDao.updateUser(user);
                Toast.makeText(getActivity(),"保存成功！",Toast.LENGTH_LONG).show();
            }
        });
    }
}