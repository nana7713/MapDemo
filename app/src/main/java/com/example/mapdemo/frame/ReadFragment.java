package com.example.mapdemo.frame;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.ViewModel.MyViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ImageButton closeButton;
    private String title;
    private TextView saveTime;
    private String content;
    private TextView saveWords;
    private TextView Etitle;
    private String save_time;
    private TextView Econtent;
    private ImageView note_image;
    private String noteImageUri;
    private double latitude,longitude;
    private long id;
    NoteEntity noteEntity;
    private FragmentManager fragmentManager;
    NoteDao noteDao = MapApp.getAppDb().noteDao();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReadFragment newInstance(String param1, String param2) {
        ReadFragment fragment = new ReadFragment();
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
        return inflater.inflate(R.layout.fragment_read, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Econtent = view.findViewById(R.id.content);
        Etitle = view.findViewById(R.id.title);
        closeButton = view.findViewById(R.id.close_button);
        saveTime = view.findViewById(R.id.save_time);
        saveWords=view.findViewById(R.id.save_words);
        note_image=view.findViewById(R.id.note_image);
        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MyViewModel viewModel = new ViewModelProvider(ReadFragment.this).get(MyViewModel.class);
        if (getArguments() != null&&getArguments().getString("content")!=null) {
            Econtent.setText(getArguments().getString("content"));
            Etitle.setText(getArguments().getString("title"));
            id = getArguments().getLong("id");
            viewModel.getNoteByID(id).observe(getViewLifecycleOwner(), noteEntity -> {
                if (noteEntity!=null) {
                    this.noteEntity=noteEntity;
                    String imgUrl = noteEntity.note_image_uri;
                    if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                        // 用 Glide 加载网络图片
                        Glide.with(requireContext())
                                .load(imgUrl)          // 支持 http/https
                                .into(note_image);
                    } else {
                        // 没有图片时清空 ImageView
                        note_image.setImageDrawable(null);
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
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentTransaction.remove(ReadFragment.this).commit();
                fragmentManager.popBackStack();
            }
        });
    }
}