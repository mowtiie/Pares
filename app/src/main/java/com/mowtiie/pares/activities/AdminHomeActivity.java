package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityAdminHomeBinding;

import java.util.ArrayList;

public class AdminHomeActivity extends AppCompatActivity {

    private static final Class<?>[] SCREENS = {
            AdminUsersActivity.class, AdminModerationActivity.class,
            AdminAnalyticsActivity.class, AdminReportsActivity.class
    };

    private ActivityAdminHomeBinding binding;
    private GenericAdapter<String[]> adapter;

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

        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.tvTitle.setText(item[0]);
            b.tvSubtitle.setText(item[1]);
            b.tvMeta.setText(item[2]);
            b.getRoot().setOnClickListener(v -> startActivity(new Intent(this, SCREENS[position])));
        });
        binding.rvMenu.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenu.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        String openReports = Data.value(this, "SELECT COUNT(*) FROM reports WHERE status = ?", Data.REPORT_OPEN);
        ArrayList<String[]> items = new ArrayList<>();
        items.add(new String[]{"Users", "Suspend, activate or delete members", ""});
        items.add(new String[]{"Moderation", "Hide posts and messages, handle reports", openReports + " open reports"});
        items.add(new String[]{"Analytics", "Totals for matches, users and content", ""});
        items.add(new String[]{"Reports", "New and current free and premium members", ""});
        adapter.setItems(items);
    }

    private void logout() {
        Data.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
