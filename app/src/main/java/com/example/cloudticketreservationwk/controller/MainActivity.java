package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private TextView tvTitle, tvCreateAccount, tvForgotPassword, tvError;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etEmail;
    private MaterialButton btnSubmit;

    private boolean isRegisterMode = false;
    private View divConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        divConfirm = findViewById(R.id.divConfirm);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitle = findViewById(R.id.tvTitle);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvError = findViewById(R.id.tvError);

        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etEmail = findViewById(R.id.etEmail);
        btnSubmit = findViewById(R.id.btnSubmit);

        setMode(false);

        tvCreateAccount.setOnClickListener(v -> setMode(!isRegisterMode));

        tvForgotPassword.setOnClickListener(v ->
                Snackbar.make(findViewById(android.R.id.content), "UI only (no reset logic)", Snackbar.LENGTH_SHORT).show()
        );

        btnSubmit.setOnClickListener(v -> {
            String typed = safe(etEmail).toLowerCase();

            if (typed.contains("admin")) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, EventListActivity.class));
            }
        });
    }

    private void setMode(boolean register) {
        isRegisterMode = register;
        tvError.setVisibility(View.GONE);

        if (register) {
            tvTitle.setText("Register");
            btnSubmit.setText("Create account");
            tilConfirmPassword.setVisibility(View.VISIBLE);
            if (divConfirm != null) divConfirm.setVisibility(View.VISIBLE);
            tvCreateAccount.setText("Back to login");
        } else {
            tvTitle.setText("Login");
            btnSubmit.setText("Login");
            tilConfirmPassword.setVisibility(View.GONE);
            if (divConfirm != null) divConfirm.setVisibility(View.GONE);
            tvCreateAccount.setText("Create an account");
        }
    }

    private String safe(TextInputEditText et) {
        return (et == null || et.getText() == null) ? "" : et.getText().toString().trim();
    }
}