package com.android.arijit.firebase.todoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TodolistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TodolistFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TodolistFragment() {
        // Required empty public constructor
    }


    public static TodolistFragment newInstance() {
        TodolistFragment fragment = new TodolistFragment();
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

    private RecyclerView rv_todolist;
    private ToDoAdapter toDoAdapter;
    private ArrayList<ToDo> toDoArrayList = new ArrayList<>();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String user=null, TAG = "Todolist";
    static boolean STATE_CHANGED = true;
    private Animation sync_rotate, sync_rotate_reverse;
    private ImageButton ib_sync, ib_more;
    private int sync_stat = 0;//0 means on

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_todolist, container, false);

        try {
            user = mAuth.getCurrentUser().getEmail();
        } catch (Exception e){
            user = "dummyUser";
        }


        rv_todolist = root.findViewById(R.id.rv_todolist);
        rv_todolist.setLayoutManager(new LinearLayoutManager(getActivity()));

        toDoAdapter = new ToDoAdapter(getContext(), toDoArrayList, getFragmentManager(), getActivity().findViewById(R.id.comments_fragment_container));
        rv_todolist.setAdapter(toDoAdapter);

        rv_todolist = root.findViewById(R.id.rv_todolist);
        ib_more = (ImageButton) root.findViewById(R.id.ib_more);
        ib_sync = (ImageButton) root.findViewById(R.id.ib_sync);

        sync_rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_sync);
        sync_rotate_reverse = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_sync_reverse);
        sync_rotate.setFillAfter(true);
        sync_rotate_reverse.setFillAfter(true);

        sync_stat = getActivity()
                    .getSharedPreferences("Settings", Context.MODE_PRIVATE)
                    .getInt("sync", 0);
        if(sync_stat == 0){
            ib_sync.setImageResource(R.drawable.ic_baseline_sync_24);
            firestore.enableNetwork();
        }   else {
            firestore.disableNetwork();
            ib_sync.setImageResource(R.drawable.ic_baseline_sync_disabled_24);
        }

        ib_more.setOnClickListener(v -> {
            PopupMenu pop = new PopupMenu(getContext(), v);
            pop.inflate(R.menu.home_menu);
            pop.show();
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.menu_home_profile:
//                            Snackbar.make(getView(), "Profile", Snackbar.LENGTH_SHORT)
//                                    .setAction(null,null)
//                                    .show();
                            getActivity().startActivity(new Intent(getContext(), ProfileActivity.class));
                            return true;
                        case R.id.menu_home_logout:
                            mAuth.signOut();
                            getActivity().startActivity(new Intent(getContext(), AccountActivity.class));
                            getActivity().finish();
                            return true;
                        default:
                            return false;
                    }
                }
            });
        });

        ib_sync.setOnClickListener(v -> {
            toggleSync();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(STATE_CHANGED) {
            toDoArrayList.clear();
            setListener();
        }
        else{
            STATE_CHANGED = true;
        }
        Log.i(TAG, "onResume: end");
    }

    private void setListener(){
        Source src = Source.CACHE;
        if(InternetConnection.checkConnection(getContext())){
            src = Source.DEFAULT;
        }
        Log.i(TAG, "setListener: entry");
        firestore.collection(user)
                .orderBy("serverTimestamp")
                .get(src)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isComplete()){
                            Log.i(TAG, "onComplete: firestoreGet");
                            for(QueryDocumentSnapshot ds:task.getResult()){
                                ToDo t = new ToDo();
                                t.setId(ds.getId());
                                t.setTask(ds.get("task").toString());
                                t.setCompleted((Boolean) ds.get("completed"));
                                t.setCdate(ds.get("date").toString());
                                toDoArrayList.add(t);
                                Log.i(TAG, "onComplete: "+t.getTask());
                            }
                            toDoAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG)
                                .setAction(null,null)
                                .show();
                    }
                });
    }

    private void toggleSync(){
        if(sync_stat == 0 ){
            sync_stat ^= 1;
            ib_sync.startAnimation(sync_rotate);
            sync_rotate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    ib_sync.setRotation(0);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ib_sync.setImageResource(R.drawable.ic_baseline_sync_disabled_24);
                    ib_sync.setRotation(135);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            firestore.disableNetwork();
        }
        else {
            sync_stat ^= 1;
            ib_sync.startAnimation(sync_rotate_reverse);
            sync_rotate_reverse.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    ib_sync.setImageResource(R.drawable.ic_baseline_sync_24);
                    ib_sync.setRotation(45);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            firestore.enableNetwork();
        }
        SharedPreferences.Editor mEdit = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE).edit();
        mEdit.putInt("sync", sync_stat).commit();
    }
}