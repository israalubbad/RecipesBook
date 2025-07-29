package com.example.recipesbook.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.recipesbook.Activitys.UserProfile;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.Model.Users;

import com.example.recipesbook.R;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.FragmentAboutRecipeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
/*
 * AboutRecipeFragment
 *
 * Fragment that displays detailed information about  selected recipe, including
 * ingredients, preparation steps, and author information.
 *
 * Features:
 * - Loads and displays recipe data from Firestore.
 * - Shows author information including name and profile image.
 * - Allows users to follow or unfollow the recipe's author.
 * - Updates follow button state based on current follow status.
 * - Opens video tutorial link if provided.
 * - Navigates to author's profile on image or name click.
 *
 */

public class AboutRecipeFragment extends Fragment {
    private static final String ARG_RECIPEID = "RecipeId";
    FragmentAboutRecipeBinding binding;
    private String recipeId;
    FirebaseFirestore firestore;
    RecipeModel recipe;
    private boolean isFollowing = false;


    public AboutRecipeFragment() {
        // Required empty public constructor
    }

    public static AboutRecipeFragment newInstance(String recipeId) {
        AboutRecipeFragment fragment = new AboutRecipeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPEID, recipeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipeId = getArguments().getString(ARG_RECIPEID);
        }
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAboutRecipeBinding.inflate(inflater, container, false);
        loadRecipeData();

        return binding.getRoot();

    }

    private void loadRecipeData() {
        firestore.collection("recipe").document(recipeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    recipe = task.getResult().toObject(RecipeModel.class);

                    if (recipe == null) return;

                    String authorId = recipe.getUserId();
                    FollowButton(authorId);

                    binding.stepsTV.setText(recipe.getSteps());
                    binding.ingredientsTV.setText(recipe.getIngredients());
                    binding.VideoUrlBT.setOnClickListener(v -> {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.getVideUrl())));
                    });

                    firestore.collection("users").document(authorId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Users user = task.getResult().toObject(Users.class);
                                if (user != null) {
                                    binding.tvAuthor.setText(user.getName());
                                    Picasso.get().load(user.getImageUrl()).into(binding.userImageIV);
                                }
                            }
                        }
                    });



                    binding.ownerLayout.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), UserProfile.class);
                        intent.putExtra("userId", authorId);
                        startActivity(intent);
                    });
                }
            }
        });
    }
    private void FollowButton(String authorId) {
        firestore.collection("users").document(Utils.USER_ID)
                .collection("following").document(authorId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    isFollowing = snapshot.exists();
                    Utils.updateFollowButton(requireContext(), isFollowing, binding.btnFollow);

                    binding.btnFollow.setOnClickListener(v -> {
                        if (isFollowing) {
                            Utils.unfollowUser(requireActivity(), authorId, Utils.USER_ID, () -> {
                                isFollowing = false;
                                Utils.updateFollowButton(requireContext(), false, binding.btnFollow);

                            });
                        } else {
                            Utils.followUser(requireActivity(), authorId, Utils.USER_ID, () -> {
                                isFollowing = true;
                                Utils.updateFollowButton(requireContext(), true, binding.btnFollow);

                            });
                        }
                    });
                });
    }


}
