package com.example.mapdemo.frame;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mapdemo.Adapter.MyAdapter;
import com.example.mapdemo.Bean.NoteCard;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.ViewModel.MyViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentNote#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentNote extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private List<NoteCard> MList;
    private RecyclerView.Adapter myAdapter;
    private FloatingActionButton floatButton;
    private FragmentManager fragmentManager;
    NoteDao noteDao = MapApp.getAppDb().noteDao();
    private TextView no_note,noteAlert;
    private LinearLayout itemCard;
    private MyViewModel viewModel;


    public FragmentNote() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentNote.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentNote newInstance(String param1, String param2) {
        FragmentNote fragment = new FragmentNote();
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
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        floatButton = view.findViewById(R.id.floating_action_button);
        no_note=view.findViewById(R.id.no_note);
        noteAlert=view.findViewById(R.id.noteAlert);
        floatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, AddNoteFragment.class, null).addToBackStack(null).commit();
            }
        });
        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        // 观察 LiveData
        viewModel.getNotesByUserID().observe(getViewLifecycleOwner(), notes -> {
            if (notes != null && notes.size() > 0) {
                MList = noteToCard(notes);
                InitEvent();
                noteAlert.setText(getString(R.string.note_alert, MList.size() + ""));
            } else {
                MList=noteToCard(noteDao.findByUserID(MapApp.getUserID()));
                InitEvent();
                noteAlert.setText(getString(R.string.note_alert, MList.size() + ""));
            }
        });


    }

    private ArrayList<NoteCard> noteToCard(List<NoteEntity> localNote) {
        ArrayList<NoteCard> cards = new ArrayList<>();
        for (NoteEntity noteEntity : localNote) {
            NoteCard noteCard = new NoteCard(noteEntity.id, noteEntity.user_name,
                    noteEntity.slogan, noteEntity.title, noteEntity.content, noteEntity.avatar_uri
                    , noteEntity.note_image_uri, noteEntity.create_time,noteEntity.longitude,noteEntity.latitude,noteEntity.isDirect);
            cards.add(noteCard);

        }
        return cards;
    }

    private void InitEvent() {
        myAdapter = new MyAdapter(MList, getActivity(), new MyAdapter.CountInterface() {
            @Override
            public void Count(int count) {
                noteAlert.setText(getString(R.string.note_alert, count + ""));
            }
        }, new MyAdapter.FragmentHelper() {
            @Override
            public void Helper(String title,String content,long id) {
                Bundle bundle=new Bundle();
                bundle.putString("title",title);
                bundle.putString("content",content);
                bundle.putBoolean("is_new",false);
                bundle.putLong("id",id);
                AddNoteFragment addNoteFragment=new AddNoteFragment();
                addNoteFragment.setArguments(bundle);
    fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, addNoteFragment,null).addToBackStack(null).commit();

}
        });
                recyclerView.setAdapter(myAdapter);
RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
    }



}