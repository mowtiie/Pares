package com.mowtiie.pares.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityEditProfileBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.etFirstName.setText(Data.USER_FIRST_NAME);
        binding.etLastName.setText(Data.USER_LAST_NAME);
        binding.etAge.setText(Data.USER_AGE);
        binding.etBio.setText(Data.USER_BIO);
        setDropdown(binding.ddGender, Data.GENDERS, Data.USER_GENDER);
        setDropdown(binding.ddLocation, Data.LOCATIONS, Data.USER_LOCATION);
        setDropdown(binding.ddReligion, Data.RELIGIONS, Data.USER_RELIGION);
        setDropdown(binding.ddLanguage, Data.LANGUAGES, Data.USER_LANGUAGE);

        for (String avatar : Data.AVATARS) {
            Chip chip = new Chip(this);
            chip.setText(avatar);
            chip.setTextSize(24);
            chip.setCheckable(true);
            chip.setChecked(avatar.equals(Data.USER_AVATAR));
            binding.chipAvatars.addView(chip);
        }

        ArrayList<String> selected = new ArrayList<>(Arrays.asList(Data.USER_HOBBIES.split(",")));
        for (String hobby : Data.HOBBIES) {
            Chip chip = new Chip(this);
            chip.setText(hobby);
            chip.setCheckable(true);
            chip.setChecked(selected.contains(hobby));
            binding.chipHobbies.addView(chip);
        }

        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setDropdown(com.google.android.material.textfield.MaterialAutoCompleteTextView view, String[] options, String current) {
        view.setSimpleItems(options);
        view.setText(current, false);
    }

    private void save() {
        binding.tilFirstName.setError(null);
        binding.tilLastName.setError(null);
        binding.tilAge.setError(null);
        binding.tilGender.setError(null);
        binding.tilLocation.setError(null);
        binding.tilReligion.setError(null);
        binding.tilLanguage.setError(null);

        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String ageText = binding.etAge.getText().toString().trim();
        String gender = binding.ddGender.getText().toString();
        String location = binding.ddLocation.getText().toString();
        String religion = binding.ddReligion.getText().toString();
        String language = binding.ddLanguage.getText().toString();

        if (firstName.isEmpty()) {
            binding.tilFirstName.setError("Enter your first name");
            return;
        }
        if (lastName.isEmpty()) {
            binding.tilLastName.setError("Enter your last name");
            return;
        }
        int age = ageText.isEmpty() ? 0 : Integer.parseInt(ageText);
        if (age < 18 || age > 99) {
            binding.tilAge.setError("Enter an age from 18 to 99");
            return;
        }
        if (gender.isEmpty()) {
            binding.tilGender.setError("Pick a gender");
            return;
        }
        if (location.isEmpty()) {
            binding.tilLocation.setError("Pick a location");
            return;
        }
        if (religion.isEmpty()) {
            binding.tilReligion.setError("Pick a religion");
            return;
        }
        if (language.isEmpty()) {
            binding.tilLanguage.setError("Pick a language");
            return;
        }

        StringBuilder hobbies = new StringBuilder();
        for (int i = 0; i < binding.chipHobbies.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipHobbies.getChildAt(i);
            if (chip.isChecked()) {
                hobbies.append(hobbies.length() == 0 ? "" : ",").append(chip.getText());
            }
        }
        String avatar = Data.USER_AVATAR;
        for (int i = 0; i < binding.chipAvatars.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipAvatars.getChildAt(i);
            if (chip.isChecked()) {
                avatar = chip.getText().toString();
            }
        }

        Data.saveProfile(this, firstName, lastName, binding.etBio.getText().toString().trim(), age, gender, location, religion, language, hobbies.toString(), avatar);
        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
