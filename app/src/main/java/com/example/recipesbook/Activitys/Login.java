package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recipesbook.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences("Recipe", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        // if user login before
        if (sharedPreferences.getBoolean("remembered", false)) {
            Intent intent = new Intent(Login.this, Main.class);
            startActivity(intent);
            finish();
            return;
        }

        binding.loginBt.setOnClickListener(v -> {
            loginUser();
        });


        binding.registerTv.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Register.class));
        });
        binding.forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, ForgotPassword.class));
        });

    }

    private void loginUser() {
        String email = binding.emailEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    // Remember Me
                    if (binding.rememberSwitch.isChecked()) {
                        editor.putBoolean("remembered", true);
                        editor.apply();
                    }

                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login.this, Main.class));
                    finish();

                } else {
                    Toast.makeText(Login.this, "Please comfort data ", Toast.LENGTH_SHORT).show();

                }

            }
        });


    }
}
