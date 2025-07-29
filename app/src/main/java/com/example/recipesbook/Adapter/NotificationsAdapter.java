package com.example.recipesbook.Adapter;

import android.view.LayoutInflater;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesbook.Model.Notification;
import com.example.recipesbook.databinding.NotificationItemBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    List<Notification> navlist;
    OnClickListener listener;
    public NotificationsAdapter(List<Notification> navlist, OnClickListener listener) {
        this.navlist = navlist;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NotificationItemBinding binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationsAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder view, int i) {
        Notification n = navlist.get(i);
        view.binding.title.setText(n.title);
        view.binding.message.setText(n.message);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault());
        String formattedTime = sdf.format(n.getTimestamp().toDate());
        view.binding.time.setText(formattedTime);

            view.itemView.setOnClickListener(v -> {
                if (n.getRecipeId() != null && !n.getRecipeId().isEmpty()) {
                    listener.onClickNotificationRecipe(n.getRecipeId());
                } else if (n.getUserId() != null && !n.getUserId().isEmpty()) {
                    listener.onClickNotificationUser(n.getUserId());
                }
            });



    }

    @Override
    public int getItemCount() {
        return navlist.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        NotificationItemBinding binding;

        ViewHolder(@NonNull NotificationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    public interface OnClickListener {
        void onClickNotificationRecipe(String recipeId);
        void onClickNotificationUser(String userId);
    }
}

