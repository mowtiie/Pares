package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivitySearchBinding;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private GenericAdapter<String[]> adapter;
    private boolean peopleTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tabs.addTab(binding.tabs.newTab().setText("People"));
        binding.tabs.addTab(binding.tabs.newTab().setText("Posts"));
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                peopleTab = tab.getPosition() == 0;
                load();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        binding.etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                load();
            }
        });

        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            if (peopleTab) {
                b.tvTitle.setText(item[4] + " " + item[1] + ", " + item[5]);
                b.tvSubtitle.setText(item[2] + " • " + item[3]);
                b.tvMeta.setText("");
                b.btnAction.setVisibility(View.VISIBLE);
                b.btnAction.setText(Data.isFollowing(this, item[0]) ? "Following" : "Follow");
                b.btnAction.setOnClickListener(v -> {
                    Data.setFollowing(this, item[0], !Data.isFollowing(this, item[0]));
                    load();
                });
            } else {
                b.tvTitle.setText(item[2]);
                b.tvSubtitle.setText(item[3]);
                b.tvMeta.setText(item[4]);
                b.btnAction.setVisibility(View.GONE);
            }
            b.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileViewActivity.class);
                intent.putExtra("user_id", peopleTab ? item[0] : item[1]);
                startActivity(intent);
            });
        });
        binding.rvResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvResults.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        String me = Data.me();
        String keyword = "%" + binding.etKeyword.getText().toString().trim() + "%";
        ArrayList<String[]> results;
        if (peopleTab) {
            results = Data.rows(this,
                    "SELECT u.id, u.first_name || ' ' || u.last_name, u.location, u.religion, u.avatar, u.age FROM users u"
                            + " WHERE u.id <> ? AND u.role <> 'ADMIN' AND u.status = 'ACTIVE' AND u.visibility = 'PUBLIC'"
                            + " AND (u.first_name LIKE ? OR u.last_name LIKE ?)" + Data.notBlocked("u.id")
                            + " ORDER BY u.first_name LIMIT 50",
                    me, keyword, keyword, me, me);
            binding.viewEmpty.tvEmptyTitle.setText("No people found");
        } else {
            results = Data.rows(this,
                    "SELECT p.id, p.user_id, u.first_name || ' ' || u.last_name, p.body, p.time FROM posts p JOIN users u ON u.id = p.user_id"
                            + " WHERE p.hidden = 0 AND u.status = 'ACTIVE' AND (p.user_id = ? OR u.visibility = 'PUBLIC') AND p.body LIKE ?"
                            + Data.notBlocked("p.user_id") + " ORDER BY p.time DESC, p.id DESC LIMIT 50",
                    me, keyword, me, me);
            binding.viewEmpty.tvEmptyTitle.setText("No posts found");
        }
        binding.viewEmpty.tvEmptyMessage.setText("Try a different keyword.");
        adapter.setItems(results);
        binding.viewEmpty.getRoot().setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
