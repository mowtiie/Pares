package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivitySearchBinding;
import com.mowtiie.pares.databinding.SheetFiltersBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private GenericAdapter<String[]> adapter;
    private boolean peopleTab = true;
    private String filterGender = Data.ANY;
    private String filterLocation = Data.ANY;
    private String filterReligion = Data.ANY;
    private String filterLanguage = Data.ANY;
    private String filterHobby = Data.ANY;
    private String filterAgeMin = "";
    private String filterAgeMax = "";

    private boolean isPremium() {
        return Data.ROLE_PREMIUM.equals(Data.USER_ROLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnFilters.setText(isPremium() ? "Search to match filters" : "Search to match (Premium)");
        binding.btnFilters.setOnClickListener(v -> {
            if (isPremium()) {
                showFilters();
            } else {
                Snackbar.make(binding.getRoot(), "Search to match is a Premium feature", Snackbar.LENGTH_LONG)
                        .setAction("Upgrade", view -> startActivity(new Intent(this, PremiumActivity.class)))
                        .show();
            }
        });
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
                boolean liked = item[6].equals("1");
                b.btnSecondary.setVisibility(isPremium() ? View.VISIBLE : View.GONE);
                b.btnSecondary.setText(liked ? "Liked" : "Like");
                b.btnSecondary.setEnabled(!liked);
                b.btnSecondary.setOnClickListener(v -> like(item[0], item[1]));
            } else {
                b.tvTitle.setText(item[2]);
                b.tvSubtitle.setText(item[3]);
                b.tvMeta.setText(item[4]);
                b.btnAction.setVisibility(View.GONE);
                b.btnSecondary.setVisibility(View.GONE);
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

    private void like(String targetId, String name) {
        boolean matched = Data.likeFromSearch(this, targetId);
        load();
        if (matched) {
            String firstName = name.split(" ")[0];
            new MaterialAlertDialogBuilder(this)
                    .setTitle("It's a match")
                    .setMessage("You and " + firstName + " liked each other.")
                    .setPositiveButton("Chat", (dialog, which) -> {
                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.putExtra("user_id", targetId);
                        intent.putExtra("name", firstName);
                        startActivity(intent);
                    })
                    .setNegativeButton("Keep browsing", null)
                    .show();
        } else {
            Snackbar.make(binding.getRoot(), "Liked " + name, Snackbar.LENGTH_SHORT).show();
        }
    }

    private String[] withAny(String[] options) {
        String[] result = new String[options.length + 1];
        result[0] = Data.ANY;
        System.arraycopy(options, 0, result, 1, options.length);
        return result;
    }

    private void setDropdown(MaterialAutoCompleteTextView view, String[] options, String current) {
        view.setSimpleItems(withAny(options));
        view.setText(current, false);
    }

    private void showFilters() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        SheetFiltersBinding sheetBinding = SheetFiltersBinding.inflate(getLayoutInflater());
        sheet.setContentView(sheetBinding.getRoot());
        setDropdown(sheetBinding.ddLanguage, Data.LANGUAGES, filterLanguage);
        setDropdown(sheetBinding.ddGender, Data.GENDERS, filterGender);
        setDropdown(sheetBinding.ddLocation, Data.LOCATIONS, filterLocation);
        setDropdown(sheetBinding.ddReligion, Data.RELIGIONS, filterReligion);
        setDropdown(sheetBinding.ddHobby, Data.HOBBIES, filterHobby);
        sheetBinding.etAgeMin.setText(filterAgeMin);
        sheetBinding.etAgeMax.setText(filterAgeMax);

        sheetBinding.btnApply.setOnClickListener(v -> {
            sheetBinding.tilAgeMin.setError(null);
            sheetBinding.tilAgeMax.setError(null);
            String min = sheetBinding.etAgeMin.getText().toString().trim();
            String max = sheetBinding.etAgeMax.getText().toString().trim();
            if (!min.isEmpty() && !max.isEmpty() && Integer.parseInt(max) < Integer.parseInt(min)) {
                sheetBinding.tilAgeMax.setError("Not lower than the minimum");
                return;
            }
            filterLanguage = sheetBinding.ddLanguage.getText().toString();
            filterGender = sheetBinding.ddGender.getText().toString();
            filterLocation = sheetBinding.ddLocation.getText().toString();
            filterReligion = sheetBinding.ddReligion.getText().toString();
            filterHobby = sheetBinding.ddHobby.getText().toString();
            filterAgeMin = min;
            filterAgeMax = max;
            sheet.dismiss();
            load();
        });
        sheetBinding.btnClear.setOnClickListener(v -> {
            filterLanguage = Data.ANY;
            filterGender = Data.ANY;
            filterLocation = Data.ANY;
            filterReligion = Data.ANY;
            filterHobby = Data.ANY;
            filterAgeMin = "";
            filterAgeMax = "";
            sheet.dismiss();
            load();
        });
        sheet.show();
    }

    private void load() {
        String me = Data.me();
        String keyword = "%" + binding.etKeyword.getText().toString().trim() + "%";
        ArrayList<String[]> results;
        if (peopleTab) {
            ArrayList<String> args = new ArrayList<>(Arrays.asList(me, me, keyword, keyword, me, me));
            String sql = "SELECT u.id, u.first_name || ' ' || u.last_name, u.location, u.religion, u.avatar, u.age,"
                    + " EXISTS(SELECT 1 FROM swipes WHERE user_id = ? AND target_id = u.id AND action = 'LIKE') FROM users u"
                    + " WHERE u.id <> ? AND u.role <> 'ADMIN' AND u.status = 'ACTIVE' AND u.visibility = 'PUBLIC'"
                    + " AND (u.first_name LIKE ? OR u.last_name LIKE ?)" + Data.notBlocked("u.id");
            if (isPremium()) {
                if (!filterGender.equals(Data.ANY)) {
                    sql += " AND u.gender = ?";
                    args.add(filterGender);
                }
                if (!filterLocation.equals(Data.ANY)) {
                    sql += " AND u.location = ?";
                    args.add(filterLocation);
                }
                if (!filterReligion.equals(Data.ANY)) {
                    sql += " AND u.religion = ?";
                    args.add(filterReligion);
                }
                if (!filterLanguage.equals(Data.ANY)) {
                    sql += " AND u.language = ?";
                    args.add(filterLanguage);
                }
                if (!filterHobby.equals(Data.ANY)) {
                    sql += " AND (',' || u.hobbies || ',') LIKE ?";
                    args.add("%," + filterHobby + ",%");
                }
                if (!filterAgeMin.isEmpty()) {
                    sql += " AND u.age >= ?";
                    args.add(filterAgeMin);
                }
                if (!filterAgeMax.isEmpty()) {
                    sql += " AND u.age <= ?";
                    args.add(filterAgeMax);
                }
            }
            sql += " ORDER BY u.first_name LIMIT 50";
            results = Data.rows(this, sql, args.toArray(new String[0]));
            binding.viewEmpty.tvEmptyTitle.setText("No people found");
        } else {
            results = Data.rows(this,
                    "SELECT p.id, p.user_id, u.first_name || ' ' || u.last_name, p.body, p.time FROM posts p JOIN users u ON u.id = p.user_id"
                            + " WHERE p.hidden = 0 AND u.status = 'ACTIVE' AND (p.user_id = ? OR u.visibility = 'PUBLIC') AND p.body LIKE ?"
                            + Data.notBlocked("p.user_id") + " ORDER BY p.time DESC, p.id DESC LIMIT 50",
                    me, keyword, me, me);
            binding.viewEmpty.tvEmptyTitle.setText("No posts found");
        }
        binding.viewEmpty.tvEmptyMessage.setText("Try a different keyword or filters.");
        adapter.setItems(results);
        binding.viewEmpty.getRoot().setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
