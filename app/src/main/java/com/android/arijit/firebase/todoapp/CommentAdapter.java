package com.android.arijit.firebase.todoapp;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentRowViewHolder> {
    private Context context;
    private ArrayList<Comment> commentList;
    private String todoId;
    private String TAG = "CommentAdapter";

    public CommentAdapter(Context context, ArrayList<Comment> list, String todoId){
        this.commentList = list;
        this.context = context;
        this.todoId = todoId;
    }


    @NonNull
    @Override
    public CommentRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recordView = LayoutInflater.from(context).inflate(R.layout.row_layout_comments,parent, false);
        return new CommentRowViewHolder(recordView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentRowViewHolder holder, int position) {
        holder.bindData(commentList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class CommentRowViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener
    {
        private int position;
        private TextView tvComment, tvDate;
        public CommentRowViewHolder(@NonNull View itemView) {
            super(itemView);
            tvComment = (TextView) itemView.findViewById(R.id.tv_row_comment);
            tvDate = (TextView) itemView.findViewById(R.id.tv_cmdate_row);
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
            switch (item.getItemId())
            {
                case 1:
                    copyToClipboard(commentList.get(position).getCmtext());
                    Snackbar.make(tvComment, "Copied", Snackbar.LENGTH_SHORT)
                            .setAction(null, null)
                            .show();
                    return true;
                case 2:
                    String user = null;
                    try{
                        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    } catch (Exception e){
                        user = "dummyUser";
                    }
                    user = user;
                    FirebaseFirestore.getInstance()
                            .collection("commentCollection")
                            .document(commentList.get(position).getCid())
                            .delete()
                            .addOnFailureListener(e -> {
                                Snackbar.make(tvComment, "Unable to delete", Snackbar.LENGTH_SHORT)
                                        .setAction(null, null).show();
                            });
                    commentList.remove(position);
                    notifyDataSetChanged();
                    Snackbar.make(tvComment, "Delete", Snackbar.LENGTH_SHORT)
                            .setAction(null, null).show();

                    return true;
                default:
                    return false;
            }
        }

        public void bindData(Comment cm, int position){
            tvComment.setText(cm.getCmtext());
            tvDate.setText(cm.getCmdate());
            this.position = position;
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
