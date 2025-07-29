package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipesbook.CloudinaryHelper;
import com.example.recipesbook.Model.Users;
import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
public class Register extends AppCompatActivity {
    ActivityRegisterBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore fireStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        showListCountries();

        binding.registerBt.setOnClickListener(v -> {
                registerUser();
        });

        binding.loginTv.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, Login.class));});
        binding.backBT.setOnClickListener(v -> {
            finish();
        });


    }
    // This method handles the user registration using email & password
    private void registerUser() {
        String email = binding.emailEt.getText().toString().trim();
        String name = binding.userNameEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();
        String comfortPassword = binding.comfortPasswordEt.getText().toString().trim();
        String country = binding.countriesSp.getSelectedItem().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.setError("Invalid email format");
            return;
        }
        if (email.isEmpty() || country.isEmpty() || name.isEmpty() || password.isEmpty() || comfortPassword.isEmpty()) {
            Toast.makeText(Register.this, "Check your data", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!comfortPassword.equals(password)) {
            Toast.makeText(Register.this, "Password and confirm password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.registerBt.setEnabled(false);

        String uploadedImageUrl = "https://th.bing.com/th/id/R.9f50b5a313af60b2f20c86afac116835?rik=Lxs%2f5zSFq%2bNlAQ&pid=ImgRaw&r=0";
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    binding.registerBt.setEnabled(true);
                    FirebaseUser user = task.getResult().getUser();
                    if (user != null) {
                        Users users = new Users(user.getUid(), name, email, country, "", uploadedImageUrl);

                        fireStore.collection("users").document(user.getUid()).set(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Register.this, Login.class));
                                    finish();
                                } else {
                                    Toast.makeText(Register.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    } else {
                        Toast.makeText(Register.this, "Registration Failed ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String error = task.getException().getMessage();
                    if (error.contains("email address is already in use")) {
                        Toast.makeText(Register.this, "email address is already exist", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Register.this, "Registration Failed" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }

    // This method populates the countries spinner using Locale
    private void showListCountries() {
        SortedSet<String> countries = new TreeSet<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            if (!locale.getDisplayCountry().isEmpty()) {
                countries.add(locale.getDisplayCountry());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countries.toArray(new String[0]));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.countriesSp.setAdapter(adapter);

    }


}