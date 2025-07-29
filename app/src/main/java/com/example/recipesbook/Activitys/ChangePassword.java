package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
/*
 * ChangePassword Activity
 *
 *  allows the user to securely update their password.
 * It requires the user to enter the current password, new password, and confirm the new password.
 * The activity performs reauthentication before updating the password in Firebase Authentication.
 *
 * Features:
 * - Input validation for empty fields and password matching.
 * - Secure reauthentication before password update.
 * - Navigation to ForgotPassword screen if the user forgot their password.
 *
 */
public class ChangePassword extends AppCompatActivity {
    ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.updatePasswordButton.setOnClickListener(v -> {
            changePassword();

        });

        binding.backBT.setOnClickListener(v -> {
            finish();
        });

        binding.passwordForgot.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePassword.this, ForgotPassword.class);
            startActivity(intent);

        });

    }

    private void changePassword() {
        String currentPassword = binding.currentPasswordET.getText().toString().trim();
        String newPassword = binding.newPasswordET.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordET.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Update failed ", Toast.LENGTH_LONG).show();

                }

            }
        });
    }
}