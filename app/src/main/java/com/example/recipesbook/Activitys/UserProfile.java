package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipesbook.Adapter.RecipeAdapter;
import com.example.recipesbook.R;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.ActivityUserProfileBinding;

import com.google.firebase.firestore.FirebaseFirestore;

/*
 * UserProfile Activity
 *
 * Features:
 * - Profile image, name, and bio.
 * - Followers count, following count, and number of recipes.
 * - Grid view of the user's posted recipes.
 * - Follow/unfollow functionality with dynamic update of follow button and follower count.
 */

public class UserProfile extends AppCompatActivity implements RecipeAdapter.onClickListener {
    ActivityUserProfileBinding binding;
    FirebaseFirestore fireStore;
    String userId;
    boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fireStore = FirebaseFirestore.getInstance();
        if(getIntent().getStringExtra("userId") != null) {
            userId = getIntent().getStringExtra("userId");
        }
        // set user details
        Utils.loadUserData(userId,binding.userNameTV, binding.bioUserTV, binding.profileImg);

        Utils.loadCountOfFollowers(userId,binding.followersCountTV);

        Utils.loadCountOfFollowings(userId,binding.followingCountTV);

        FollowButton(userId);

        // get all recipe of user
        Utils.loadMyRecipe(userId, binding.recipesCountTV, binding.recipesRecyclerView,getBaseContext(), UserProfile.this,"userProfile");

        binding.backIV.setOnClickListener(v -> {
            finish();
        });


    }

    private void FollowButton(String authorId) {
        fireStore.collection("users").document(Utils.USER_ID)
                .collection("following").document(authorId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    isFollowing = snapshot.exists();
                    Utils.updateFollowButton(this, isFollowing, binding.btnFollow);

                    binding.btnFollow.setOnClickListener(v -> {
                        if (isFollowing) {
                            Utils.unfollowUser(this, authorId, Utils.USER_ID, () -> {
                                isFollowing = false;
                                Utils.updateFollowButton(this, false, binding.btnFollow);
                                Utils.loadCountOfFollowers(userId, binding.followersCountTV);
                            });
                        } else {
                            Utils.followUser(this, authorId, Utils.USER_ID, () -> {
                                isFollowing = true;
                                Utils.updateFollowButton(this, true, binding.btnFollow);
                                Utils.loadCountOfFollowers(userId, binding.followersCountTV);
                            });
                        }
                    });
                });
    }


    @Override
    public void onClick(String recipeId) {
        Intent intent = new Intent(this, RecipeDetails.class);
        intent.putExtra("recipeId", recipeId);
        intent.putExtra("activity", "profile");
        startActivity(intent);
    }

}