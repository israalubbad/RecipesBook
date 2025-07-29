package com.example.recipesbook.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.recipesbook.Activitys.AllRecipes;
import com.example.recipesbook.Adapter.RecipeAdapter;
import com.example.recipesbook.Activitys.RecipeDetails;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.FragmentViewRecipeBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
/*
 * ViewRecipeFragment
 * This fragment displays two horizontal lists of recipes:
 * 1. All available recipes.
 * 2. Recipes from users that the current user follows.
 *
 * It fetches data from Firebase Firestore asynchronously using Utils helper methods,
 * and sets up RecyclerViews with RecipeAdapter to display the recipes.
 *
 * The fragment also handles user interactions:
 * - Clicking on recipe items to open details.
 * - Clicking on "See All" titles to open the AllRecipes activity filtered accordingly.
 */

public class ViewRecipeFragment extends Fragment implements RecipeAdapter.onClickListener {
    FirebaseFirestore firestore;
    RecipeAdapter adapter;
    List<RecipeModel> recipes;
    FragmentViewRecipeBinding binding;

    public ViewRecipeFragment() {

    }


    public static ViewRecipeFragment newInstance() {
        ViewRecipeFragment fragment = new ViewRecipeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        firestore = FirebaseFirestore.getInstance();
        recipes = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewRecipeBinding.inflate(getLayoutInflater());

        binding.seeAllFollowers.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AllRecipes.class);
            intent.putExtra("All", "AllFollowers");
            startActivity(intent);
        });

        binding.seeAllRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AllRecipes.class);
            intent.putExtra("All", "AllRecipe");
            startActivity(intent);
        });

        // Load all recipes and followers' recipes

        Utils.showAllRecipes(new Utils.RecipesCallback() {
            @Override
            public void onSuccess(List<RecipeModel> recipesList) {
                adapter = new RecipeAdapter(recipesList, "home", ViewRecipeFragment.this);
                binding.recyclerviewAllRecipe.setAdapter(adapter);
                binding.recyclerviewAllRecipe.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));
            }

            @Override
            public void onFailure(Exception e) {
            }
        });

        showListOfFollowers();

        return binding.getRoot();
    }

    private void showListOfFollowers() {
        firestore.collection("users").document(Utils.USER_ID).collection("following").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> userIds = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            String id = doc.getString("userId");
                            if (id != null) userIds.add(id);
                        }
                        if (!userIds.isEmpty()) {
                            Utils.showFollowersRecipes(userIds, new Utils.RecipesCallback() {
                                @Override
                                public void onSuccess(List<RecipeModel> recipesList) {
                                    adapter = new RecipeAdapter(recipesList, "home", ViewRecipeFragment.this);
                                    binding.recyclerviewAllFollowers.setAdapter(adapter);
                                    binding.recyclerviewAllFollowers.setLayoutManager(
                                            new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });
                        }
                    }
                });

    }


    // Handle clicks on recipe item
    @Override
    public void onClick(String recipeId) {
        Intent intent = new Intent(getContext(), RecipeDetails.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);

    }


    @Override
    public void onResume() {
        super.onResume();
        showListOfFollowers();
    }
}