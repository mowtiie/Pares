package com.mowtiie.pares.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityAdminReportsBinding;

import java.util.ArrayList;

public class AdminReportsActivity extends AppCompatActivity {

    private ActivityAdminReportsBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (!Data.ROLE_ADMIN.equals(Data.USER_ROLE)) {
            finish();
            return;
        }
        binding = ActivityAdminReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tabs.addTab(binding.tabs.newTab().setText("New free today"));
        binding.tabs.addTab(binding.tabs.newTab().setText("New premium today"));
        binding.tabs.addTab(binding.tabs.newTab().setText("Free users"));
        binding.tabs.addTab(binding.tabs.newTab().setText("Premium users"));
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                load();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.tvTitle.setText(item[4] + " " + item[1]);
            b.tvSubtitle.setText(item[2]);
            b.tvMeta.setText(item[3]);
        });
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) {
            load();
        }
    }

    private void load() {
        int tab = binding.tabs.getSelectedTabPosition();
        String base = "SELECT id, first_name || ' ' || last_name, email, 'Joined ' || created || CASE WHEN role = 'PREMIUM' THEN ' • ' || premium_plan || ' until ' || premium_until ELSE '' END, avatar FROM users WHERE ";
        String today = Data.today();
        ArrayList<String[]> users;
        if (tab == 0) {
            users = Data.rows(this, base + "role = ? AND created = ? ORDER BY id DESC", Data.ROLE_FREE, today);
        } else if (tab == 1) {
            users = Data.rows(this, base + "role = ? AND (created = ? OR id IN (SELECT user_id FROM payments WHERE substr(time, 1, 10) = ?)) ORDER BY id DESC", Data.ROLE_PREMIUM, today, today);
        } else if (tab == 2) {
            users = Data.rows(this, base + "role = ? ORDER BY first_name", Data.ROLE_FREE);
        } else {
            users = Data.rows(this, base + "role = ? ORDER BY first_name", Data.ROLE_PREMIUM);
        }
        binding.viewEmpty.tvEmptyTitle.setText("No members");
        binding.viewEmpty.tvEmptyMessage.setText("Nobody matches this report yet.");
        adapter.setItems(users);
        binding.viewEmpty.getRoot().setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
