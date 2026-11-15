package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityPaymentBinding;

import java.util.Calendar;

public class PaymentActivity extends AppCompatActivity {

    private static final String[] METHODS = {"Online payment", "E-wallet", "Debit card", "Credit card"};

    private ActivityPaymentBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String plan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        plan = getIntent().getStringExtra("plan");
        boolean monthly = Data.PLAN_MONTHLY.equals(plan);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvSummary.setText((monthly ? "Monthly plan: " : "Annual plan: ") + Data.money(monthly ? Data.PRICE_MONTHLY : Data.PRICE_ANNUAL));

        binding.ddMethod.setSimpleItems(METHODS);
        binding.ddMethod.setText(METHODS[0], false);
        showFields();
        binding.ddMethod.setOnItemClickListener((parent, view, position, id) -> showFields());
        binding.btnPay.setOnClickListener(v -> pay());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private boolean isCard() {
        String method = binding.ddMethod.getText().toString();
        return method.equals("Debit card") || method.equals("Credit card");
    }

    private void showFields() {
        String method = binding.ddMethod.getText().toString();
        binding.llCard.setVisibility(isCard() ? View.VISIBLE : View.GONE);
        binding.tilWallet.setVisibility(method.equals("E-wallet") ? View.VISIBLE : View.GONE);
        binding.tilOnlineEmail.setVisibility(method.equals("Online payment") ? View.VISIBLE : View.GONE);
    }

    private boolean validExpiry(String expiry) {
        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return false;
        }
        int month = Integer.parseInt(expiry.substring(0, 2));
        int year = 2000 + Integer.parseInt(expiry.substring(3));
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        return year > currentYear || (year == currentYear && month >= now.get(Calendar.MONTH) + 1);
    }

    private void pay() {
        binding.tilCardNumber.setError(null);
        binding.tilExpiry.setError(null);
        binding.tilCvv.setError(null);
        binding.tilWallet.setError(null);
        binding.tilOnlineEmail.setError(null);
        String method = binding.ddMethod.getText().toString();

        if (isCard()) {
            if (!binding.etCardNumber.getText().toString().matches("\\d{16}")) {
                binding.tilCardNumber.setError("Enter 16 digits");
                return;
            }
            if (!validExpiry(binding.etExpiry.getText().toString().trim())) {
                binding.tilExpiry.setError("Use MM/YY, not expired");
                return;
            }
            if (!binding.etCvv.getText().toString().matches("\\d{3}")) {
                binding.tilCvv.setError("Enter 3 digits");
                return;
            }
        } else if (method.equals("E-wallet")) {
            String mobile = binding.etWallet.getText().toString().trim();
            int digits = mobile.replaceAll("\\D", "").length();
            if (!Patterns.PHONE.matcher(mobile).matches() || digits < 10 || digits > 13) {
                binding.tilWallet.setError("Enter 10 to 13 digits");
                return;
            }
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etOnlineEmail.getText().toString().trim()).matches()) {
            binding.tilOnlineEmail.setError("Enter a valid email");
            return;
        }

        binding.llProcessing.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            Data.upgrade(this, plan, method);
            Toast.makeText(this, "Payment successful. Welcome to Premium!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MemberHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, 2000);
    }
}
