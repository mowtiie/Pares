package com.mowtiie.pares.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnReset.setOnClickListener(v -> attemptReset());
    }

    private void attemptReset() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email");
            return;
        }
        if (!Data.emailExists(this, email)) {
            binding.tilEmail.setError("No account uses this email");
            return;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("Use at least 6 characters");
            return;
        }

        Data.updatePassword(this, email, password);
        Toast.makeText(this, "Password updated. Log in with your new password.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
