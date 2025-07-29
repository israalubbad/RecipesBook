package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.recipesbook.Adapter.RecipeAdapter;
import com.example.recipesbook.R;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.ActivityAllRecipesBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
/*
 * AllRecipes Activity
 * This activity displays a list of recipes based on the intent extra passed:
 * - "AllRecipe": shows all recipes available in Firestore.
 * - "AllFollowers": shows recipes from users that the current user follows.
 *
 * It uses Firebase Firestore to fetch data asynchronously via Utils helper methods,
 * and displays the recipes in a RecyclerView using RecipeAdapter.
 */

public class AllRecipes extends AppCompatActivity implements RecipeAdapter.onClickListener {
    ActivityAllRecipesBinding binding;
    FirebaseFirestore firestore;
    ArrayList<RecipeModel> recipes = new ArrayList<>();
    RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAllRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firestore = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        String all = intent.getStringExtra("All");

        if (all.equals("AllRecipe")) {
            binding.titleTv.setText("All Recipes");
            allRecipes();
        } else if (all.equals("AllFollowers")) {
            binding.titleTv.setText("All Followers Recipe");
            allFollowersRecipes();
        }

        binding.backBT.setOnClickListener(v -> {
            finish();
        });

    }

    private void allFollowersRecipes() {
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
                                    adapter = new RecipeAdapter(recipesList, "all", AllRecipes.this);
                                    binding.recyclerView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    binding.recyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), 2, GridLayoutManager.VERTICAL, false));
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });
                        }
                    }
                });
    }

    void allRecipes() {
        Utils.showAllRecipes(new Utils.RecipesCallback() {
            @Override
            public void onSuccess(List<RecipeModel> recipesList) {
                adapter = new RecipeAdapter(recipesList, "all", AllRecipes.this);
                binding.recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                binding.recyclerView.setLayoutManager( new GridLayoutManager(getBaseContext(), 2, GridLayoutManager.VERTICAL, false));

            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    @Override
    public void onClick(String recipeId) {
        Intent intent = new Intent(getBaseContext(), RecipeDetails.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);
    }



}