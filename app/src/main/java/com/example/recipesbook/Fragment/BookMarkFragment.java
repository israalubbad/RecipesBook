package com.example.recipesbook.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.recipesbook.Activitys.RecipeDetails;
import com.example.recipesbook.Adapter.RecipeAdapter;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.FragmentBookMarkBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BookMarkFragment
 *
 * Fragment that displays all bookmarked recipes of the current user.
 *
 * Features:
 * - Loads bookmarked recipe IDs from Firstore under the current user's bookmarks collection.
 * - Fetches detailed recipe data for each bookmarked recipe.
 * - Displays recipes in a grid layout using RecipeAdapter.
 * - Provides a search functionality to filter bookmarked recipes by title.
 * - Opens RecipeDetails activity when recipe is selected.
 */

public class BookMarkFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener, RecipeAdapter.onClickListener {

    FirebaseFirestore firestore;
    ArrayList<RecipeModel> recipes;
    FragmentBookMarkBinding binding;

    public BookMarkFragment() {
        // Required empty public constructor
    }


    public static BookMarkFragment newInstance() {
        BookMarkFragment fragment = new BookMarkFragment();
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
        binding = FragmentBookMarkBinding.inflate(getLayoutInflater());

        showAllRecipeBookMarks();

        return binding.getRoot();
    }

    private void showAllRecipeBookMarks() {
        recipes.clear();
        firestore.collection("users").document(Utils.USER_ID)
                .collection("bookmarks").get()
                .addOnSuccessListener(task -> {
                    ArrayList<String> recipeIds = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getDocuments()) {
                        String recipeId = doc.getString("recipeId");
                        if (recipeId != null) {
                            recipeIds.add(recipeId);
                        }
                    }
                    if (recipeIds.isEmpty()) {
                        binding.recyclerView.setAdapter(null);
                        return;
                    }

                    AtomicInteger counter = new AtomicInteger(0);
                    for (String recipeId : recipeIds) {
                        firestore.collection("recipe").document(recipeId)
                                .get().addOnSuccessListener(recipeDoc -> {
                                    RecipeModel recipe = recipeDoc.toObject(RecipeModel.class);
                                    if (recipe != null) {
                                        recipes.add(recipe);
                                    }

                                    if (counter.incrementAndGet() == recipeIds.size()) {
                                        RecipeAdapter adapter = new RecipeAdapter(recipes, "bookMark", BookMarkFragment.this);
                                        binding.recyclerView.setAdapter(adapter);
                                        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                                    }
                                });
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