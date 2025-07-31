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

import com.example.mapdemo.Adapter.PoiAdapter;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.ViewModel.MyViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FindFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView.Adapter poiAdapter;
    private List<NoteEntity> MList;
    private MyViewModel viewModel;

    public FindFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment2.
     */
    // TODO: Rename and change types and number of parameters
    public static FindFragment newInstance(String param1, String param2) {
        FindFragment fragment = new FindFragment();
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
        return inflater.inflate(R.layout.find_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        // 观察 LiveData

        viewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {

            if (notes != null && notes.size() > 0) {
                 MList =notes;
                poiAdapter = new PoiAdapter(MList, getActivity(), new PoiAdapter.CountInterface() {
                    @Override
                    public void Count(int count) {

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
                } else MList =new ArrayList<>();
        });






    }
}