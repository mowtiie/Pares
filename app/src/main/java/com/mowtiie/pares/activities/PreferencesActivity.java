package com.mowtiie.pares.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityPreferencesBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class PreferencesActivity extends AppCompatActivity {

    private ActivityPreferencesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPreferencesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String[] pref = Data.rows(this, "SELECT pref_gender, pref_age_min, pref_age_max, pref_location, pref_religion, pref_language, pref_hobbies FROM users WHERE id = ?", Data.me()).get(0);
        setDropdown(binding.ddGender, withAny(Data.GENDERS), pref[0]);
        setDropdown(binding.ddLocation, withAny(Data.LOCATIONS), pref[3]);
        setDropdown(binding.ddReligion, withAny(Data.RELIGIONS), pref[4]);
        setDropdown(binding.ddLanguage, withAny(Data.LANGUAGES), pref[5]);
        binding.etAgeMin.setText(pref[1]);
        binding.etAgeMax.setText(pref[2]);

        ArrayList<String> selected = new ArrayList<>(Arrays.asList(pref[6].split(",")));
        for (String hobby : Data.HOBBIES) {
            Chip chip = new Chip(this);
            chip.setText(hobby);
            chip.setCheckable(true);
            chip.setChecked(selected.contains(hobby));
            binding.chipHobbies.addView(chip);
        }

        binding.btnSave.setOnClickListener(v -> save());
    }

    private String[] withAny(String[] options) {
        String[] result = new String[options.length + 1];
        result[0] = Data.ANY;
        System.arraycopy(options, 0, result, 1, options.length);
        return result;
    }

    private void setDropdown(MaterialAutoCompleteTextView view, String[] options, String current) {
        view.setSimpleItems(options);
        view.setText(current, false);
    }

    private void save() {
        binding.tilAgeMin.setError(null);
        binding.tilAgeMax.setError(null);

        String minText = binding.etAgeMin.getText().toString().trim();
        String maxText = binding.etAgeMax.getText().toString().trim();
        int ageMin = minText.isEmpty() ? 0 : Integer.parseInt(minText);
        int ageMax = maxText.isEmpty() ? 0 : Integer.parseInt(maxText);

        if (ageMin < 18 || ageMin > 99) {
            binding.tilAgeMin.setError("Enter an age from 18 to 99");
            return;
        }
        if (ageMax < ageMin || ageMax > 99) {
            binding.tilAgeMax.setError("Enter an age from " + ageMin + " to 99");
            return;
        }

        StringBuilder hobbies = new StringBuilder();
        for (int i = 0; i < binding.chipHobbies.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipHobbies.getChildAt(i);
            if (chip.isChecked()) {
                hobbies.append(hobbies.length() == 0 ? "" : ",").append(chip.getText());
            }
        }

        Data.savePreferences(this, binding.ddGender.getText().toString(), ageMin, ageMax,
                binding.ddLocation.getText().toString(), binding.ddReligion.getText().toString(),
                binding.ddLanguage.getText().toString(), hobbies.toString());
        Toast.makeText(this, "Preferences saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
