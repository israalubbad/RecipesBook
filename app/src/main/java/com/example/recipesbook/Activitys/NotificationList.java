package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesbook.Adapter.NotificationsAdapter;
import com.example.recipesbook.Model.Notification;
import com.example.recipesbook.R;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.ActivityNotificationBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
/*
 * NotificationList Activity
 *
 * This Activity displays  list of notifications specific
 *
 * Features:
 * - Loads and displays notifications from Firestore under the user's "notifications" collection.
 * - Allows users to swipe to delete notifications.
 * - When a user clicks on notification:
 *    >> If the notification is related to recipe, it navigates to the RecipeDetails screen.
 *    >> If the notification is related to  user, it navigates to the UserProfile screen.
 */
public class NotificationList extends AppCompatActivity  implements  NotificationsAdapter.OnClickListener{

    ActivityNotificationBinding binding;
    List<Notification> list = new ArrayList<>();
    NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRecyclerView();

        loadNotifications();
        binding.backButton.setOnClickListener(v -> {finish(); });
    }
    private void setupRecyclerView() {
        adapter = new NotificationsAdapter(list,this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Notification removedNotification = list.get(position);

                list.remove(position);
                adapter.notifyItemRemoved(position);
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(Utils.USER_ID)
                        .collection("notifications")
                        .document(removedNotification.getNotificationId())
                        .delete();

                Toast.makeText(NotificationList.this, "Notification has been removed ", Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.recyclerView);
    }

    private void loadNotifications() {
        if (Utils.USER_ID == null || Utils.USER_ID.trim().isEmpty()) {
            Toast.makeText(this, "User ID is null or empty", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(Utils.USER_ID)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    list.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            list.add(notification);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onClickNotificationRecipe(String recipeId) {
        if (recipeId != null) {
            Intent intent = new Intent(NotificationList.this, RecipeDetails.class);
            intent.putExtra("recipeId", recipeId);
            startActivity(intent);
        }
    }

    @Override
    public void onClickNotificationUser(String userId) {
        if (userId != null) {
            Intent intent = new Intent(NotificationList.this, UserProfile.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        }
    }
}
