package com.example.recipesbook.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesbook.Model.Users;
import com.example.recipesbook.databinding.ItemFollowUserBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersAdapter  extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>  {
List<Users> users;
    OnClickListener listener;

    public UsersAdapter(List<Users> users, OnClickListener listener) {
        this.users = users;
        this.listener = listener;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFollowUserBinding binding = ItemFollowUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UsersAdapter.UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users users1 = users.get(position);
        holder.binding.userName.setText(users1.getName());
        holder.binding.userCountry.setText(users1.getCountry());
        Picasso.get().load(users1.getImageUrl()).into(holder.binding.userImage);
        holder.itemView.setOnClickListener(v -> {
            listener.onClick(users1.getUserId());
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemFollowUserBinding binding;

        public UserViewHolder(@NonNull ItemFollowUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }



    public interface OnClickListener {
        void onClick(String userId);
    }
}
