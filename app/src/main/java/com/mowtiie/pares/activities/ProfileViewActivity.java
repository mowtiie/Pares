package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mowtiie.pares.R;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityProfileViewBinding;
import com.mowtiie.pares.databinding.DialogReportBinding;

import java.util.ArrayList;

public class ProfileViewActivity extends AppCompatActivity {

    private static final String[] REASONS = {"Spam", "Harassment", "Fake profile", "Inappropriate content", "Other"};

    private ActivityProfileViewBinding binding;
    private String userId;
    private String firstName;
    private boolean own;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        userId = getIntent().getStringExtra("user_id");
        own = userId.equals(Data.me());

        ArrayList<String[]> found = Data.rows(this,
                "SELECT id, first_name, last_name, age, location, religion, language, hobbies, bio, avatar, visibility, status FROM users WHERE id = ?" + Data.notBlocked("id"),
                userId, Data.me(), Data.me());
        if (found.isEmpty() || Data.STATUS_SUSPENDED.equals(found.get(0)[11])) {
            Toast.makeText(this, "This profile is not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String[] user = found.get(0);
        if (!own && Data.VISIBILITY_PRIVATE.equals(user[10]) && !Data.isMatched(this, userId)) {
            Toast.makeText(this, "This profile is private", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firstName = user[1];
        binding.tvAvatar.setText(user[9]);
        binding.tvName.setText(user[1] + " " + user[2] + ", " + user[3]);
        binding.tvInfo.setText(user[4] + " • " + user[5] + " • " + user[6]);
        binding.tvHobbies.setText(user[7].replace(",", " • "));
        binding.tvBio.setText(user[8]);

        binding.btnFollow.setOnClickListener(v -> {
            boolean following = !Data.isFollowing(this, userId);
            Data.setFollowing(this, userId, following);
            if (following) {
                Toast.makeText(this, firstName + " was notified that you followed", Toast.LENGTH_SHORT).show();
            }
            refresh();
        });
        binding.btnChat.setOnClickListener(v -> openChat());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_report) {
                showReport();
            } else if (id == R.id.action_block) {
                blockUser();
            } else if (id == R.id.action_unmatch) {
                unmatchUser();
            }
            return true;
        });
        refresh();
    }

    private void refresh() {
        boolean matched = Data.isMatched(this, userId);
        binding.btnChat.setVisibility(!own && Data.canChat(this, userId) ? View.VISIBLE : View.GONE);
        binding.btnFollow.setVisibility(own ? View.GONE : View.VISIBLE);
        binding.btnFollow.setText(Data.isFollowing(this, userId) ? "Following" : "Follow");
        binding.toolbar.getMenu().findItem(R.id.action_report).setVisible(!own);
        binding.toolbar.getMenu().findItem(R.id.action_block).setVisible(!own);
        binding.toolbar.getMenu().findItem(R.id.action_unmatch).setVisible(matched);
        String followers = Data.value(this, "SELECT COUNT(*) FROM follows WHERE followee_id = ?", userId);
        String following = Data.value(this, "SELECT COUNT(*) FROM follows WHERE follower_id = ?", userId);
        binding.tvCounts.setText(followers + " followers • " + following + " following");
    }

    private void openChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("name", firstName);
        startActivity(intent);
    }

    private void showReport() {
        DialogReportBinding dialogBinding = DialogReportBinding.inflate(getLayoutInflater());
        dialogBinding.ddReason.setSimpleItems(REASONS);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Report " + firstName)
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Report", null)
                .setNegativeButton("Cancel", null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            dialogBinding.tilReason.setError(null);
            String reason = dialogBinding.ddReason.getText().toString();
            if (reason.isEmpty()) {
                dialogBinding.tilReason.setError("Pick a reason");
                return;
            }
            String reportId = Data.addReport(this, Data.TARGET_PROFILE, userId, reason);
            dialog.dismiss();
            Snackbar.make(binding.getRoot(), "Report sent", Snackbar.LENGTH_LONG)
                    .setAction("Undo", view -> Data.deleteRow(this, "reports", reportId))
                    .show();
        });
    }

    private void blockUser() {
        boolean wasMatched = Data.isMatched(this, userId);
        Data.block(this, userId);
        Snackbar.make(binding.getRoot(), firstName + " is blocked", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    Data.unblock(this, userId);
                    if (wasMatched) {
                        Data.setMatchStatus(this, userId, Data.MATCH_ACTIVE);
                    }
                    refresh();
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            finish();
                        }
                    }
                })
                .show();
    }

    private void unmatchUser() {
        Data.setMatchStatus(this, userId, Data.MATCH_UNMATCHED);
        refresh();
        Snackbar.make(binding.getRoot(), "Unmatched from " + firstName, Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    Data.setMatchStatus(this, userId, Data.MATCH_ACTIVE);
                    refresh();
                })
                .show();
    }
}
