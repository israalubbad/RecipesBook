package com.example.recipesbook.Activitys;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipesbook.Fragment.BookMarkFragment;
import com.example.recipesbook.Fragment.HomeFragment;
import com.example.recipesbook.Fragment.ProfileFragment;
import com.example.recipesbook.Fragment.SearchFragment;
import com.example.recipesbook.Model.Notification;
import com.example.recipesbook.R;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
/*
 * Main activity of the Recipes Book app.
 * - Sets up the main layout and bottom navigation with fragments (Home, Search, Post, Bookmarks, Profile).
 * - Retrieves and stores the device's FCM token for push notifications.
 * - Requests permission is required to show system notifications.
 * - Fetches unread notifications from Firestore.
 * - Displays system notifications for each unread notification.
 * - Handles navigation to specific screens (RecipeDetails or Profile) when a notification is clicked.
 */

public class Main extends AppCompatActivity {
    ActivityMainBinding binding;
    ArrayList<Notification> notificationsList = new ArrayList<>();
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //    EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        auth = FirebaseAuth.getInstance();
        Utils.USER_ID = auth.getCurrentUser().getUid();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                HomeFragment.newInstance()).commit();

        setupFirebaseToken();

        requestNotificationPermission();

        binding.bottomNavigationView.getMenu().getItem(2).setEnabled(false);
        binding.bottomNavigationView.setBackground(null);
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(Main.this ,PostRecipe.class);
            startActivity(intent);
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            bottomNavigation(item);
            return true;
        });

        showNotifications();

    }

    private void setupFirebaseToken() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String savedToken = prefs.getString("fcmToken", null);

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (savedToken == null || !savedToken.equals(token)) {
                FirebaseFirestore.getInstance().collection("users")
                        .document(FirebaseAuth.getInstance().getUid())
                        .update("fcmToken", token);

                prefs.edit().putString("fcmToken", token).apply();
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void bottomNavigation(MenuItem item) {
        if (item.getItemId() == R.id.homeItem) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                    HomeFragment.newInstance()).commit();
        } else if (item.getItemId() == R.id.searchItem) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                    SearchFragment.newInstance()).commit();

        } else if (item.getItemId() == R.id.bookMarkItem) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                    BookMarkFragment.newInstance()).commit();
        } else if (item.getItemId() == R.id.profileItem) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                    ProfileFragment.newInstance()).commit();
        }


    }

    private void showNotifications() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(Utils.USER_ID)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (DocumentSnapshot doc : snapshot) {
                        Notification notification = doc.toObject(Notification.class);
                        if (!notification.isRead()) {
                            notificationsList.add(notification);
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(Utils.USER_ID)
                                    .collection("notifications")
                                    .document(doc.getId())
                                    .update("read", true);

                            showSystemNotification(notification.getTitle(), notification.getMessage(), notification.getRecipeId(), notification.getUserId(), notification.getNotificationId());
                        }
                    }

                });
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void showSystemNotification(String title, String message, String recipeId, String userId, String notificationId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "recipe_channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Recipe Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent;

        if (recipeId != null) {
            intent = new Intent(this, RecipeDetails.class);
            intent.putExtra("recipeId", recipeId);
            intent.putExtra("notificationId", notificationId);
        } else if (userId != null) {
            intent = new Intent(this, UserProfile.class);
            intent.putExtra("userId", userId);
            intent.putExtra("notificationId", notificationId);
        } else {
            intent = new Intent(this, Main.class);
        }

        PendingIntent pi = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo_recipe)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), builder.build());
    }
}
