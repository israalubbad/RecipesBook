package com.example.recipesbook.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.recipesbook.Activitys.RecipeDetails;
import com.example.recipesbook.Adapter.RecipeAdapter;
import com.example.recipesbook.Adapter.UsersAdapter;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.Model.Users;
import com.example.recipesbook.R;
import com.example.recipesbook.databinding.BottomSheetFilterBinding;
import com.example.recipesbook.databinding.FragmentSearchBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * SearchFragment
 *
 * Features:
 * - Displays all recipes by default.
 * - Allows users to search recipes by name using  search bar.
 * - Shows recent search history suggestions .
 * - Provides filters via a bottom sheet for:
 *      - Cooking time
 *      - Calories
 *      - Rating (evaluation)
 *      - Category
 * - Updates RecyclerView with search results based on filters and keywords.
 *
 */

public class SearchFragment extends Fragment implements RecipeAdapter.onClickListener {
    FirebaseFirestore firestore;
    List<RecipeModel> recipeList;
    private Map<String, String> filterMap = new HashMap<>();
    FragmentSearchBinding binding;
    private List<ToggleButton> toggleButtons;
    RecipeAdapter adapter;

    private List<ToggleButton> timeButtons;
    private List<ToggleButton> calorieButtons;
    private List<ToggleButton> rateButtons;
    private List<ToggleButton> categoryButtons;

    public SearchFragment() {
        // Required empty public constructor
    }


    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
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
        recipeList = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        binding.searchBar.setCardViewElevation(8);

        binding.filterIV.setOnClickListener(v -> {
            showFilter();

        });

        showAllRecipe();


        binding.searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    searchWithFilterAndQuery("");
                    showAllRecipe();
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                searchWithFilterAndQuery(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });


        binding.searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showAllRecipe();
                } else {
                    searchWithFilterAndQuery(query);
                    //    searchUsers(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchWithFilterAndQuery(s.toString());
                //   searchUsers(s.toString());
            }

        });

        return binding.getRoot();
    }

    private void showAllRecipe() {

        firestore.collection("recipe").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    recipeList.clear();
                    for (DocumentSnapshot doc : task.getResult()) {
                        RecipeModel recipe = doc.toObject(RecipeModel.class);
                        recipeList.add(recipe);
                    }
                    binding.tvResultCount.setText(new StringBuilder().append(recipeList.size()).append(" results").toString());
                    updateRecyclerView(recipeList);
                }
            }
        });
    }

    private void showFilter() {
        BottomSheetFilterBinding sheetBinding = BottomSheetFilterBinding.inflate(LayoutInflater.from(getContext()));
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(sheetBinding.getRoot());
        dialog.show();
        setupFilters(sheetBinding);

        sheetBinding.filtersBT.setOnClickListener(v -> {
            filterMap.clear();
            ToggleButton selectedTimeButton = getCheckedButton(timeButtons);
            if (selectedTimeButton != null && !selectedTimeButton.getText().equals("All")) {
                switch (selectedTimeButton.getText().toString()) {
                    case "≤ 30 min":
                        filterMap.put("cookingTime_max", "30");
                        break;
                    case "≤ 1 hour":
                        filterMap.put("cookingTime_max", "60");
                        break;
                    case "≤ 2 hour":
                        filterMap.put("cookingTime_max", "120");
                        break;
                }
            }

            ToggleButton selectedRateButton = getCheckedButton(rateButtons);
            if (selectedRateButton != null && !selectedRateButton.getText().equals("All")) {
                String rateSelected = selectedRateButton.getText().toString();
                filterMap.put("evaluation", rateSelected);
            }

            ToggleButton selectedCalorie = getCheckedButton(calorieButtons);
            if (selectedCalorie != null && !selectedCalorie.getText().equals("All")) {
                switch (selectedCalorie.getText().toString()) {
                    case "< 300":
                        filterMap.put("calories_min", "300");
                        break;
                    case "400-800":
                        filterMap.put("calories_min", "400");
                        filterMap.put("calories_max", "800");
                        break;
                    case "> 1000":
                        filterMap.put("calories_max", "1000");
                        break;
                }
            }

            ToggleButton selectedCategoryButton = getCheckedButton(categoryButtons);
            if (selectedCategoryButton != null && !selectedCategoryButton.getText().equals("All")) {
                String categorySelected = selectedCategoryButton.getText().toString();
                filterMap.put("category", categorySelected);
            }

            String currentSearch = binding.searchBar.getText().toString();

            searchWithFilterAndQuery(currentSearch);

            dialog.dismiss();

        });


    }

    private void searchWithFilterAndQuery(String querySearch) {
        recipeList.clear();

        CollectionReference recipesRef = firestore.collection("recipe");
        Query query = recipesRef;

        if (filterMap.containsKey("cookingTime_max")) {
            query = query.whereLessThanOrEqualTo("cookingTime", Integer.parseInt(filterMap.get("cookingTime_max")));
        }
        if (filterMap.containsKey("calories_min")) {
            query = query.whereGreaterThanOrEqualTo("calories", Integer.parseInt(filterMap.get("calories_min")));
        }

        if (filterMap.containsKey("calories_max")) {
            query = query.whereLessThanOrEqualTo("calories", Integer.parseInt(filterMap.get("calories_max")));
        }

        if (filterMap.containsKey("category")) {
            query = query.whereEqualTo("category", filterMap.get("category"));
        }

        if (filterMap.containsKey("evaluation")) {
            query = query.whereEqualTo("evaluation", Integer.parseInt(filterMap.get("evaluation")));
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                recipeList.clear();
                String lowerCaseQuery = querySearch.trim().toLowerCase();

                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    RecipeModel recipe = doc.toObject(RecipeModel.class);

                    if (recipe != null) {
                        if (lowerCaseQuery.isEmpty() || (recipe.getTitle() != null &&
                                recipe.getTitle().toLowerCase().contains(lowerCaseQuery))) {
                            recipeList.add(recipe);
                        }
                    }
                }

                binding.tvResultCount.setText(new StringBuilder().append(recipeList.size()).append(" results").toString());
                updateRecyclerView(recipeList);
            }
        });


    }

    // if want to search by users
    private void searchUsers(String querySearch) {
        firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Users> usersList = new ArrayList<>();
                    String lowerCaseQuery = querySearch.trim().toLowerCase();

                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        Users users = doc.toObject(Users.class);

                        if (users != null) {
                            if (lowerCaseQuery.isEmpty() || (users.getName() != null &&
                                    users.getName().toLowerCase().contains(lowerCaseQuery))) {
                                usersList.add(users);
                            }
                        }
                    }
                    UsersAdapter adapter = new UsersAdapter(usersList, null);
                    binding.recyclerview.setAdapter(adapter);
                    binding.recyclerview.setLayoutManager(new GridLayoutManager(getContext(), 1));
                    binding.tvResultCount.setText(new StringBuilder().append(usersList.size()).append(" results").toString());

                }
            }
        });
    }


    private void updateRecyclerView(List<RecipeModel> recipes) {
        if (adapter == null) {
            adapter = new RecipeAdapter(recipes, "search", (RecipeAdapter.onClickListener) SearchFragment.this);
            binding.recyclerview.setAdapter(adapter);
            binding.recyclerview.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 أعمدة مثلا

        } else {

            adapter.setList(recipes);
        }
    }

    private void setupFilters(BottomSheetFilterBinding sheetBinding) {

        timeButtons = Arrays.asList(
                sheetBinding.timeAll,
                sheetBinding.time1hour,
                sheetBinding.time30min,
                sheetBinding.time2hour
        );

        calorieButtons = Arrays.asList(
                sheetBinding.calAll,
                sheetBinding.calLess300,
                sheetBinding.cal400800,
                sheetBinding.calMore1000
        );

        rateButtons = Arrays.asList(
                sheetBinding.rate5,
                sheetBinding.rate4,
                sheetBinding.rate3,
                sheetBinding.rate2,
                sheetBinding.rate1
        );

        categoryButtons = Arrays.asList(
                sheetBinding.allBT,
                sheetBinding.breakfastBT,
                sheetBinding.beveragesBT,
                sheetBinding.lunchBT,
                sheetBinding.dinnerBT,
                sheetBinding.dessertsBT,
                sheetBinding.saladsBT,
                sheetBinding.soupsBT,
                sheetBinding.fastFoodBT,
                sheetBinding.healthyMealsBT,
                sheetBinding.appetizersBT
        );

        setupToggleGroup(timeButtons);
        setupToggleGroup(calorieButtons);
        setupToggleGroup(rateButtons);
        setupToggleGroup(categoryButtons);
    }

    private void setupToggleGroup(List<ToggleButton> toggleButtons) {
        this.toggleButtons = toggleButtons;
        for (ToggleButton button : toggleButtons) {
            button.setOnClickListener(v -> {
                for (ToggleButton other : toggleButtons) {
                    other.setChecked(false);
                    other.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.backgroundColor));
                    other.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
                }

                button.setChecked(true);
                button.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.orange_primary));
                button.setTextColor(Color.WHITE);
            });
        }
    }


    private ToggleButton getCheckedButton(List<ToggleButton> buttons) {
        for (ToggleButton button : buttons) {
            if (button.isChecked()) {
                return button;
            }
        }
        return null;
    }

    @Override
    public void onClick(String recipeId) {
        Intent intent = new Intent(getContext(), RecipeDetails.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);

    }


}