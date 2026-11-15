package com.mowtiie.pares.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityFeedBinding;
import com.mowtiie.pares.databinding.SheetCommentsBinding;
import com.mowtiie.pares.databinding.SheetNewPostBinding;

import java.util.ArrayList;

public class FeedActivity extends AppCompatActivity {

    private ActivityFeedBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            showNewPost();
            return true;
        });
        binding.viewEmpty.tvEmptyTitle.setText("No posts yet");
        binding.viewEmpty.tvEmptyMessage.setText("Posts from people you follow show up under Following.");
        binding.chipFilter.setOnCheckedStateChangeListener((group, checkedIds) -> load());

        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.tvTitle.setText(item[2]);
            b.tvSubtitle.setText(item[3]);
            b.tvMeta.setText(item[5] + (item[5].equals("1") ? " like • " : " likes • ")
                    + item[6] + (item[6].equals("1") ? " comment • " : " comments • ") + item[4]);
            b.btnAction.setVisibility(View.VISIBLE);
            b.btnAction.setText(item[7].equals("1") ? "Liked" : "Like");
            b.btnAction.setOnClickListener(v -> toggleLike(item[0], item[7].equals("1")));
            b.getRoot().setOnClickListener(v -> showComments(item));
        });
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPosts.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        boolean following = binding.chipFollowing.isChecked();
        String me = Data.me();
        String sql = "SELECT p.id, p.user_id, u.first_name || ' ' || u.last_name, p.body, p.time,"
                + " (SELECT COUNT(*) FROM post_likes WHERE post_id = p.id),"
                + " (SELECT COUNT(*) FROM comments WHERE post_id = p.id),"
                + " EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.id AND user_id = ?)"
                + " FROM posts p JOIN users u ON u.id = p.user_id"
                + " WHERE p.hidden = 0 AND u.status = 'ACTIVE' AND (p.user_id = ? OR u.visibility = 'PUBLIC')"
                + Data.notBlocked("p.user_id")
                + (following ? " AND p.user_id IN (SELECT followee_id FROM follows WHERE follower_id = ?)" : "")
                + " ORDER BY p.time DESC, p.id DESC";
        ArrayList<String[]> posts = following
                ? Data.rows(this, sql, me, me, me, me, me)
                : Data.rows(this, sql, me, me, me, me);
        adapter.setItems(posts);
        binding.viewEmpty.getRoot().setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void toggleLike(String postId, boolean liked) {
        if (liked) {
            Data.db(this).delete("post_likes", "post_id = ? AND user_id = ?", new String[]{postId, Data.me()});
        } else {
            ContentValues values = new ContentValues();
            values.put("post_id", postId);
            values.put("user_id", Data.me());
            Data.db(this).insert("post_likes", null, values);
        }
        load();
    }

    private void showNewPost() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        SheetNewPostBinding sheetBinding = SheetNewPostBinding.inflate(getLayoutInflater());
        sheet.setContentView(sheetBinding.getRoot());
        sheetBinding.btnPost.setOnClickListener(v -> {
            sheetBinding.tilPost.setError(null);
            String body = sheetBinding.etPost.getText().toString().trim();
            if (body.isEmpty()) {
                sheetBinding.tilPost.setError("Write something to post");
                return;
            }
            ContentValues values = new ContentValues();
            values.put("user_id", Data.me());
            values.put("body", body);
            values.put("time", Data.now());
            values.put("hidden", 0);
            Data.db(this).insert("posts", null, values);
            sheet.dismiss();
            load();
            Snackbar.make(binding.getRoot(), "Posted", Snackbar.LENGTH_SHORT).show();
        });
        sheet.show();
    }

    private void showComments(String[] post) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        SheetCommentsBinding sheetBinding = SheetCommentsBinding.inflate(getLayoutInflater());
        sheet.setContentView(sheetBinding.getRoot());
        boolean own = post[1].equals(Data.me());

        sheetBinding.tvPostAuthor.setText(post[2]);
        sheetBinding.tvPostBody.setText(post[3]);
        sheetBinding.btnDelete.setVisibility(own ? View.VISIBLE : View.GONE);
        sheetBinding.btnProfile.setVisibility(own ? View.GONE : View.VISIBLE);
        sheetBinding.viewEmpty.tvEmptyTitle.setText("No comments yet");
        sheetBinding.viewEmpty.tvEmptyMessage.setText("Be the first to comment.");

        GenericAdapter<String[]> commentAdapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.tvTitle.setText(item[0]);
            b.tvSubtitle.setText(item[1]);
            b.tvMeta.setText(item[2]);
        });
        sheetBinding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        sheetBinding.rvComments.setAdapter(commentAdapter);

        Runnable reload = () -> {
            ArrayList<String[]> comments = Data.rows(this,
                    "SELECT u.first_name || ' ' || u.last_name, c.body, c.time FROM comments c JOIN users u ON u.id = c.user_id WHERE c.post_id = ? ORDER BY c.id",
                    post[0]);
            commentAdapter.setItems(comments);
            sheetBinding.viewEmpty.getRoot().setVisibility(comments.isEmpty() ? View.VISIBLE : View.GONE);
        };
        reload.run();

        sheetBinding.btnSend.setOnClickListener(v -> {
            sheetBinding.tilComment.setError(null);
            String body = sheetBinding.etComment.getText().toString().trim();
            if (body.isEmpty()) {
                sheetBinding.tilComment.setError("Write a comment");
                return;
            }
            ContentValues values = new ContentValues();
            values.put("post_id", post[0]);
            values.put("user_id", Data.me());
            values.put("body", body);
            values.put("time", Data.now());
            Data.db(this).insert("comments", null, values);
            sheetBinding.etComment.setText("");
            reload.run();
        });
        sheetBinding.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileViewActivity.class);
            intent.putExtra("user_id", post[1]);
            startActivity(intent);
        });
        sheetBinding.btnDelete.setOnClickListener(v -> {
            setPostHidden(post[0], 2);
            sheet.dismiss();
            Snackbar.make(binding.getRoot(), "Post deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo", view -> {
                        setPostHidden(post[0], 0);
                        load();
                    })
                    .show();
        });
        sheet.setOnDismissListener(dialog -> load());
        sheet.show();
    }

    private void setPostHidden(String postId, int hidden) {
        ContentValues values = new ContentValues();
        values.put("hidden", hidden);
        Data.db(this).update("posts", values, "id = ?", new String[]{postId});
        load();
    }
}
