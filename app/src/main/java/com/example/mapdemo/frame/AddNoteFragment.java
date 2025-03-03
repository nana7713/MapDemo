package com.example.mapdemo.frame;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    NoteDao noteDao = MapApp.getAppDb().noteDao();
    UserDao userDao=MapApp.getAppDb().userDao();
    private FragmentManager fragmentManager;
    private boolean is_new = true;
    private long id;
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
            Glide.with(getActivity()).load(noteEntity.note_image_uri).into(note_image);
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
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
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

                        noteDao.insertAll(new NoteEntity(user.getName(), MapApp.getUserID(),user.slogan, content, title, noteImageUri, save_time, user.getAvatar()));
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
}