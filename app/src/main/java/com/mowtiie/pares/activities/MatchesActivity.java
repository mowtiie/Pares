package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityMatchesBinding;

import java.util.ArrayList;

public class MatchesActivity extends AppCompatActivity {

    private ActivityMatchesBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMatchesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.viewEmpty.tvEmptyTitle.setText("No matches yet");
        binding.viewEmpty.tvEmptyMessage.setText("Like people in Discover. When they like you back, they show up here.");

        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.tvTitle.setText(item[3] + " " + item[1] + " " + item[2]);
            b.tvSubtitle.setText(item[5].isEmpty() ? "Say hello" : item[5]);
            b.tvMeta.setText("Matched " + item[4]);
            b.btnAction.setVisibility(View.VISIBLE);
            b.btnAction.setText("Chat");
            View.OnClickListener open = v -> {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("user_id", item[0]);
                intent.putExtra("name", item[1]);
                startActivity(intent);
            };
            b.btnAction.setOnClickListener(open);
            b.getRoot().setOnClickListener(open);
        });
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMatches.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String me = Data.me();
        ArrayList<String[]> matches = Data.rows(this,
                "SELECT u.id, u.first_name, u.last_name, u.avatar, m.created, COALESCE((SELECT body FROM messages WHERE hidden = 0 AND ((sender_id = u.id AND receiver_id = ?) OR (sender_id = ? AND receiver_id = u.id)) ORDER BY time DESC, id DESC LIMIT 1), '')"
                        + " FROM matches m JOIN users u ON u.id = CASE WHEN m.user_a = ? THEN m.user_b ELSE m.user_a END"
                        + " WHERE (m.user_a = ? OR m.user_b = ?) AND m.status = ? ORDER BY m.id DESC",
                me, me, me, me, me, Data.MATCH_ACTIVE);
        adapter.setItems(matches);
        binding.viewEmpty.getRoot().setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
