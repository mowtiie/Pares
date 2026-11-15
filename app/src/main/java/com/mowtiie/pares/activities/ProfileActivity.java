package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            Data.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        });
        binding.btnEdit.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        binding.btnPreferences.setOnClickListener(v -> startActivity(new Intent(this, PreferencesActivity.class)));
        binding.btnUpgrade.setOnClickListener(v -> startActivity(new Intent(this, PremiumActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Data.loadUser(this, Data.USER_ID);
        boolean premium = Data.ROLE_PREMIUM.equals(Data.USER_ROLE);

        binding.tvAvatar.setText(Data.USER_AVATAR);
        binding.tvName.setText(Data.USER_FIRST_NAME + " " + Data.USER_LAST_NAME);
        binding.tvContact.setText(Data.USER_EMAIL + " • " + Data.USER_PHONE);
        binding.tvBio.setText(Data.USER_BIO);
        binding.tvPlan.setText(premium
                ? "Premium " + Data.USER_PREMIUM_PLAN.toLowerCase() + " plan until " + Data.USER_PREMIUM_UNTIL
                : "Free member");

        binding.btnUpgrade.setVisibility(premium ? View.GONE : View.VISIBLE);
        binding.swPrivate.setVisibility(premium ? View.VISIBLE : View.GONE);
        binding.tvPrivacyHint.setVisibility(premium ? View.VISIBLE : View.GONE);
        binding.swPrivate.setOnCheckedChangeListener(null);
        binding.swPrivate.setChecked(Data.VISIBILITY_PRIVATE.equals(Data.USER_VISIBILITY));
        binding.swPrivate.setOnCheckedChangeListener((button, checked) ->
                Data.setVisibility(this, checked ? Data.VISIBILITY_PRIVATE : Data.VISIBILITY_PUBLIC));
    }
}
