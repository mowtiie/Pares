package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityDiscoverBinding;

import java.util.ArrayList;

public class DiscoverActivity extends AppCompatActivity {

    private ActivityDiscoverBinding binding;
    private String[] current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDiscoverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.viewEmpty.tvEmptyTitle.setText("All done for today");
        binding.viewEmpty.tvEmptyMessage.setText("You have used your 10 suggestions. Come back tomorrow for new people.");
        binding.btnLike.setOnClickListener(v -> act(Data.ACTION_LIKE));
        binding.btnSkip.setOnClickListener(v -> act(Data.ACTION_SKIP));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Data.ensureSuggestions(this);
        showNext();
    }

    private void showNext() {
        ArrayList<String[]> pending = Data.rows(this,
                "SELECT s.id, u.id, u.first_name, u.last_name, u.age, u.location, u.religion, u.language, u.hobbies, u.bio, u.avatar FROM swipes s JOIN users u ON u.id = s.target_id WHERE s.user_id = ? AND s.day = ? AND s.action = ? ORDER BY s.id LIMIT 1",
                Data.me(), Data.today(), Data.ACTION_PENDING);
        int done = Integer.parseInt(Data.value(this, "SELECT COUNT(*) FROM swipes WHERE user_id = ? AND day = ? AND action <> ?", Data.me(), Data.today(), Data.ACTION_PENDING));
        binding.tvProgress.setText(done + " of " + Data.DAILY_LIMIT + " today");
        binding.progress.setProgress(done);

        if (pending.isEmpty()) {
            current = null;
            binding.llDeck.setVisibility(View.GONE);
            binding.viewEmpty.getRoot().setVisibility(View.VISIBLE);
            return;
        }
        current = pending.get(0);
        binding.llDeck.setVisibility(View.VISIBLE);
        binding.viewEmpty.getRoot().setVisibility(View.GONE);
        binding.tvAvatar.setText(current[10]);
        binding.tvName.setText(current[2] + " " + current[3] + ", " + current[4]);
        binding.tvInfo.setText(current[5] + " • " + current[6] + " • " + current[7]);
        binding.tvHobbies.setText(current[8].replace(",", " • "));
        binding.tvBio.setText(current[9]);
    }

    private void act(String action) {
        if (current == null) {
            return;
        }
        String targetId = current[1];
        String targetName = current[2];
        boolean matched = Data.swipe(this, current[0], targetId, action);
        showNext();
        if (matched) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("It's a match")
                    .setMessage("You and " + targetName + " liked each other.")
                    .setPositiveButton("Chat", (dialog, which) -> {
                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.putExtra("user_id", targetId);
                        intent.putExtra("name", targetName);
                        startActivity(intent);
                    })
                    .setNegativeButton("Keep browsing", null)
                    .show();
        }
    }
}
