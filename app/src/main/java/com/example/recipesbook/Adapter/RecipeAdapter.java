package com.example.recipesbook.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesbook.Activitys.PostRecipe;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.Model.Users;
import com.example.recipesbook.R;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.RecipeItemBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeHolder> {
    List<RecipeModel> recipeData;
    RecipeItemBinding binding;
    onClickListener listener;
    String Screen;
    RecipeModel recipes;

    public RecipeAdapter(List<RecipeModel> recipeDta, String Screen, onClickListener listener) {
        this.recipeData = recipeDta;
        this.listener = listener;
        this.Screen = Screen;
    }

    @NonNull
    @Override
    public RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecipeHolder(binding);

    }


    @Override
    public void onBindViewHolder(@NonNull RecipeHolder holder, int position) {
        RecipeItemBinding binding = holder.binding;
        int pos = holder.getAdapterPosition();
        recipes = recipeData.get(pos);

        binding.titleTV.setText(recipes.getTitle());
        binding.timeTV.setText(String.valueOf(recipes.getCookingTime() +"m"));
        binding.caloriesRecipeTV.setText("🔥"+recipes.getCalories() + "kc");
        binding.starTV.setText(String.valueOf(recipes.getEvaluation()));
        Picasso.get().load(recipes.getImageUrl()).into(binding.imageIV);
        loadUserData(binding, recipes);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sent the recipeId to show RecipeDetails
                listener.onClick(recipeData.get(pos).getRecipeId());
            }
        });

        Utils.loadBookMarK(recipeData.get(pos).getRecipeId(), binding.bookMarkIV);

        binding.bookMarkIV.setOnClickListener(v -> {
            Utils.bookmark(recipeData.get(pos).getRecipeId(), binding.bookMarkIV, v.getContext());
        });

        if (Screen.equals("home")) {
            ViewGroup.LayoutParams params = binding.recipeItem.getLayoutParams();
            params.width = 800;
            binding.recipeItem.setLayoutParams(params);
        }

        // if screen profile to update and delete the recipe
        if (Screen.equals("profile")) {
            binding.moreIcon.setOnClickListener(v -> {

                if (pos != RecyclerView.NO_POSITION) {
                    Utils.showRecipePopupMenu(v.getContext(), binding.moreIcon, recipeData.get(pos),
                            () -> {
                                recipeData.remove(pos);
                                notifyItemRemoved(pos);
                            },
                            () -> {
                                Intent intent = new Intent(v.getContext(), PostRecipe.class);
                                intent.putExtra("recipeId", recipes.getRecipeId());
                                v.getContext().startActivity(intent);
                            }
                    );
                }
            });

        }else{
            binding.moreIcon.setVisibility(View.GONE);
        }




    }


    @Override
    public int getItemCount() {
        return recipeData.size();
    }

    public interface OnRecipeClickListener {
    }

    public class RecipeHolder extends RecyclerView.ViewHolder {
        RecipeItemBinding binding;

        public RecipeHolder(@NonNull RecipeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    public interface onClickListener {
        void onClick(String recipeId);

    }

    public void setList(List<RecipeModel> newData) {
        this.recipeData = newData;
        notifyDataSetChanged();
    }

    private void loadUserData(RecipeItemBinding binding, RecipeModel recipe) {
        FirebaseFirestore.getInstance().collection("users").document(recipe.getUserId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Users user = task.getResult().toObject(Users.class);
                if (user != null) {
                    binding.userNameTV.setText(user.getName());
                    Picasso.get().load(user.getImageUrl()).placeholder(R.drawable.profile_image).into(binding.userImageIV);
                }
            } else {
                Toast.makeText(binding.getRoot().getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
