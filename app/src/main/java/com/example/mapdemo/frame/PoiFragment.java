package com.example.mapdemo.frame;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapdemo.Adapter.PoiAdapter;
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
 * Use the {@link PoiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PoiFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String poiName;
    private String poiId;
    private String poiLatitude;
    private String poiLongitude;
    private TextView noteAlert;
    private RecyclerView.Adapter poiAdapter;
    private List<NoteEntity> MList;
    NoteDao noteDao = MapApp.getAppDb().noteDao();
    private FloatingActionButton floatButton;
    private FragmentManager fragmentManager;


    public PoiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PoiFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PoiFragment newInstance(String param1, String param2) {
        PoiFragment fragment = new PoiFragment();
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
            poiName=getArguments().getString("poiName");
            poiId=getArguments().getString("poiId");
            poiLatitude=getArguments().getString("latitude");
            poiLongitude=getArguments().getString("longitude");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_poi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        TextView poiNameView = view.findViewById(R.id.poiName);
        noteAlert = view.findViewById(R.id.noteAlert);
        poiNameView.setText(poiName);
        MyViewModel viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        // 观察 LiveData
        viewModel.getNotesByPoi(poiId).observe(getViewLifecycleOwner(), notes -> {
            if (notes != null && notes.size() > 0) {
                MList=notes;
                poiAdapter = new PoiAdapter(MList, getActivity(), new PoiAdapter.CountInterface() {
                    @Override
                    public void Count(int count) {
                        noteAlert.setText(getString(R.string.note_alert, count + ""));
                    }
                },new PoiAdapter.FragmentHelper(){
                    @Override
                    public void Helper(String title,String content,long id) {
                        Bundle bundle=new Bundle();
                        bundle.putString("title",title);
                        bundle.putString("content",content);
                        bundle.putLong("id",id);
                        ReadFragment readFragment=new ReadFragment();
                        readFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment, readFragment,null).addToBackStack(null).commit();

                    }
                });

                recyclerView.setAdapter(poiAdapter);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(),2);
                recyclerView.setLayoutManager(layoutManager);

                noteAlert.setText(getString(R.string.note_alert, MList.size() + ""));
            }

        });
        floatButton = view.findViewById(R.id.floating_action_button);
        floatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MapApp.getUserID()==0){
                    Toast.makeText(getActivity(),"请先登录",Toast.LENGTH_LONG).show();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isFromMap",true);
                    LoginFragment loginFragment=new LoginFragment();
                    loginFragment.setArguments(bundle);
                    fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, loginFragment, null).commit();
                }else{
                    Bundle bundle = new Bundle();
                    bundle.putString("poiId", poiId);
                    bundle.putString("poiLatitude",poiLatitude);
                    bundle.putString("poiLongitude",poiLongitude);

                    AddNoteFragment addNoteFragment = new AddNoteFragment();
                    addNoteFragment.setArguments(bundle);
                    fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, addNoteFragment, null).commit();
                }

            }
        });

    }
    private List<NoteEntity> getPoiNote() {//根据用户id得到数据库中对应的笔记数据
        List<NoteEntity> allNote = noteDao.findByPoiID(poiId);
        if (allNote.size() > 0) {
            return allNote;
        } else return new ArrayList<>();
    }
}