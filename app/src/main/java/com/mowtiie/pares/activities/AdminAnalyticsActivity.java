package com.mowtiie.pares.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityAdminAnalyticsBinding;

import java.util.ArrayList;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private ActivityAdminAnalyticsBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (!Data.ROLE_ADMIN.equals(Data.USER_ROLE)) {
            finish();
            return;
        }
        binding = ActivityAdminAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.tvTitle.setText(item[0]);
            b.tvSubtitle.setText(item[1]);
        });
        binding.rvStats.setLayoutManager(new LinearLayoutManager(this));
        binding.rvStats.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        ArrayList<String[]> stats = new ArrayList<>();
        stats.add(new String[]{"Total matches", Data.value(this, "SELECT COUNT(*) FROM matches")});
        stats.add(new String[]{"Matches today", Data.value(this, "SELECT COUNT(*) FROM matches WHERE created = ?", Data.today())});
        stats.add(new String[]{"Total users", Data.value(this, "SELECT COUNT(*) FROM users WHERE role <> ?", Data.ROLE_ADMIN)});
        stats.add(new String[]{"Free users", Data.value(this, "SELECT COUNT(*) FROM users WHERE role = ?", Data.ROLE_FREE)});
        stats.add(new String[]{"Premium users", Data.value(this, "SELECT COUNT(*) FROM users WHERE role = ?", Data.ROLE_PREMIUM)});
        stats.add(new String[]{"Total posts", Data.value(this, "SELECT COUNT(*) FROM posts WHERE hidden IN (0, 1)")});
        stats.add(new String[]{"Total messages", Data.value(this, "SELECT COUNT(*) FROM messages")});
        adapter.setItems(stats);
    }
}
