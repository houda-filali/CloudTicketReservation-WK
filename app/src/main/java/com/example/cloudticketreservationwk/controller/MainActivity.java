package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.firebase.Constants;
import com.example.cloudticketreservationwk.firebase.PhoneAuthService;
import com.example.cloudticketreservationwk.firebase.RoleService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private TextView tvTitle, tvCreateAccount, tvForgotPassword, tvError;

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private TextInputLayout tilConfirmPassword;
    private View divConfirm;

    private MaterialButton btnSubmit;

    private boolean isRegisterMode = false;

    private final AuthService authService = new AuthService();
    private final PhoneAuthService phoneAuthService = new PhoneAuthService();
    private final RoleService roleService = new RoleService();

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

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        setMode(false);

        tvCreateAccount.setOnClickListener(v -> setMode(!isRegisterMode));

        tvForgotPassword.setOnClickListener(v -> showError("tba"));

        btnSubmit.setOnClickListener(v -> {
            hideError();

            String login = safe(etEmail);
            if (login.isEmpty()) {
                showError("Email / phone number is required");
                return;
            }

            if (looksLikeEmail(login)) {
                String pass = safe(etPassword);

                if (isRegisterMode) {
                    String confirm = safe(etConfirmPassword);
                    if (pass.isEmpty()) { showError("Password is required"); return; }
                    if (confirm.isEmpty()) { showError("Confirm password is required"); return; }
                    if (!pass.equals(confirm)) { showError("Passwords do not match"); return; }
                    registerWithEmail(login, pass);
                } else {
                    if (pass.isEmpty()) { showError("Password is required"); return; }
                    loginWithEmail(login, pass);
                }
            } else {
                if (isRegisterMode) registerWithPhone(login);
                else loginWithPhone(login);
            }
        });
    }

    private void loginWithEmail(String email, String password) {
        authService.loginWithEmail(email.trim(), password, new AuthService.UserCallback() {
            @Override public void onSuccess(FirebaseUser user) {
                routeByRoleIfProfileExists(user);
            }
            @Override public void onError(Exception e) {
                showError(mapAuthError(e));
            }
        });
    }

    private void registerWithEmail(String email, String password) {
        authService.registerWithEmail(email.trim(), password, new AuthService.UserCallback() {
            @Override public void onSuccess(FirebaseUser user) {
                routeByRoleIfProfileExists(user);
            }
            @Override public void onError(Exception e) {
                showError(mapAuthError(e));
            }
        });
    }

    private void loginWithPhone(String rawPhone) {
        String phoneE164 = toE164(rawPhone);
        if (phoneE164 == null) { showError("Enter a valid phone number"); return; }

        roleService.userProfileExistsByPhone(phoneE164, new RoleService.ExistsCallback() {
            @Override public void onResult(boolean exists) {
                if (!exists) {
                    showError("Phone number not registered. Please create an account first.");
                    return;
                }
                startPhoneOtp(phoneE164);
            }
            @Override public void onError(Exception e) { showError(mapAuthError(e)); }
        });
    }

    private void registerWithPhone(String rawPhone) {
        String phoneE164 = toE164(rawPhone);
        if (phoneE164 == null) { showError("Enter a valid phone number"); return; }

        roleService.userProfileExistsByPhone(phoneE164, new RoleService.ExistsCallback() {
            @Override public void onResult(boolean exists) {
                if (exists) {
                    showError("Phone number already registered. Please login instead.");
                    return;
                }
                startPhoneOtp(phoneE164);
            }
            @Override public void onError(Exception e) { showError(mapAuthError(e)); }
        });
    }

    private void startPhoneOtp(String phoneE164) {
        phoneAuthService.startPhoneVerification(phoneE164, MainActivity.this, new PhoneAuthService.StartCallback() {
            @Override public void onCodeSent(String verificationId) { showOtpDialog(verificationId); }
            @Override public void onAutoVerified(FirebaseUser user) { routeByRoleIfProfileExists(user); }
            @Override public void onError(Exception e) { showError(mapAuthError(e)); }
        });
    }

    private void showOtpDialog(String verificationId) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("6-digit code");

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Enter verification code")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Verify", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = input.getText() == null ? "" : input.getText().toString().trim();
            if (code.isEmpty()) { input.setError("Required"); return; }

            phoneAuthService.verifyOtp(verificationId, code, new PhoneAuthService.VerifyCallback() {
                @Override public void onSuccess(FirebaseUser user) {
                    dialog.dismiss();
                    routeByRoleIfProfileExists(user);
                }
                @Override public void onError(Exception e) { showError(mapAuthError(e)); }
            });
        }));

        dialog.show();
    }

    private void routeByRoleIfProfileExists(FirebaseUser user) {
        roleService.getUserRole(user.getUid(), new RoleService.RoleCallback() {
            @Override public void onSuccess(String role) {
                if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
                    startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, EventListActivity.class));
                }
            }

            @Override public void onNotFound() {
                authService.logout();
                showError("User not registered. Please create an account first.");
            }

            @Override public void onError(Exception e) {
                showError(mapAuthError(e));
            }
        });
    }

    private String mapAuthError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) return "User not found. Please create an account first.";
        if (e instanceof FirebaseAuthInvalidCredentialsException) return "Incorrect password";
        if (e instanceof FirebaseNetworkException) return "Network error. Try again.";

        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (msg.contains("already in use")) return "Account already exists. Please login instead.";
        if (msg.contains("password is invalid")) return "Incorrect email or password";
        if (msg.contains("no user record")) return "User not found. Please create an account first.";

        msg = e.getMessage();
        return (msg != null && !msg.trim().isEmpty()) ? msg : "Auth failed. Try again.";
    }

    private void setMode(boolean register) {
        isRegisterMode = register;
        hideError();

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

    private boolean looksLikeEmail(String input) {
        return input != null && input.contains("@");
    }

    private String toE164(String input) {
        if (input == null) return null;
        String t = input.trim();
        if (t.isEmpty()) return null;

        if (t.startsWith("+")) {
            String digits = t.substring(1).replaceAll("\\D", "");
            if (digits.length() < 10 || digits.length() > 15) return null;
            return "+" + digits;
        }

        String digits = t.replaceAll("\\D", "");
        if (digits.length() == 10) return "+1" + digits;
        if (digits.length() == 11 && digits.startsWith("1")) return "+" + digits;
        return null;
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

    private String safe(TextInputEditText et) {
        return (et == null || et.getText() == null) ? "" : et.getText().toString().trim();
    }
}