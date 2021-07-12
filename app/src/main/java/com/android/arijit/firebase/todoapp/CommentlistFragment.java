package com.android.arijit.firebase.todoapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommentlistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentlistFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TODO_ID = "todoId";
    private String todoId = "Hello Blank";
    private String taskName;


    public CommentlistFragment() {
        // Required empty public constructor
    }


    public static CommentlistFragment newInstance(@NonNull String param1,@NonNull String param2) {
        CommentlistFragment fragment = new CommentlistFragment();
        Bundle args = new Bundle();
        args.putString(TODO_ID, param1);
        args.putString("Task", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            todoId = String.valueOf(getArguments().getString(TODO_ID));
            taskName = String.valueOf(getArguments().getString("Task"));
        }
    }
    private RecyclerView rvComments;
    private ArrayList<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private String user = null;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText et_comment_add;
    private TextView noComment;
    private String TAG = "Commentlist";
    public static boolean STATE_CHANGED;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_commentlist, container, false);
        ((TextView) root.findViewById(R.id.tv_header_comment)).setText(taskName);

        try {
            user = mAuth.getCurrentUser().getEmail();
        } catch (Exception e){
            user = "dummyUser";
        }
        STATE_CHANGED = true;
        user = user;
        noComment = (TextView) root.findViewById(R.id.tv_noComment);
        et_comment_add = (EditText) root.findViewById(R.id.et_comment_add);
        rvComments = root.findViewById(R.id.rv_comments);
        rvComments.setLayoutManager(new LinearLayoutManager(getActivity()));
        commentAdapter = new CommentAdapter(getContext(), commentList, todoId);
        rvComments.setAdapter(commentAdapter);


        ((FloatingActionButton) root.findViewById(R.id.fab_comment_add)).setOnClickListener(v -> {
            if(et_comment_add.getText().toString().trim().equals("")){
                Snackbar.make(getView(), "Can't add empty comment", Snackbar.LENGTH_SHORT)
                        .setAction(null,null)
                        .show();
                return;
            }
            closeKeyboard();
            Comment newComment = new Comment();
            newComment.setCmtext(et_comment_add.getText().toString().trim());
            HashMap<String, Object> data = new HashMap<>();
            data.put("tid", todoId);
            data.put("user", user);
            data.put("comment", newComment.getCmtext());
            data.put("cdate", newComment.getCmdate());
            data.put("serverTimestamp", FieldValue.serverTimestamp());
            firestore.collection("commentCollection")
                    .add(data)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                newComment.setCid(task.getResult().getId());
                                Log.i(TAG, "onComplete: "+newComment.getCid());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null)
                                .show();
                    });
            if(noComment.getVisibility()==View.VISIBLE){
                noComment.setVisibility(View.INVISIBLE);
            }
            et_comment_add.setText("");
            commentList.add(newComment);
            commentAdapter.notifyDataSetChanged();
            Snackbar.make(v, "Comment Added", Snackbar.LENGTH_LONG)
                    .setAction(null, null)
                    .show();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setListener();
    }

    private void setListener(){
        if(!STATE_CHANGED)
            return;
        STATE_CHANGED = false;
        Source src = Source.CACHE;
        if(InternetConnection.checkConnection(getContext())){
            src = Source.DEFAULT;
        }
        firestore.collection("commentCollection")
                .whereEqualTo("user", user)
                .whereEqualTo("tid", todoId)
                .orderBy("serverTimestamp")
                .get(src)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot ds:task.getResult()){
                            Comment tmp = new Comment();
                            tmp.setCid(ds.getId());
                            tmp.setCmtext(ds.get("comment").toString());
                            tmp.setCmdate(ds.get("cdate").toString());
                            commentList.add(tmp);
                        }
                        if(commentList.size()==0){
                            noComment.setVisibility(View.VISIBLE);
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.i(TAG, "setListener: "+e.getLocalizedMessage());
                    Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("",null)
                            .show();
                });

    }
    private void closeKeyboard() {
        View view = this.getActivity().getCurrentFocus();
        if (view!=null){
            //getActivity() is needed since this a single fragment class and not a activity
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}