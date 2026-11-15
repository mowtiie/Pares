package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.btnForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        binding.btnSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        binding.btnGoogle.setOnClickListener(v -> socialLogin("Google"));
        binding.btnFacebook.setOnClickListener(v -> socialLogin("Facebook"));
    }

    private void attemptLogin() {
        binding.tilIdentifier.setError(null);
        binding.tilPassword.setError(null);
        String identifier = binding.etIdentifier.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        if (identifier.isEmpty()) {
            binding.tilIdentifier.setError("Enter your email or phone");
            return;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Enter your password");
            return;
        }

        int result = Data.login(this, identifier, password);
        if (result == Data.LOGIN_INVALID) {
            binding.tilPassword.setError("Wrong email, phone or password");
        } else if (result == Data.LOGIN_SUSPENDED) {
            Snackbar.make(binding.getRoot(), "This account is suspended", Snackbar.LENGTH_LONG).show();
        } else {
            openHome();
        }
    }

    private void socialLogin(String provider) {
        Data.login(this, Data.SOCIAL_EMAIL, Data.DEMO_PASSWORD);
        Toast.makeText(this, "Signed in with " + provider, Toast.LENGTH_SHORT).show();
        openHome();
    }

    private void openHome() {
        Class<?> home = Data.ROLE_ADMIN.equals(Data.USER_ROLE) ? AdminHomeActivity.class : MemberHomeActivity.class;
        Intent intent = new Intent(this, home);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
