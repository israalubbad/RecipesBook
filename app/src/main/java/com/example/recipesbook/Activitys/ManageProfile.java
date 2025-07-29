package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipesbook.CloudinaryHelper;
import com.example.recipesbook.Model.Users;
import com.example.recipesbook.R;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.ActivityManageProfileBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
/*
 * ManageProfile
 * <p>
 * Activity that allows users to update their personal profile details,
 * including name, bio, country, and profile image.
 * <p>
 * Features:
 * - Allows selection of country from a sorted list.
 * - Lets users change their profile image and upload it to Cloudinary.
 * - Updates user information in Firestore and Firebase Authentication.
 * - Prompts the user to enter their current password in order to update their email address.
 *
 */

public class ManageProfile extends AppCompatActivity {

    FirebaseFirestore fireStore;
    ActivityManageProfileBinding binding;
    Bitmap bitmap;
    String imageUrl;
    ArrayAdapter<String> adapter;
    String email;
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            binding.profileImage.setImageBitmap(bitmap);
                            Utils.uploadImageToCloudinary("profile_images", bitmap, getApplicationContext(), Utils.USER_ID, new Utils.UploadImageCallback() {
                                @Override
                                public void onSuccess(String imagesUrl) {
                                    imageUrl = imagesUrl;
                                    Toast.makeText(ManageProfile.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(ManageProfile.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            Toast.makeText(ManageProfile.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityManageProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CloudinaryHelper.init(this);
        fireStore = FirebaseFirestore.getInstance();

        // Populate countries
        SortedSet<String> countries = new TreeSet<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            if (!locale.getDisplayCountry().isEmpty()) {
                countries.add(locale.getDisplayCountry());
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries.toArray(new String[0]));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.countrySp.setAdapter(adapter);

        binding.backBT.setOnClickListener(v -> finish());
        binding.changeImageBtn.setOnClickListener(v -> openImagePicker());
        binding.saveBtn.setOnClickListener(v -> handleSave());

        loadUserData();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        launcher.launch(intent);
    }

    private void loadUserData() {
        fireStore.collection("users").document(Utils.USER_ID).get().addOnSuccessListener(doc -> {
            Users user = doc.toObject(Users.class);
            if (user != null) {
                email =user.getEmail();
                binding.nameEt.setText(user.getName());
                binding.emailEt.setText(user.getEmail());
                binding.bioEt.setText(user.getBio());
                imageUrl = user.getImageUrl();

                int position = adapter.getPosition(user.getCountry());
                if (position >= 0) {
                    binding.countrySp.setSelection(position);
                }

                Picasso.get().load(imageUrl).placeholder(R.drawable.profile_image).into(binding.profileImage);
            }
        });
    }

    private void handleSave() {
        String newEmail = binding.emailEt.getText().toString().trim();
        String name = binding.nameEt.getText().toString().trim();
        String bio = binding.bioEt.getText().toString().trim();
        String country = binding.countrySp.getSelectedItem().toString();

        if (newEmail.isEmpty() || name.isEmpty() || bio.isEmpty() || imageUrl == null || country.isEmpty()) {
            Toast.makeText(this, "Please complete all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Users updatedUser = new Users(Utils.USER_ID, name, newEmail, country, bio, imageUrl);


        // If the user has changed their email, verify password before updating it securely
        if(!email.equals(newEmail)){
            promptForPasswordAndUpdate(newEmail ,updatedUser );
        }else{
            saveUserDataToFirestore(updatedUser);
        }

    }
    private void promptForPasswordAndUpdate(String newEmail, Users updatedUser) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_password, null);

        TextView titleTV = dialogView.findViewById(R.id.titleTV);
        TextView messageTV = dialogView.findViewById(R.id.massageTV);
        TextInputEditText passwordEt = dialogView.findViewById(R.id.passwordEt);

        titleTV.setText("Update Email");
        messageTV.setText("To update your email, please enter your password");
        passwordEt.setHint("Password");

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordEt.getText().toString().trim();

                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    user.reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.verifyBeforeUpdateEmail(newEmail)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            saveUserDataToFirestore(updatedUser);
                                            Toast.makeText(ManageProfile.this, "A verification link has been sent to the new email. Please confirm it from your inbox.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(ManageProfile.this, "Failed to send verification link: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(ManageProfile.this, "Incorrect password or an error occurred: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveUserDataToFirestore(Users updatedUser) {
        fireStore.collection("users").document(Utils.USER_ID)
                .set(updatedUser)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile. Try again.", Toast.LENGTH_SHORT).show();
                });
    }
}