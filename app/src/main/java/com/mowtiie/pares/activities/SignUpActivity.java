package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSignUp.setOnClickListener(v -> attemptSignUp());
        binding.btnGoogle.setOnClickListener(v -> socialLogin("Google"));
        binding.btnFacebook.setOnClickListener(v -> socialLogin("Facebook"));
    }

    private void attemptSignUp() {
        binding.tilFirstName.setError(null);
        binding.tilLastName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);

        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        int phoneDigits = phone.replaceAll("\\D", "").length();

        if (firstName.isEmpty()) {
            binding.tilFirstName.setError("Enter your first name");
            return;
        }
        if (lastName.isEmpty()) {
            binding.tilLastName.setError("Enter your last name");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email");
            return;
        }
        if (Data.emailExists(this, email)) {
            binding.tilEmail.setError("This email is already registered");
            return;
        }
        if (!Patterns.PHONE.matcher(phone).matches() || phoneDigits < 10 || phoneDigits > 13) {
            binding.tilPhone.setError("Enter 10 to 13 digits");
            return;
        }
        if (Data.phoneExists(this, phone)) {
            binding.tilPhone.setError("This phone is already registered");
            return;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("Use at least 6 characters");
            return;
        }

        Data.signUp(this, firstName, lastName, email, phone, password);
        Toast.makeText(this, "Welcome to Pares, " + firstName, Toast.LENGTH_SHORT).show();
        openHome();
    }

    private void socialLogin(String provider) {
        Data.login(this, Data.SOCIAL_EMAIL, Data.DEMO_PASSWORD);
        Toast.makeText(this, "Signed in with " + provider, Toast.LENGTH_SHORT).show();
        openHome();
    }

    private void openHome() {
        Intent intent = new Intent(this, MemberHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
