package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivitySettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
/*
 * SettingsActivity
 *
 * handles the settings screen of the Recipe Book app.
 *
 * Features:
 * - Navigate to Change Password screen.
 * - Logout user from Firebase Authentication and clear local preferences.
 * - Show an About dialog describing the app.
 *
 */
public class SettingsActivity extends AppCompatActivity {
ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.changePasswordLayout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangePassword.class);
            startActivity(intent);

            });


        binding.logoutLayout.setOnClickListener(v -> {
            // sign out from firebase
            FirebaseAuth.getInstance().signOut();
            SharedPreferences sharedPreferences = getSharedPreferences("Recipe", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(SettingsActivity.this, Login.class);
            // clean all the Stack an activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.aboutAppLayout.setOnClickListener(v -> {
            aboutApp();
        });

        binding.backBT.setOnClickListener(v -> {
            finish();
        });


    }

    private void aboutApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);

        String title ="About Recipe Book";
        String message = "Recipe Book is a social cooking app that allows users to explore and rate a wide variety of recipes from around the world. \n\n" +
                "Features include:\n- Uploading and saving favorite recipes\n" +
                "- Following other food lovers\n" +
                "- Watching video tutorials\n" +
                "- Viewing detailed ingredients and preparation steps";

        SpannableString spannable = new SpannableString(title);
        SpannableString spannableMassage = new SpannableString(message);
        int color = getResources().getColor(R.color.orange_primary);
        int colorMassage = getResources().getColor(R.color.gray_dark);
        spannable.setSpan(new ForegroundColorSpan(color), 0, title.length(), 0);
        spannableMassage.setSpan(new ForegroundColorSpan(colorMassage), 0, message.length(), 0);

        builder.setTitle(spannable);
        builder.setMessage(spannableMassage);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

    }
}