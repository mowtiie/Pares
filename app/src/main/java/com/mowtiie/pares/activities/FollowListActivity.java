package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityFollowListBinding;

import java.util.ArrayList;

public class FollowListActivity extends AppCompatActivity {

    private ActivityFollowListBinding binding;
    private GenericAdapter<String[]> adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFollowListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getStringExtra("user_id");
        if (userId == null) {
            userId = Data.me();
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tabs.addTab(binding.tabs.newTab().setText("Followers"));
        binding.tabs.addTab(binding.tabs.newTab().setText("Following"));
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
            b.tvTitle.setText(item[3] + " " + item[1]);
            b.tvSubtitle.setText(item[2]);
            b.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileViewActivity.class);
                intent.putExtra("user_id", item[0]);
                startActivity(intent);
            });
        });
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        boolean followers = binding.tabs.getSelectedTabPosition() == 0;
        String sql = "SELECT u.id, u.first_name || ' ' || u.last_name, u.location, u.avatar FROM follows f JOIN users u ON u.id = "
                + (followers ? "f.follower_id WHERE f.followee_id = ?" : "f.followee_id WHERE f.follower_id = ?")
                + " AND u.status = 'ACTIVE'" + Data.notBlocked("u.id") + " ORDER BY u.first_name";
        ArrayList<String[]> users = Data.rows(this, sql, userId, Data.me(), Data.me());
        adapter.setItems(users);
        binding.viewEmpty.tvEmptyTitle.setText(followers ? "No followers yet" : "Not following anyone yet");
        binding.viewEmpty.tvEmptyMessage.setText("Follow people from their profile or from Search.");
        binding.viewEmpty.getRoot().setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
