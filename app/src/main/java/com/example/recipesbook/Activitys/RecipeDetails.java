package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;


import com.example.recipesbook.Adapter.ViewPagerAdapter;
import com.example.recipesbook.Fragment.AboutRecipeFragment;
import com.example.recipesbook.Fragment.CommentsFragment;
import com.example.recipesbook.R;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.ActivityRecipeDetailsBinding;
import com.example.recipesbook.databinding.RatingBottomSheetBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * RecipeDetails Activity
 * This activity displays detailed information about  selected recipe.
 * <p>
 * Features:
 * - Loads and displays recipe details: title, description, image, category, calories, and cooking time.
 * - Shows recipe owner's action menu (edit/delete) if the current user is the creator.
 * - Allows users to bookmark or remove bookmark from a recipe.
 * - Allows users to rate the recipe through  bottom sheet dialog.
 * - Loads and calculates the average recipe rating.
 * - Uses ViewPager and TabLayout to show tabs: About & Comments.
 */

public class RecipeDetails extends AppCompatActivity {
    ActivityRecipeDetailsBinding binding;
    String recipeId;
    FirebaseFirestore firestore;
    RatingBottomSheetBinding sheetBinding;
    DocumentReference bookmarkRefBookMark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseFirestore.getInstance();
        recipeId = getIntent().getStringExtra("recipeId");
        binding.moreIV.setVisibility(View.GONE);
        showDetailsRecipe();


        // book mark
        bookmarkRefBookMark = firestore.collection("users").document(Utils.USER_ID)
                .collection("bookmarks").document(recipeId);


        Utils.loadBookMarK(recipeId, binding.bookMarkIV);


        binding.bookMarkIV.setOnClickListener(v -> {
            Utils.bookmark(recipeId, binding.bookMarkIV, RecipeDetails.this);

        }
        );


        binding.ratingIV.setOnClickListener(v -> {
            showRatingBottomSheet();

        });


        binding.ivBack.setOnClickListener(v -> {
            finish();
        });


    }


    private void showDetailsRecipe() {
        firestore.collection("recipe")
                .document(recipeId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            RecipeModel recipe = task.getResult().toObject(RecipeModel.class);
                            if (recipe != null) {
                                if (recipe.getUserId() != null && recipe.getUserId().equals(Utils.USER_ID)) {
                                    binding.moreIV.setVisibility(View.VISIBLE);
                                }

                                binding.moreIV.setOnClickListener(v -> {
                                    Utils.showRecipePopupMenu(v.getContext(), binding.moreIV, recipe,
                                            () -> finish(),
                                            () -> {
                                                Intent intent = new Intent(RecipeDetails.this, PostRecipe.class);
                                                intent.putExtra("recipeId", recipe.getRecipeId());
                                                startActivity(intent);
                                            }
                                    );

                                });

                                binding.categoryRecipeTV.setText(recipe.getCategory());
                                binding.nameRecipeTV.setText(recipe.getTitle());
                                binding.descriptionRecipeET.setText(recipe.getDescription());
                                binding.caloriesRecipeTV.append(recipe.getCalories() + "kcal");
                                binding.timeRecipeTV.setText(String.valueOf(recipe.getCookingTime()));
                                Picasso.get().load(recipe.getImageUrl()).into(binding.imageRecipeTV);
                                // load Average Rating to recipe
                                loadAverageRating();

                                // tab
                                loadTabLayout();

                            } else {
                                Toast.makeText(RecipeDetails.this, "Recipe not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }


                });

    }


    private void loadTabLayout() {
        ArrayList<String> tabs = new ArrayList<>();
        ArrayList<Fragment> fragments = new ArrayList<>();
        tabs.add("About");
        fragments.add(AboutRecipeFragment.newInstance(recipeId));
        tabs.add("Comment");
        fragments.add(CommentsFragment.newInstance(recipeId));


        ViewPagerAdapter adapter = new ViewPagerAdapter(RecipeDetails.this, fragments);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(fragments.size());

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(tabs.get(position));
        }).attach();

    }



    private void showRatingBottomSheet() {
        RatingBottomSheetBinding sheetBinding = RatingBottomSheetBinding.inflate(LayoutInflater.from(getBaseContext()));
        BottomSheetDialog dialog = new BottomSheetDialog(RecipeDetails.this);
        dialog.setContentView(sheetBinding.getRoot());
        dialog.show();

        firestore.collection("recipe").document(recipeId)
                .collection("rating").document(Utils.USER_ID)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double rating = documentSnapshot.getDouble("rating");
                        sheetBinding.ratingBar.setRating(rating.floatValue());
                    }
                });

        sheetBinding.btnSubmit.setOnClickListener(v -> {
            float rating = sheetBinding.ratingBar.getRating();

            if (rating != 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("userId", Utils.USER_ID);
                data.put("recipeId", recipeId);
                data.put("rating", rating);

                firestore.collection("recipe").document(recipeId)
                        .collection("rating").document(Utils.USER_ID)
                        .set(data).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RecipeDetails.this, "Rating added successfully", Toast.LENGTH_SHORT).show();
                                loadAverageRating();
                            } else {
                                Toast.makeText(RecipeDetails.this, "Failed to add rating", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        });
            } else {
                Toast.makeText(RecipeDetails.this, "Please select a rating", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadAverageRating() {
        firestore.collection("recipe")
                .document(recipeId)
                .collection("rating")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int total = 0;
                        int count = 0;
                        for (DocumentSnapshot doc : task.getResult()) {
                            total += doc.getDouble("rating").intValue();
                            count++;
                        }
                        double avgValue = (count > 0) ? ((double) total / count) : 0.0;
                        int avgInt = (int) avgValue;

                        binding.ratingRecipeTV.setText(String.format("%.1f", avgValue));
                        firestore.collection("recipe").document(recipeId)
                                .update("evaluation", avgInt);

                    }
                });
    }


}