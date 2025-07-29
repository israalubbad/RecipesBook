package com.example.recipesbook;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.recipesbook.Adapter.RecipeAdapter;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.Model.Users;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
/*
 * Utils is a helper class that contains various static utility methods used throughout the app.
 * It includes functionality for:
 * - Uploading images to Cloudinary
 * - Managing bookmarks and recipe data from Firestore
 * - Handling user follow/unfollow logic
 * - Displaying popup menus
 * - Sending notifications
 * - Loading user profiles and recipe lists
 *
 * This class centralizes common logic to keep other parts of the codebase clean and reusable.
 */
public class Utils {
    public static String USER_ID = "";
    static FirebaseFirestore firestore  =FirebaseFirestore.getInstance();;

    // convert the bitmap to byte
    static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }


    public interface UploadImageCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    //uploading image in Utils to be clean code without duplicating
    public static void uploadImageToCloudinary(String folderName, Bitmap bitmap, Context context,String id, UploadImageCallback callback) {
        MediaManager.get().upload(bitmapToByteArray(bitmap))
                .option("folder", folderName)
                .option("public_id", folderName + "_" +id)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(context, "Uploading image please wait", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show();
                        callback.onSuccess(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(context, "Upload failed " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        callback.onFailure(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }



    public static void sendNotificationToUser(String userId, String recipeId, String fromUserId, String title, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> nov = new HashMap<>();
        nov.put("title", title);
        nov.put("message", message);
        nov.put("recipeId", recipeId);
        nov.put("userId", fromUserId);
        nov.put("notificationId", UUID.randomUUID().toString());
        nov.put("timestamp", FieldValue.serverTimestamp());
        nov.put("read", false);

        db.collection("users").document(userId).collection("notifications").add(nov);
    }


    public interface RecipesCallback {
        void onSuccess(List<RecipeModel> recipes);
        void onFailure(Exception e);
    }

    // Fetches all recipes from Firestore and displays them in recyclerviewAllRecipe.
    public static void showAllRecipes(RecipesCallback callback) {
        firestore.collection("recipe").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RecipeModel> recipes = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            recipes.add(doc.toObject(RecipeModel.class));
                        }
                        callback.onSuccess(recipes);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }


    // Fetches recipes of users that the current user follows and displays them in recyclerviewAllFollowers.
    public static void showFollowersRecipes(List<String> userIds, RecipesCallback callback) {
        List<RecipeModel> allRecipes = new ArrayList<>();
        for (String uid : userIds) {
            firestore.collection("recipe").whereEqualTo("userId", uid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                allRecipes.add(doc.toObject(RecipeModel.class));
                            }
                            callback.onSuccess(allRecipes);
                        }

                    });
        }
    }


    public static void showRecipePopupMenu(Context context, View anchorView, RecipeModel recipe, Runnable onDelete, Runnable onEdit) {
        PopupMenu popupMenu = new PopupMenu(context, anchorView);
        popupMenu.inflate(R.menu.user_menu);

        try {
            // Enable icon showing
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceShowIcon.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.editeItem) {
                if (onEdit != null) onEdit.run();
                return true;
            } else if (itemId == R.id.deleteItem) {
                FirebaseFirestore.getInstance().collection("recipe")
                        .document(recipe.getRecipeId())
                        .delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
                                if (onDelete != null) onDelete.run();
                            } else {
                                Toast.makeText(context, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                            }
                        });
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    public static void showPopupMenuWithIcons(Context context, View anchor, int menuRes, PopupMenu.OnMenuItemClickListener listener) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.inflate(menuRes);

        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceShowIcon.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popupMenu.setOnMenuItemClickListener(listener);
        popupMenu.show();
    }

    public static void loadBookMarK(String recipeId, ImageView bookMarkIcon) {
        firestore.collection("users").document(Utils.USER_ID)
                .collection("bookmarks").document(recipeId)
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        bookMarkIcon.setImageResource(R.drawable.bookmark);
                    } else {
                        bookMarkIcon.setImageResource(R.drawable.unbookmark);
                    }
                });
    }

    public static void bookmark(String recipeId, ImageView bookMarkIV, Context context) {
        DocumentReference bookmarkRef = firestore
                .collection("users")
                .document(Utils.USER_ID)
                .collection("bookmarks")
                .document(recipeId);

        bookmarkRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                bookmarkRef.delete();
                bookMarkIV.setImageResource(R.drawable.unbookmark);
                Toast.makeText(context, "Recipe unbookmarked successfully", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("userId", Utils.USER_ID);
                data.put("recipeId", recipeId);
                bookmarkRef.set(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookMarkIV.setImageResource(R.drawable.bookmark);
                        Toast.makeText(context, "Recipe bookmarked successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    public static void loadMyRecipe(String userId,TextView recipesCountTV, RecyclerView recipesRecyclerView, Context context, RecipeAdapter.onClickListener listener ,String screen) {
        firestore.collection("recipe")
                .whereEqualTo("userId",userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ArrayList<RecipeModel> recipes = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            RecipeModel recipe = doc.toObject(RecipeModel.class);
                            recipes.add(recipe);
                        }

                        recipesCountTV.setText(String.valueOf(recipes.size()));

                        RecipeAdapter adapter = new RecipeAdapter(recipes, screen, listener);
                        recipesRecyclerView.setAdapter(adapter);
                        recipesRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                    }
                });
    }


    public static void loadUserData(String userId,TextView nameTV, TextView bioTV, ImageView profileImg) {
        firestore.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Users users = task.getResult().toObject(Users.class);
                nameTV.setText(users.getName());
                bioTV.setText(users.getBio());
                Picasso.get().load(users.getImageUrl())
                        .placeholder(R.drawable.profile_image)
                        .into(profileImg);
            }
        });
    }
    public static void loadCountOfFollowers(String userId,TextView followersCountTV) {

        firestore.collection("users").document(userId).collection("followers")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        followersCountTV.setText(String.valueOf(count));
                    }
                });
    }

    public static void loadCountOfFollowings(String userId,TextView followingCountTV) {

        firestore.collection("users").document(userId).collection("following")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        followingCountTV.setText(String.valueOf(count));
                    }
                });
    }

    public static void followUser(Activity activity, String authorId, String userId, Runnable onSuccess) {
        if (authorId.equals(userId)) {
            Toast.makeText(activity, "You can't follow yourself", Toast.LENGTH_SHORT).show();
            return;
        }
       Map<String, Object> data = new HashMap<>();
        data.put("userId", authorId);
        data.put("followedAt", FieldValue.serverTimestamp());

        firestore.collection("users").document(userId)
                .collection("following").document(authorId)
                .set(data)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        firestore.collection("users").document(authorId)
                                .collection("followers").document(userId)
                                .set(data)
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Toast.makeText(activity, "Followed Successfully", Toast.LENGTH_SHORT).show();

                                        firestore.collection("users").document(Utils.USER_ID)
                                                .get()
                                                .addOnSuccessListener(doc -> {
                                                    String name = doc.getString("name");

                                                    Utils.sendNotificationToUser(authorId, null, Utils.USER_ID, "New Follower", name + " started following you!");

                                                    if (onSuccess != null) {
                                                        onSuccess.run();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }


    public static void unfollowUser(Activity activity, String authorId, String userId, Runnable onSuccess) {

        firestore.collection("users").document(userId)
                .collection("following").document(authorId)
                .delete()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        firestore.collection("users").document(authorId)
                                .collection("followers").document(userId)
                                .delete()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Toast.makeText(activity, "Unfollowed Successful", Toast.LENGTH_SHORT).show();
                                        if (onSuccess != null) {
                                            onSuccess.run();
                                        }
                                    }
                                });
                    }
                });
    }


    public static void updateFollowButton(Context context, boolean isFollowing, Button btnFollow) {
        if (isFollowing) {
           btnFollow.setText("Unfollow");
            btnFollow.setBackgroundColor(context.getResources().getColor(R.color.orange_primary));
           btnFollow.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundColor(context.getResources().getColor(R.color.orange_light));
            btnFollow.setTextColor(context.getResources().getColor(R.color.orange_primary));
        }
    }


}
