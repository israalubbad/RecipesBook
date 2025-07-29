package com.example.recipesbook.Adapter;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipesbook.Model.Comments;
import com.example.recipesbook.Model.Users;
import com.example.recipesbook.R;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.ItemCommentBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {
    List<Comments> commentsList;

    public CommentsAdapter(List<Comments> commentsList) {
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        Comments comment = commentsList.get(position);
        holder.binding.commentContent.setText(comment.getComment());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String currentDateTime = dateFormat.format(Long.parseLong(comment.getTimestamp()) * 1000);
            holder.binding.commentTimestamp.setText(currentDateTime);
        } catch (Exception e) {
            holder.binding.commentTimestamp.setText("Unknown Time");
        }

        loadUserData(holder, comment);

        if (comment.getUserId().equals(Utils.USER_ID)) {
            holder.binding.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btnDelete.setVisibility(View.GONE);
        }

        holder.binding.btnDelete.setOnClickListener(v -> {
            Utils.showPopupMenuWithIcons(
                    v.getContext(),
                    holder.binding.btnDelete,
                    R.menu.delete_menu,
                    item -> {
                        if (item.getItemId() == R.id.deleteItem) {
                            FirebaseFirestore.getInstance()
                                    .collection("recipe").document(comment.getRecipeId())
                                    .collection("comments").document(comment.getCommentId())
                                    .delete()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            commentsList.remove(position);
                                            notifyItemRemoved(position);
                                            Toast.makeText(v.getContext(), "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(v.getContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            return true;
                        }
                        return false;
                    }
            );
        });

    }

    private void loadUserData(CommentsViewHolder holder, Comments comment) {
        FirebaseFirestore.getInstance().collection("users")
                .document(comment.getUserId()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Users user = task.getResult().toObject(Users.class);
                        if (user != null) {
                            holder.binding.commentUsername.setText(user.getName());
                            Picasso.get().load(user.getImageUrl()).into(holder.binding.userImageIV);
                        }
                    } else {
                        Toast.makeText(holder.binding.getRoot().getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;

        public CommentsViewHolder(@NonNull ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
