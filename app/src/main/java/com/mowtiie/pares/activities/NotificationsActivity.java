package com.mowtiie.pares.activities;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityNotificationsBinding;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.viewEmpty.tvEmptyTitle.setText("No notifications");
        binding.viewEmpty.tvEmptyMessage.setText("Likes, matches and messages show up here.");

        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.tvTitle.setText(item[1]);
            b.tvSubtitle.setText(item[2]);
            b.tvMeta.setText(item[3] + (item[4].equals("0") ? " • NEW" : ""));
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<String[]> notifications = Data.rows(this,
                "SELECT id, type, body, time, seen FROM notifications WHERE user_id = ? AND type IN (?, ?, ?) ORDER BY time DESC, id DESC",
                Data.me(), Data.NOTIFY_LIKE, Data.NOTIFY_MESSAGE, Data.NOTIFY_MATCH);
        adapter.setItems(notifications);
        binding.viewEmpty.getRoot().setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);

        ContentValues values = new ContentValues();
        values.put("seen", 1);
        Data.db(this).update("notifications", values, "user_id = ?", new String[]{Data.me()});
    }
}
