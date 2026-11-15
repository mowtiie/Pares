package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityAdminHomeBinding;

public class AdminHomeActivity extends AppCompatActivity {

    private ActivityAdminHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (!Data.ROLE_ADMIN.equals(Data.USER_ROLE)) {
            logout();
            return;
        }
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvWelcome.setText("Welcome, " + Data.USER_FIRST_NAME + " " + Data.USER_LAST_NAME);
        binding.tvRole.setText("Role: " + Data.USER_ROLE);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            logout();
            return true;
        });
    }

    private void logout() {
        Data.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
