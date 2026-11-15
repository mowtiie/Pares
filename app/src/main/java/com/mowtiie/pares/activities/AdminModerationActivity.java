package com.mowtiie.pares.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityAdminModerationBinding;

import java.util.ArrayList;

public class AdminModerationActivity extends AppCompatActivity {

    private static final int TAB_POSTS = 0;
    private static final int TAB_MESSAGES = 1;
    private static final int TAB_PROFILES = 2;
    private static final int TAB_REPORTS = 3;

    private ActivityAdminModerationBinding binding;
    private GenericAdapter<String[]> adapter;
    private int tab = TAB_POSTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (!Data.ROLE_ADMIN.equals(Data.USER_ROLE)) {
            finish();
            return;
        }
        binding = ActivityAdminModerationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tabs.addTab(binding.tabs.newTab().setText("Posts"));
        binding.tabs.addTab(binding.tabs.newTab().setText("Messages"));
        binding.tabs.addTab(binding.tabs.newTab().setText("Profiles"));
        binding.tabs.addTab(binding.tabs.newTab().setText("Open reports"));
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab selected) {
                tab = selected.getPosition();
                load();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab unselected) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab reselected) {
            }
        });

        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.btnAction.setVisibility(View.VISIBLE);
            b.btnAction.setEnabled(true);
            b.btnSecondary.setVisibility(View.GONE);
            if (tab == TAB_POSTS) {
                bindHideRow(b, "posts", item[0], item[1], item[2], item[3], item[4]);
            } else if (tab == TAB_MESSAGES) {
                bindHideRow(b, "messages", item[0], item[1] + " to " + item[2], item[3], item[4], item[5]);
            } else if (tab == TAB_PROFILES) {
                bindProfileRow(b, item);
            } else {
                bindReportRow(b, item);
            }
        });
        binding.rvItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItems.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) {
            load();
        }
    }

    private void bindHideRow(com.mowtiie.pares.databinding.ItemRowBinding b, String table, String id, String title, String body, String time, String hidden) {
        boolean isHidden = hidden.equals("1");
        b.tvTitle.setText(title);
        b.tvSubtitle.setText(body);
        b.tvMeta.setText(time + (isHidden ? " • HIDDEN" : ""));
        b.btnAction.setText(isHidden ? "Unhide" : "Hide");
        b.btnAction.setOnClickListener(v -> {
            Data.setColumn(this, table, id, "hidden", isHidden ? "0" : "1");
            load();
            undoable(isHidden ? "Item is visible again" : "Item hidden", () -> Data.setColumn(this, table, id, "hidden", hidden));
        });
    }

    private void bindProfileRow(com.mowtiie.pares.databinding.ItemRowBinding b, String[] item) {
        boolean suspended = Data.STATUS_SUSPENDED.equals(item[3]);
        b.tvTitle.setText(item[1]);
        b.tvSubtitle.setText(item[2].isEmpty() ? "No bio" : item[2]);
        b.tvMeta.setText(item[3]);
        b.btnAction.setText("Clear bio");
        b.btnAction.setEnabled(!item[2].isEmpty());
        b.btnAction.setOnClickListener(v -> {
            Data.setColumn(this, "users", item[0], "bio", "");
            load();
            undoable("Bio cleared", () -> Data.setColumn(this, "users", item[0], "bio", item[2]));
        });
        b.btnSecondary.setVisibility(View.VISIBLE);
        b.btnSecondary.setText(suspended ? "Activate" : "Suspend");
        b.btnSecondary.setOnClickListener(v -> {
            Data.setColumn(this, "users", item[0], "status", suspended ? Data.STATUS_ACTIVE : Data.STATUS_SUSPENDED);
            load();
            undoable(suspended ? "User is active" : "User is suspended", () -> Data.setColumn(this, "users", item[0], "status", item[3]));
        });
    }

    private void bindReportRow(com.mowtiie.pares.databinding.ItemRowBinding b, String[] item) {
        String type = item[1];
        b.tvTitle.setText(type + " report: " + item[3]);
        b.tvSubtitle.setText(item[6].isEmpty() ? "Content no longer exists" : item[6]);
        b.tvMeta.setText("By " + item[5] + " • " + item[4]);
        b.btnAction.setText("Resolve");
        b.btnAction.setOnClickListener(v -> {
            Data.setColumn(this, "reports", item[0], "status", Data.REPORT_RESOLVED);
            load();
            undoable("Report resolved", () -> Data.setColumn(this, "reports", item[0], "status", Data.REPORT_OPEN));
        });
        b.btnSecondary.setVisibility(View.VISIBLE);
        b.btnSecondary.setText(Data.TARGET_PROFILE.equals(type) ? "Suspend" : "Hide");
        b.btnSecondary.setOnClickListener(v -> {
            if (Data.TARGET_PROFILE.equals(type)) {
                Data.setColumn(this, "users", item[2], "status", Data.STATUS_SUSPENDED);
                undoable("User is suspended", () -> Data.setColumn(this, "users", item[2], "status", Data.STATUS_ACTIVE));
            } else {
                String table = Data.TARGET_POST.equals(type) ? "posts" : "messages";
                Data.setColumn(this, table, item[2], "hidden", "1");
                undoable("Content hidden", () -> Data.setColumn(this, table, item[2], "hidden", "0"));
            }
        });
    }

    private void undoable(String message, Runnable undo) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    undo.run();
                    load();
                })
                .show();
    }

    private void load() {
        ArrayList<String[]> items;
        if (tab == TAB_POSTS) {
            items = Data.rows(this,
                    "SELECT p.id, u.first_name || ' ' || u.last_name, p.body, p.time, p.hidden FROM posts p JOIN users u ON u.id = p.user_id WHERE p.hidden IN (0, 1) ORDER BY p.time DESC, p.id DESC");
            binding.viewEmpty.tvEmptyTitle.setText("No posts");
        } else if (tab == TAB_MESSAGES) {
            items = Data.rows(this,
                    "SELECT m.id, s.first_name, r.first_name, m.body, m.time, m.hidden FROM messages m JOIN users s ON s.id = m.sender_id JOIN users r ON r.id = m.receiver_id ORDER BY m.time DESC, m.id DESC LIMIT 100");
            binding.viewEmpty.tvEmptyTitle.setText("No messages");
        } else if (tab == TAB_PROFILES) {
            items = Data.rows(this,
                    "SELECT id, first_name || ' ' || last_name, bio, status FROM users WHERE role <> ? ORDER BY first_name",
                    Data.ROLE_ADMIN);
            binding.viewEmpty.tvEmptyTitle.setText("No profiles");
        } else {
            items = Data.rows(this,
                    "SELECT r.id, r.target_type, r.target_id, r.reason, r.time, u.first_name,"
                            + " CASE r.target_type WHEN 'POST' THEN (SELECT body FROM posts WHERE id = r.target_id)"
                            + " WHEN 'MESSAGE' THEN (SELECT body FROM messages WHERE id = r.target_id)"
                            + " ELSE (SELECT first_name || ' ' || last_name FROM users WHERE id = r.target_id) END"
                            + " FROM reports r JOIN users u ON u.id = r.reporter_id WHERE r.status = ? ORDER BY r.id DESC",
                    Data.REPORT_OPEN);
            binding.viewEmpty.tvEmptyTitle.setText("No open reports");
        }
        binding.viewEmpty.tvEmptyMessage.setText("Nothing to moderate here.");
        adapter.setItems(items);
        binding.viewEmpty.getRoot().setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
