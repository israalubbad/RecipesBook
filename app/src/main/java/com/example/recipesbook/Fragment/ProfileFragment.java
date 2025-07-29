package com.example.recipesbook.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.recipesbook.Activitys.FollowList;
import com.example.recipesbook.Activitys.ManageProfile;
import com.example.recipesbook.Activitys.RecipeDetails;
import com.example.recipesbook.Activitys.SettingsActivity;
import com.example.recipesbook.Adapter.RecipeAdapter;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.Model.Users;
import com.example.recipesbook.R;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.FragmentProfileBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/*
 * ProfileFragment
 *
 *  Features:
 * Fragment that displays the current user's profile including:
 * - Profile image, name, and bio.
 * - Number of followers, following, and recipes.
 * - Grid of user's posted recipes.
 * - Navigation to: Edit Profile, Settings, Followers, and Following list.
 *
 */

public class ProfileFragment extends Fragment implements RecipeAdapter.onClickListener {
    FragmentProfileBinding binding;
    FirebaseFirestore fireStore;

    public ProfileFragment() {
    }


    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        fireStore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // set user details
        Utils.loadUserData(Utils.USER_ID,binding.userNameTV, binding.bioUserTV, binding.profileImg);

        Utils.loadCountOfFollowers(Utils.USER_ID,binding.followersCountTV);

        Utils.loadCountOfFollowings(Utils.USER_ID,binding.followingCountTV);


        // get all recipe of user
        Utils.loadMyRecipe(Utils.USER_ID, binding.recipesCountTV, binding.recipesRecyclerView, getContext(), this, "profile");


        binding.manageProfileBT.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ManageProfile.class);
            startActivity(intent);
        });

        binding.followingLL.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowList.class);
            intent.putExtra("follow", 0);
            startActivity(intent);
        });

        binding.followersLL.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowList.class);
            intent.putExtra("follow", 1);
            startActivity(intent);
        });

        binding.iconSettingsIV.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });


        return binding.getRoot();
    }





    @Override
    public void onClick(String recipeId) {
        Intent intent = new Intent(getContext(), RecipeDetails.class);
        intent.putExtra("recipeId", recipeId);
        intent.putExtra("activity", "profile");
        startActivity(intent);
    }


}