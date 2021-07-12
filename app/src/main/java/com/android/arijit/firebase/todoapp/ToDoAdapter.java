package com.android.arijit.firebase.todoapp;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ToDoRowViewHolder> {
    private String TAG = "ToDoAdapter";
    private ArrayList<ToDo> toDoArrayList;
    private Context context;
    private FragmentManager mFragmentManager;
    private View root;

    public ToDoAdapter( Context context, ArrayList<ToDo> list, FragmentManager FM, View root){
        this.toDoArrayList = list;
        this.context = context;
        this.mFragmentManager = FM;
        this.root = root;
    }

    @NonNull
    @Override
    public ToDoRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recordView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_todo, parent, false);
        return new ToDoRowViewHolder(recordView);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoAdapter.ToDoRowViewHolder holder, int position) {
        holder.bindData(toDoArrayList.get(position), position);

        holder.itemView.setOnClickListener(v -> {
            //check r dual pane and act accordingly
            if(root == null ) {
                TodolistFragment.STATE_CHANGED = false;
                mFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.list_fragment_container, CommentlistFragment.newInstance(toDoArrayList.get(position).getId(), toDoArrayList.get(position).getTask()))
                        .addToBackStack(null)
                        .commit();
            }
            else {
                TodolistFragment.STATE_CHANGED = false;
                mFragmentManager.beginTransaction()
                        .replace(R.id.comments_fragment_container, CommentlistFragment.newInstance(toDoArrayList.get(position).getId(), toDoArrayList.get(position).getTask()))
                        .commit();
            }
        });
    }
    public ToDo getItem(int position){
        return toDoArrayList.get(position);
    }

    @Override
    public int getItemCount() {
        return toDoArrayList.size();
    }

    class ToDoRowViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener
    {
        private TextView tv_lvtask, tv_lvdate;
        private CheckBox cb_lvcompleted;
        private ImageView imageView;
        private int position;
        public ToDoRowViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_lvtask = (TextView) itemView.findViewById(R.id.tv_lvtask);
            tv_lvdate = (TextView) itemView.findViewById(R.id.tv_lvdate);
            cb_lvcompleted = (CheckBox) itemView.findViewById(R.id.cb_lvcompleted);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem copy = menu.add(0,1,1,"Copy");
            MenuItem delete = menu.add(0,2,2,"Delete");
            copy.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()){
                case 1:
                    copyToClipboard(toDoArrayList.get(position).getTask());
                    Snackbar.make(tv_lvtask, "Copied", Snackbar.LENGTH_SHORT)
                            .setAction(null,null)
                            .show();
                    return true;
                case 2:

                    new AlertDialog.Builder(context)
                            .setMessage("Delete this task")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String docId = toDoArrayList.get(position).getId();//todo id only
                                    String user = null;
                                    try{
                                        user = FirebaseAuth.getInstance()
                                                .getCurrentUser().getEmail();
                                    } catch (Exception e){
                                        user = "dummyUser";
                                    }
                                    FirebaseFirestore.getInstance()
                                            .collection("commentCollection")
                                            .whereEqualTo("user", user)
                                            .whereEqualTo("tid", docId)
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                CollectionReference commentCollection = FirebaseFirestore.getInstance()
                                                        .collection("commentCollection");
                                                for(QueryDocumentSnapshot ds: task.getResult()){
                                                            commentCollection
                                                                .document(ds.getId())
                                                                .delete();
                                                }
                                            });
                                    FirebaseFirestore.getInstance()
                                            .collection(user)
                                            .document(docId)
                                            .delete();
//

                                    toDoArrayList.remove(position);
                                    notifyDataSetChanged();
                                    Snackbar.make(tv_lvtask, "Deleted", Snackbar.LENGTH_SHORT)
                                            .setAction(null,null)
                                            .show();
                                }
                            })
                            .create().show();
                    return true;
                default:
                    return true;
            }

        }

        public void bindData(ToDo t, int position){
            this.position = position;
            tv_lvtask.setText(t.getTask());
            tv_lvdate.setText(t.getCdate());
            cb_lvcompleted.setChecked(t.isCompleted());

            if(cb_lvcompleted.isChecked()){
                tv_lvtask
                        .setPaintFlags(tv_lvtask.getPaintFlags()
                                | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else {
                tv_lvtask.setPaintFlags(tv_lvtask.getPaintFlags()
                        & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
            cb_lvcompleted.setOnClickListener(v -> {
                ToDo temp = toDoArrayList.get(position);
                temp.setCompleted(cb_lvcompleted.isChecked());
                toDoArrayList.set(position, temp);
                if(cb_lvcompleted.isChecked()){
                    tv_lvtask
                            .setPaintFlags(tv_lvtask.getPaintFlags()
                                    | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else {
                    tv_lvtask.setPaintFlags(tv_lvtask.getPaintFlags()
                            & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                String user = null;
                try{
                    user = FirebaseAuth.getInstance()
                            .getCurrentUser().getEmail();
                } catch (Exception e){
                    user = "dummyUser";
                }
                user = user;
                FirebaseFirestore.getInstance()
                        .collection(user)
                        .document(t.getId())
                        .update("completed", cb_lvcompleted.isChecked());
            });

        }
        private void copyToClipboard(String toCopy){
            ClipboardManager clipboardManager = (ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            String clip = toCopy;
            ClipData clipData = ClipData.newPlainText("task", clip);
            clipboardManager.setPrimaryClip(clipData);
        }
    }
}
