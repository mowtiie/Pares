package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityPremiumBinding;

public class PremiumActivity extends AppCompatActivity {

    private ActivityPremiumBinding binding;
    private String plan = Data.PLAN_MONTHLY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPremiumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvMonthlyPrice.setText(Data.money(Data.PRICE_MONTHLY));
        binding.tvAnnualPrice.setText(Data.money(Data.PRICE_ANNUAL));
        binding.tvAnnualSaving.setText("Save " + Data.money(Data.PRICE_MONTHLY * 12 - Data.PRICE_ANNUAL) + " compared to monthly");
        binding.cardMonthly.setChecked(true);

        binding.cardMonthly.setOnClickListener(v -> select(Data.PLAN_MONTHLY));
        binding.cardAnnual.setOnClickListener(v -> select(Data.PLAN_ANNUAL));
        binding.btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("plan", plan);
            startActivity(intent);
        });
    }

    private void select(String selected) {
        plan = selected;
        binding.cardMonthly.setChecked(Data.PLAN_MONTHLY.equals(selected));
        binding.cardAnnual.setChecked(Data.PLAN_ANNUAL.equals(selected));
    }
}
