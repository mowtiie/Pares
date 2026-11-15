package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityMemberHomeBinding;

import java.util.ArrayList;

public class MemberHomeActivity extends AppCompatActivity {

    private static final Class<?>[] SCREENS = {
            DiscoverActivity.class, MatchesActivity.class, NotificationsActivity.class,
            FeedActivity.class, SearchActivity.class, FollowListActivity.class,
            EditProfileActivity.class, PreferencesActivity.class
    };

    private ActivityMemberHomeBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (Data.USER_ID == 0) {
            logout();
            return;
        }
        binding = ActivityMemberHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setOnMenuItemClickListener(item -> {
            logout();
            return true;
        });

        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.tvTitle.setText(item[0]);
            b.tvSubtitle.setText(item[1]);
            b.tvMeta.setText(item[2]);
            b.getRoot().setOnClickListener(v -> startActivity(new Intent(this, SCREENS[position])));
        });
        binding.rvMenu.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenu.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        binding.tvWelcome.setText("Welcome, " + Data.USER_FIRST_NAME + " " + Data.USER_LAST_NAME);
        binding.tvRole.setText("Role: " + Data.USER_ROLE);

        int unseen = Data.unseenCount(this);
        ArrayList<String[]> items = new ArrayList<>();
        items.add(new String[]{"Discover", "Your " + Data.DAILY_LIMIT + " daily suggestions", ""});
        items.add(new String[]{"Matches", "People who liked you back", ""});
        items.add(new String[]{"Notifications", "Likes, matches and messages", unseen + " unseen"});
        items.add(new String[]{"Feed", "Posts, likes and comments", ""});
        items.add(new String[]{"Search", "Find people and posts", ""});
        items.add(new String[]{"Followers and following", "Who follows you", ""});
        items.add(new String[]{"Edit profile", "Bio, hobbies and avatar", ""});
        items.add(new String[]{"Preferences", "Who you want to meet", ""});
        adapter.setItems(items);
    }

    private void logout() {
        Data.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
