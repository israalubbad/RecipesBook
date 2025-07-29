package com.example.recipesbook.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.recipesbook.Activitys.RecipeDetails;
import com.example.recipesbook.Adapter.RecipeAdapter;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.databinding.FragmentCategoryBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment implements RecipeAdapter.onClickListener {
    RecipeAdapter adapter;
    private static final String ARG_CATEGORY = "category";
    FirebaseFirestore firestore;
    private String category;
    List<RecipeModel> recipes;
    FragmentCategoryBinding binding;

    public CategoryFragment() {

    }


    public static CategoryFragment newInstance(String category) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
        }

        firestore = FirebaseFirestore.getInstance();
        recipes = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        loadRecipeByCategory();

        return binding.getRoot();
    }

    private void loadRecipeByCategory() {
        firestore.collection("recipe").whereEqualTo("category", category).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override

            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    recipes.clear();
                    for (DocumentSnapshot doc : task.getResult()) {
                        RecipeModel recipe = doc.toObject(RecipeModel.class);
                        recipes.add(recipe);
                    }
                    adapter = new RecipeAdapter(recipes, "category", CategoryFragment.this);
                    binding.allRecipesRv.setAdapter(adapter);
                    binding.allRecipesRv.setLayoutManager(new LinearLayoutManager(getContext()));
                }
            }
        });
    }

    @Override
    public void onClick(String recipeId) {
        Intent intent = new Intent(getContext(), RecipeDetails.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);
    }


}