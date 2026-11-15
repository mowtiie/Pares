package com.mowtiie.pares.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityAdminUsersBinding;

import java.util.ArrayList;

public class AdminUsersActivity extends AppCompatActivity {

    private ActivityAdminUsersBinding binding;
    private GenericAdapter<String[]> adapter;
    private String pendingDelete = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (!Data.ROLE_ADMIN.equals(Data.USER_ROLE)) {
            finish();
            return;
        }
        binding = ActivityAdminUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.viewEmpty.tvEmptyTitle.setText("No users found");
        binding.viewEmpty.tvEmptyMessage.setText("Try a different name or email.");
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
            boolean suspended = Data.STATUS_SUSPENDED.equals(item[4]);
            b.tvTitle.setText(item[5] + " " + item[1]);
            b.tvSubtitle.setText(item[2]);
            b.tvMeta.setText(item[3] + " • " + item[4]);
            b.btnAction.setVisibility(View.VISIBLE);
            b.btnAction.setText(suspended ? "Activate" : "Suspend");
            b.btnAction.setOnClickListener(v -> {
                Data.setColumn(this, "users", item[0], "status", suspended ? Data.STATUS_ACTIVE : Data.STATUS_SUSPENDED);
                load();
                Snackbar.make(binding.getRoot(), item[1] + (suspended ? " is active" : " is suspended"), Snackbar.LENGTH_LONG)
                        .setAction("Undo", view -> {
                            Data.setColumn(this, "users", item[0], "status", item[4]);
                            load();
                        })
                        .show();
            });
            b.btnSecondary.setVisibility(View.VISIBLE);
            b.btnSecondary.setText("Delete");
            b.btnSecondary.setOnClickListener(v -> delete(item));
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

    private void delete(String[] user) {
        pendingDelete = user[0];
        load();
        Snackbar.make(binding.getRoot(), "Deleted " + user[1], Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    pendingDelete = "";
                    load();
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            Data.deleteUser(AdminUsersActivity.this, user[0]);
                            if (pendingDelete.equals(user[0])) {
                                pendingDelete = "";
                            }
                        }
                    }
                })
                .show();
    }

    private void load() {
        String keyword = "%" + binding.etKeyword.getText().toString().trim() + "%";
        ArrayList<String[]> users = new ArrayList<>();
        for (String[] row : Data.rows(this,
                "SELECT id, first_name || ' ' || last_name, email, role, status, avatar FROM users WHERE role <> ? AND (first_name LIKE ? OR last_name LIKE ? OR email LIKE ?) ORDER BY first_name",
                Data.ROLE_ADMIN, keyword, keyword, keyword)) {
            if (!row[0].equals(pendingDelete)) {
                users.add(row);
            }
        }
        adapter.setItems(users);
        binding.viewEmpty.getRoot().setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
