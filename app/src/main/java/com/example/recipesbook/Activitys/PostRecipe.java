package com.example.recipesbook.Activitys;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipesbook.CloudinaryHelper;
import com.example.recipesbook.Model.RecipeModel;
import com.example.recipesbook.R;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.ActivityPostRecipeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;
/**
 * PostRecipeActivity
 * This fragment handles the creation and editing of recipes.
 *
 * Features:
 * - Allows users to input and validate recipe details:
 *   • Title, Ingredients, Steps, Category, Video URL, Description, Cooking Time, Calories.
 * - Lets users choose and upload  image to Cloudinary.
 * - Supports both adding  new recipe and editing existing one.
 * - Sends a notification to followers when a new recipe is successfully posted.
 * - Clears input fields after a successful post or update.
 *
 */
public class PostRecipe extends AppCompatActivity {
ActivityPostRecipeBinding binding;
    String imageUrl = null;
    Bitmap bitmap;
    RecipeModel recipe;
    FirebaseFirestore fireStore;
    String title, category, ingredients, steps,recipeId, videoUrl, description;
    int calories ,cookingTimes;
    ArrayAdapter<CharSequence> adapter;


    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == RESULT_OK && o.getData() != null) {
                Uri uri = o.getData().getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    binding.imageRecipeIV.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(PostRecipe.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPostRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CloudinaryHelper.init(this);
        fireStore= FirebaseFirestore.getInstance();



        adapter = ArrayAdapter.createFromResource(this, R.array.meal_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);


        binding.imageRecipeIV.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            launcher.launch(intent);
        });

        showDataToEditing();
        binding.addRecipeBT.setOnClickListener(v -> {
            if(checkData()) {
                addRecipe();
            }
        });

        binding.editeRecipeBT.setOnClickListener(v -> {
            if(checkData()) {
                editeRecipe();

            }
        });

        binding.backBT.setOnClickListener(v -> {
            finish();
        });

    }


    private void showDataToEditing() {
        recipeId =getIntent().getStringExtra("recipeId");
        if (recipeId != null) {
            fireStore.collection("recipe").document(recipeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        recipe = task.getResult().toObject(RecipeModel.class);
                        title = recipe.getTitle();
                        category =  recipe.getCategory();
                        cookingTimes =  recipe.getCookingTime();
                        calories = recipe.getCalories();
                        ingredients =  recipe.getIngredients();
                        steps = recipe.getSteps();
                        videoUrl = recipe.getVideUrl();
                        imageUrl = recipe.getImageUrl();
                        description = recipe.getDescription();

                        binding.addRecipeBT.setVisibility(GONE);
                        binding.editeRecipeBT.setVisibility(VISIBLE);
                        binding.textRecipe.setText("Edit Recipe");
                        binding.titleRecipeET.setText(recipe.getTitle());
                        binding.ingredientsRecipeET.setText(recipe.getIngredients());
                        binding.stepsRecipeET.setText(recipe.getSteps());
                        binding.caloriesRecipeET.setText(String.valueOf(recipe.getCalories()));
                        binding.timeRecipeET.setText(String.valueOf(recipe.getCookingTime()));
                        int spinnerPosition = adapter.getPosition(category);
                        binding.spinnerCategory.setSelection(spinnerPosition);
                        binding.viedoUrlRecipeET.setText(recipe.getVideUrl());
                        binding.descriptionRecipeET.setText(recipe.getDescription());
                        Picasso.get().load(recipe.getImageUrl()).into(binding.imageRecipeIV);

                    }
                }
            });
        }
    }

    private boolean  checkData() {
        title = binding.titleRecipeET.getText().toString();
        category = binding.spinnerCategory.getSelectedItem().toString();
        String cookingTimeSt =binding.timeRecipeET.getText().toString();
        String caloriesSt = binding.caloriesRecipeET.getText().toString();
        ingredients = binding.ingredientsRecipeET.getText().toString();
        steps = binding.stepsRecipeET.getText().toString();
        videoUrl = binding.viedoUrlRecipeET.getText().toString();
        description = binding.descriptionRecipeET.getText().toString();

        try {
            cookingTimes = Integer.parseInt(cookingTimeSt);
            calories = Integer.parseInt(caloriesSt);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for Cooking Time and Calories", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (title.isEmpty() || ingredients.isEmpty() ||  cookingTimeSt.isEmpty() || caloriesSt.isEmpty() ||steps.isEmpty() || category.isEmpty() || videoUrl.isEmpty() || description.isEmpty()) {
            Toast.makeText(PostRecipe.this, "Please fill all data ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addRecipe() {
        recipeId = UUID.randomUUID().toString();
        uploadImage(recipeId, new Utils.UploadImageCallback() {
            @Override
            public void onSuccess(String imageUrlFromCloudinary) {
                recipe = new RecipeModel(recipeId, Utils.USER_ID, title, ingredients, steps, category, cookingTimes, calories,0, description, videoUrl, imageUrlFromCloudinary );

                fireStore.collection("recipe").document(recipeId).set(recipe).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PostRecipe.this, "Recipe added successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                        sendNotification(recipeId);

                    } else {
                        Toast.makeText(PostRecipe.this, "Failed to add recipe", Toast.LENGTH_SHORT).show();
                        Log.e("AddRecipeError", task.getException().toString());
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(PostRecipe.this, "Image upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String recipeId) {
        fireStore.collection("users").document(Utils.USER_ID)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String name = userDoc.getString("name");

                    fireStore.collection("users")
                            .document(Utils.USER_ID)
                            .collection("followers")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot followerDoc : querySnapshot) {
                                    String followerId = followerDoc.getId();

                                    Utils.sendNotificationToUser(followerId, recipeId, Utils.USER_ID, "New Recipe", name + " added a new recipe: " + recipe.getTitle());
                                }
                            });
                });
    }


    private void editeRecipe() {
        uploadImage(recipeId, new Utils.UploadImageCallback() {
            @Override
            public void onSuccess(String imageUrlFromCloudinary) {
                recipe = new RecipeModel(recipeId, Utils.USER_ID, title, ingredients, steps, category, cookingTimes, calories,0 ,description, videoUrl, imageUrlFromCloudinary );

                fireStore.collection("recipe").document(recipeId).set(recipe)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PostRecipe.this, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                                Intent intent =new Intent(PostRecipe.this, RecipeDetails.class);
                                intent.putExtra("recipeId",recipeId);
                                startActivity(intent);
                                clearFields();
                                finish();

                            } else {
                                Toast.makeText(PostRecipe.this, "Failed to update recipe", Toast.LENGTH_SHORT).show();
                                Log.d("EditRecipeError", "onComplete: " + task.getException());
                            }
                        });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(PostRecipe.this, "Image upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void uploadImage(String recipeId, Utils.UploadImageCallback callback){
        if(imageUrl == null) {
            Utils.uploadImageToCloudinary("recipe_images", bitmap, PostRecipe.this, recipeId, new Utils.UploadImageCallback() {
                @Override
                public void onSuccess(String imagesUrl) {
                    imageUrl = imagesUrl;
                    callback.onSuccess(imagesUrl);
                }

                @Override
                public void onFailure(String errorMessage) {
                    callback.onFailure(errorMessage);
                }
            });
        } else {
            callback.onSuccess(imageUrl);
        }
    }

    private void clearFields() {
        binding.titleRecipeET.setText("");
        binding.ingredientsRecipeET.setText("");
        binding.stepsRecipeET.setText("");
        binding.timeRecipeET.setText("");
        binding.caloriesRecipeET.setText("");
        binding.viedoUrlRecipeET.setText("");
        binding.descriptionRecipeET.setText("");
        binding.spinnerCategory.setSelection(0);
        binding.imageRecipeIV.setImageResource(R.drawable.inputsearch);
        imageUrl = null;
        bitmap = null;

    }
}