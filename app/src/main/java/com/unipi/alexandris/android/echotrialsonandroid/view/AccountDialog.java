package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.data.PlayerStatistics;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;
import com.unipi.alexandris.android.echotrialsonandroid.levelcreator.LevelCreatorActivity;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;

import java.util.Objects;
import java.util.Random;
import java.util.HashMap;
import java.util.Date;

public class AccountDialog extends Dialog {

    private static final int RC_SIGN_IN = 9001;

    private final Context context;
    private final SessionManager sessionManager;
    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;

    // UI Components
    private LinearLayout loginSection;
    private LinearLayout emailSignInSection;
    private LinearLayout emailSignUpSection;
    private LinearLayout usernameSetupSection;
    private LinearLayout accountInfoSection;

    // Login Section
    private Button btnGoogleSignIn;
    private Button btnEmailSignIn;
    private Button btnEmailSignUp;

    // Email Sign In Section
    private EditText etSignInEmail;
    private EditText etSignInPassword;
    private Button btnConfirmSignIn;
    private Button btnBackToLogin;

    // Email Sign Up Section
    private EditText etSignUpUsername;
    private EditText etSignUpEmail;
    private EditText etSignUpPassword;
    private EditText etSignUpConfirmPassword;
    private Button btnConfirmSignUp;
    private Button btnBackToLoginFromSignUp;

    // Username Setup Section (for Google users)
    private EditText etUsername;
    private Button btnConfirm;

    // Account Info Section
    private TextView tvUserDisplayName;
    private TextView tvUsername;
    private TextView tvUserEmail;
    private TextView tvUserId;
    private Button btnSignOut;

    // Common UI
    private ImageButton windowCloseButton;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;

    // Listener
    private AccountDialogListener listener;
    
    // Pending Google account for new user setup
    private GoogleSignInAccount pendingGoogleAccount;
    private String pendingGoogleIdToken;
    private FirebaseUser pendingFirebaseUser;

    public interface AccountDialogListener {
        void onAuthenticationSuccess(FirebaseUser user);
        void onAuthenticationFailure(String error);
        void onSignOut();
    }

    public AccountDialog(Context context, SessionManager sessionManager) {
        super(context);
        this.context = context;
        this.sessionManager = sessionManager;
        this.firebaseAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("999460377760-ll8f72isv7uddiifvkrm55jsb32d7vc3.apps.googleusercontent.com")
                .build();
        this.googleSignInClient = GoogleSignIn.getClient(context, gso);

        initializeDialog();
        setupViews();
        setupListeners();
        updateUI();
    }

    @Override
    public void show() {
        super.show();
        
        // Always check current session state when dialog opens
        updateUI();
    }

    @Override
    public void dismiss() {
        // Check if we need to delete a new Firebase account before dismissing
        if (pendingGoogleAccount != null && pendingFirebaseUser != null) {
            System.out.println("🗑️ ACCOUNT: User dismissed dialog, deleting new Firebase account...");
            
            // Delete the Firebase account
            pendingFirebaseUser.delete()
                    .addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            System.out.println("ACCOUNT: New Firebase account deleted successfully");
                        } else {
                            System.out.println("ACCOUNT: Failed to delete Firebase account: " +
                                (deleteTask.getException() != null ? deleteTask.getException().getMessage() : "Unknown error"));
                        }
                        // Clear data and dismiss regardless of delete success
                        clearAllData();
                        super.dismiss();
                    });
        } else {
            // No new account to delete, just clear and dismiss
            clearAllData();
            super.dismiss();
        }
    }

    private void initializeDialog() {
        setContentView(R.layout.dialog_account);
        setCancelable(true);
        setCanceledOnTouchOutside(false);

        // Set dialog dimensions using dp values like other dialogs
        WindowManager.LayoutParams params = Objects.requireNonNull(getWindow()).getAttributes();
        float density = context.getResources().getDisplayMetrics().density;
        
        // Get screen dimensions to ensure dialog doesn't exceed screen
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;
        
        // Calculate dialog dimensions with max constraints
        int width = (int) (density * 600); // 600dp width
        int height = (int) (density * 500); // 500dp height, but will be constrained
        
        // Ensure dialog doesn't exceed screen bounds
        width = Math.min(width, screenWidth - (int)(density * 40)); // Leave 20dp margin on each side
        height = Math.min(height, screenHeight - (int)(density * 80)); // Leave 40dp margin on top and bottom
        
        params.width = width;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT; // Let content determine height
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);
    }

    private void setupViews() {
        // Main sections
        loginSection = findViewById(R.id.loginSection);
        emailSignInSection = findViewById(R.id.emailSignInSection);
        emailSignUpSection = findViewById(R.id.emailSignUpSection);
        usernameSetupSection = findViewById(R.id.usernameSetupSection);
        accountInfoSection = findViewById(R.id.accountInfoSection);

        // Login section
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnEmailSignIn = findViewById(R.id.btnEmailSignIn);
        btnEmailSignUp = findViewById(R.id.btnEmailSignUp);

        // Email sign in section
        etSignInEmail = findViewById(R.id.etSignInEmail);
        etSignInPassword = findViewById(R.id.etSignInPassword);
        btnConfirmSignIn = findViewById(R.id.btnConfirmSignIn);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Email sign up section
        etSignUpUsername = findViewById(R.id.etSignUpUsername);
        etSignUpEmail = findViewById(R.id.etSignUpEmail);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etSignUpConfirmPassword = findViewById(R.id.etSignUpConfirmPassword);
        btnConfirmSignUp = findViewById(R.id.btnConfirmSignUp);
        btnBackToLoginFromSignUp = findViewById(R.id.btnBackToLoginFromSignUp);

        // Username setup section
        etUsername = findViewById(R.id.etUsername);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Account info section
        tvUserDisplayName = findViewById(R.id.tvUserDisplayName);
        tvUsername = findViewById(R.id.tvUsername);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserId = findViewById(R.id.tvUserId);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Common UI
        windowCloseButton = findViewById(R.id.windowCloseButton);
        progressBar = findViewById(R.id.progressBar);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupListeners() {
        // Apply touch animations to all buttons
        View.OnTouchListener touchListener = LevelCreatorActivity.createButtonTapFeedback();

        // Close button - follow established pattern from MultiPropertiesDialog
        if (windowCloseButton != null) {
            windowCloseButton.setOnTouchListener(LevelCreatorActivity.createButtonTapFeedback());
            ButtonSoundHelper.addClickSound(windowCloseButton, v -> handleDialogClose());
        }

        // Login section
        btnGoogleSignIn.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnGoogleSignIn, v -> signInWithGoogle());

        btnEmailSignIn.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnEmailSignIn, v -> showEmailSignInSection());

        btnEmailSignUp.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnEmailSignUp, v -> showEmailSignUpSection());

        // Email sign in section
        btnConfirmSignIn.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnConfirmSignIn, v -> signInWithEmail());

        btnBackToLogin.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnBackToLogin, v -> showLoginSection());

        // Email sign up section
        btnConfirmSignUp.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnConfirmSignUp, v -> signUpWithEmail());

        btnBackToLoginFromSignUp.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnBackToLoginFromSignUp, v -> showLoginSection());

        // Username setup section
        btnConfirm.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnConfirm, v -> confirmAndClose());

        // Account info section
        btnSignOut.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnSignOut, v -> signOut());
    }

    public void updateUI() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in
            showAccountInfoSection();
            updateAccountInfo(currentUser);
        } else {
            // User is signed out
            showLoginSection();
        }
    }

    private void showLoginSection() {
        loginSection.setVisibility(View.VISIBLE);
        emailSignInSection.setVisibility(View.GONE);
        emailSignUpSection.setVisibility(View.GONE);
        usernameSetupSection.setVisibility(View.GONE);
        accountInfoSection.setVisibility(View.GONE);
        hideError();
    }

    private void showEmailSignInSection() {
        loginSection.setVisibility(View.GONE);
        emailSignInSection.setVisibility(View.VISIBLE);
        emailSignUpSection.setVisibility(View.GONE);
        usernameSetupSection.setVisibility(View.GONE);
        accountInfoSection.setVisibility(View.GONE);
        hideError();
    }

    private void showEmailSignUpSection() {
        loginSection.setVisibility(View.GONE);
        emailSignInSection.setVisibility(View.GONE);
        emailSignUpSection.setVisibility(View.VISIBLE);
        usernameSetupSection.setVisibility(View.GONE);
        accountInfoSection.setVisibility(View.GONE);
        hideError();
    }

    private void showUsernameSetupSection() {
        loginSection.setVisibility(View.GONE);
        emailSignInSection.setVisibility(View.GONE);
        emailSignUpSection.setVisibility(View.GONE);
        usernameSetupSection.setVisibility(View.VISIBLE);
        accountInfoSection.setVisibility(View.GONE);
        hideError();
    }

    private void showAccountInfoSection() {
        loginSection.setVisibility(View.GONE);
        emailSignInSection.setVisibility(View.GONE);
        emailSignUpSection.setVisibility(View.GONE);
        usernameSetupSection.setVisibility(View.GONE);
        accountInfoSection.setVisibility(View.VISIBLE);
        hideError();
    }

    // Google Sign-In Methods
    private void signInWithGoogle() {
        showLoading();
        hideError();
        
        // Sign out first to force account picker to show
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // Now start the sign-in process
            Intent signInIntent = googleSignInClient.getSignInIntent();
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).startActivityForResult(signInIntent, RC_SIGN_IN);
            } else {
                showError("Cannot start Google Sign-In from this context");
                hideLoading();
            }
        });
    }

    public void handleGoogleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            checkUserExistsAndProceed(account);
        } catch (ApiException e) {
            System.out.println("ACCOUNT: Google sign in failed: " + e.getStatusCode());
            showError("Google sign in failed: " + e.getStatusCode());
            hideLoading();
            if (listener != null) { listener.onAuthenticationFailure("Google sign in failed: " + e.getStatusCode()); }
        }
    }

    private void checkUserExistsAndProceed(GoogleSignInAccount googleAccount) {
        System.out.println("ACCOUNT: Starting Google Firebase auth for: " + googleAccount.getEmail());
        
        // Store the Google account info immediately
        this.pendingGoogleAccount = googleAccount;
        this.pendingGoogleIdToken = googleAccount.getIdToken();
        
        // Create Firebase account using Google credentials
        AuthCredential credential = GoogleAuthProvider.getCredential(googleAccount.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        System.out.println("ACCOUNT: Google Firebase auth successful for: " + user.getEmail() + " (UID: " + user.getUid() + ")");
                        
                        // Store the Firebase user
                        this.pendingFirebaseUser = user;
                        
                        // Search database to see if user has saved data
                        checkIfUserHasData(user);
                    } else {
                        hideLoading();
                        String errorMessage = "Google authentication failed: " + authTask.getException().getMessage();
                        System.out.println("ACCOUNT: " + errorMessage);
                        showError(errorMessage);
                        if (listener != null) { listener.onAuthenticationFailure(errorMessage); }
                    }
                });
    }

    private void checkIfUserHasData(FirebaseUser user) {
        System.out.println("🔍 ACCOUNT: Checking if user has data in Firestore for UID: " + user.getUid());
        
        sessionManager.getFirestore()
                .collection("users")
                .document(user.getUid())
                .collection("data")
                .document("statistics")
                .get()
                .addOnCompleteListener(checkTask -> {
                    if (checkTask.isSuccessful()) {
                        if (checkTask.getResult() != null && checkTask.getResult().exists()) {
                            // User has saved data - download and continue to user info
                            System.out.println("ACCOUNT: User has existing data, downloading...");
                            sessionManager.downloadPlayerStatistics(null);
                            // Clear pending data since this is an existing user
                            pendingGoogleAccount = null;
                            pendingGoogleIdToken = null;
                            pendingFirebaseUser = null;
                            hideLoading(); // Hide loading for existing users
                            updateUI();
                            if (listener != null) { listener.onAuthenticationSuccess(user); }
                        } else {
                            // New user - show username setup with randomly generated username
                            System.out.println("ACCOUNT: New user, showing username setup...");
                            setupUsernameSectionForNewUser();
                            showUsernameSetupSection();
                            hideLoading();
                        }
                    } else {
                        System.out.println("ACCOUNT: Error checking user data: " +
                            (checkTask.getException() != null ? checkTask.getException().getMessage() : "Unknown error"));
                        // Treat as new user if we can't check
                        System.out.println("ACCOUNT: Treating as new user due to check error...");
                        setupUsernameSectionForNewUser();
                        showUsernameSetupSection();
                        hideLoading();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setupUsernameSectionForNewUser() {
        // Generate suggested username using stored Google account
        String suggestedUsername = generateSuggestedUsername(pendingGoogleAccount);
        etUsername.setText(suggestedUsername);
        
        // Update description text
        TextView tvUsernameDescription = findViewById(R.id.tvUsernameDescription);
        if (tvUsernameDescription != null) {
            tvUsernameDescription.setText("Choose your username for: " + pendingGoogleAccount.getEmail());
        }
    }

    private void signInWithEmail() {
        String email = etSignInEmail.getText().toString().trim();
        String password = etSignInPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        showLoading();
        hideError();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        assert user != null;
                        System.out.println("ACCOUNT: Email sign in successful for: " + user.getEmail());

                        // Download from Firebase for existing users (preserves local data if download fails)
                        sessionManager.downloadPlayerStatistics(null);

                        if (listener != null) { listener.onAuthenticationSuccess(user); }
                        updateUI();
                    } else {
                        String errorMessage = "Sign in failed: " + Objects.requireNonNull(task.getException()).getMessage();
                        System.out.println("ACCOUNT: " + errorMessage);
                        showError(errorMessage);
                        if (listener != null) { listener.onAuthenticationFailure(errorMessage); }
                    }
                });
    }

    private void signUpWithEmail() {
        String username = etSignUpUsername.getText().toString().trim();
        String email = etSignUpEmail.getText().toString().trim();
        String password = etSignUpPassword.getText().toString();
        String confirmPassword = etSignUpConfirmPassword.getText().toString();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return;
        }

        showLoading();
        hideError();

        // First check if username is available
        checkUsernameAvailability(username, available -> {
            if (!available) {
                hideLoading();
                showError("Username already exists");
                return;
            }

            // Check if email is already in use
            checkEmailAvailability(email, emailAvailable -> {
                if (!emailAvailable) {
                    hideLoading();
                    showError("Email already registered");
                    return;
                }

                // Create account
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                hideLoading();
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    assert user != null;
                                    System.out.println("ACCOUNT: Email sign up successful for: " + user.getEmail());

                                    // Keep local data and add username for new users
                                    PlayerStatistics stats = sessionManager.getPlayerStatistics();
                                    if (stats == null) {
                                        stats = new PlayerStatistics(user.getUid());
                                    }
                                    stats.setUsername(username);
                                    stats.setUserId(user.getUid());
                                    sessionManager.updatePlayerStatistics(stats);

                                    // Upload the new account data
                                    sessionManager.uploadPlayerStatistics(uploadTask -> {
                                        if (uploadTask.isSuccessful()) {
                                            System.out.println("☁ACCOUNT: New account data uploaded successfully");
                                        } else {
                                            System.out.println("ACCOUNT: Failed to upload new account data: " +
                                                (uploadTask.getException() != null ? uploadTask.getException().getMessage() : "Unknown error"));
                                        }
                                    });

                                    if (listener != null) { listener.onAuthenticationSuccess(user); }
                                    updateUI();
                                } else {
                                    String errorMessage = "Account creation failed: " + Objects.requireNonNull(task.getException()).getMessage();
                                    System.out.println("ACCOUNT: " + errorMessage);
                                    showError(errorMessage);
                                    if (listener != null) { listener.onAuthenticationFailure(errorMessage); }
                                }
                            }
                        });
            });
        });
    }

    // Username Management
    private void setupUsernameSection(FirebaseUser user) {
        String suggestedUsername = generateSuggestedUsername(user);
        etUsername.setText(suggestedUsername);
    }

    private String generateSuggestedUsername(FirebaseUser user) {
        String baseName = "Player";
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            baseName = user.getDisplayName().replaceAll("[^a-zA-Z0-9]", "");
            if (baseName.isEmpty()) baseName = "Player";
        }
        return baseName + generateRandomSuffix();
    }

    private String generateSuggestedUsername(GoogleSignInAccount account) {
        String baseName = "Player";
        if (account.getDisplayName() != null && !account.getDisplayName().isEmpty()) {
            baseName = account.getDisplayName().replaceAll("[^a-zA-Z0-9]", "");
            if (baseName.isEmpty()) baseName = "Player";
        }
        return baseName + generateRandomSuffix();
    }

    private String generateRandomSuffix() {
        Random random = new Random();
        return String.valueOf(random.nextInt(1000));
    }



    private void checkUsernameAvailability(String username, OnUsernameCheckListener listener) {
        sessionManager.getFirestore()
                .collection("usernames")
                .document(username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean available = !task.getResult().exists();
                        listener.onResult(available);
                    } else {
                        System.out.println("ACCOUNT: Error checking username availability: " + Objects.requireNonNull(task.getException()).getMessage());
                        listener.onResult(false);
                    }
                });
    }

    private void checkEmailAvailability(String email, OnEmailCheckListener listener) {
        listener.onResult(true);
    }

    private void confirmAndClose() {
        if (pendingGoogleAccount != null) {
            // New Google user - validate and save username, then complete account creation
            saveUsernameAndCompleteSetup();
        } else {
            // Regular username update for existing user
            sessionManager.uploadPlayerStatistics(task -> {
                if (task.isSuccessful()) {
                    System.out.println("☁ACCOUNT: Username setup completed and uploaded");
                } else {
                    System.out.println("ACCOUNT: Failed to upload username setup: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            });
            dismiss();
        }
    }

    private void saveUsernameAndCompleteSetup() {
        String username = etUsername.getText().toString().trim();
        
        if (username.isEmpty()) {
            showError("Please enter a username");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }

        showLoading();
        hideError();

        checkUsernameAvailability(username, available -> {
            if (available) {
                // Username is available - proceed with account creation
                createUsernameDocumentAndCompleteSetup(username);
            } else {
                hideLoading();
                showError("Username already exists");
            }
        });
    }

    private void createUsernameDocumentAndCompleteSetup(String username) {
        // Create the username document
        HashMap<String, Object> usernameData = new HashMap<>();
        usernameData.put("email", pendingGoogleAccount.getEmail());
        usernameData.put("createdAt", new Date());
        
        sessionManager.getFirestore()
                .collection("usernames")
                .document(username)
                .set(usernameData)
                .addOnCompleteListener(usernameTask -> {
                    if (usernameTask.isSuccessful()) {
                        // Username reserved successfully - complete account setup
                        completeNewUserSetup(username);
                    } else {
                        hideLoading();
                        showError("Failed to reserve username: " + usernameTask.getException().getMessage());
                    }
                });
    }

    private void completeNewUserSetup(String username) {
        // Keep local data and add user ID and username
        PlayerStatistics stats = sessionManager.getPlayerStatistics();
        if (stats == null) {
            stats = new PlayerStatistics(pendingFirebaseUser.getUid());
        }
        stats.setUserId(pendingFirebaseUser.getUid());
        stats.setUsername(username);
        sessionManager.updatePlayerStatistics(stats);

        // Upload the new account data
        sessionManager.uploadPlayerStatistics(uploadTask -> {
            hideLoading();
            if (uploadTask.isSuccessful()) {
                System.out.println("ACCOUNT: New Google account data uploaded successfully");
                // Clear pending data
                FirebaseUser user = pendingFirebaseUser; // Store reference before clearing
                pendingGoogleAccount = null;
                pendingGoogleIdToken = null;
                pendingFirebaseUser = null;
                updateUI();
                if (listener != null) { listener.onAuthenticationSuccess(user); }
            } else {
                System.out.println("ACCOUNT: Failed to upload new account data: " +
                    (uploadTask.getException() != null ? uploadTask.getException().getMessage() : "Unknown error"));
                showError("Account created but failed to upload data");
                // Clean up the username document since upload failed
                sessionManager.getFirestore()
                        .collection("usernames")
                        .document(username)
                        .delete();
            }
        });
    }

    // Sign Out
    private void signOut() {
        firebaseAuth.signOut();
        googleSignInClient.signOut();

        System.out.println("ACCOUNT: User signed out");

        if (listener != null) {
            listener.onSignOut();
        }

        updateUI();
    }

    // UI Helper Methods
    @SuppressLint("SetTextI18n")
    private void updateAccountInfo(FirebaseUser user) {
        // Always hide the display name section
        tvUserDisplayName.setVisibility(View.GONE);

        PlayerStatistics stats = sessionManager.getPlayerStatistics();
        String username = (stats != null && stats.getUsername() != null) ? stats.getUsername() : "Not set";
        tvUsername.setText("Username: " + username);
        tvUserEmail.setText("Email: " + (user.getEmail() != null ? user.getEmail() : "Anonymous"));
        tvUserId.setText("User ID: " + user.getUid().substring(0, Math.min(8, user.getUid().length())) + "...");
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        setButtonsEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnGoogleSignIn.setEnabled(enabled);
        btnEmailSignIn.setEnabled(enabled);
        btnEmailSignUp.setEnabled(enabled);
        btnConfirmSignIn.setEnabled(enabled);
        btnBackToLogin.setEnabled(enabled);
        btnConfirmSignUp.setEnabled(enabled);
        btnBackToLoginFromSignUp.setEnabled(enabled);
        btnConfirm.setEnabled(enabled);
        btnSignOut.setEnabled(enabled);
        windowCloseButton.setEnabled(enabled);
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvErrorMessage.setVisibility(View.GONE);
    }

    /**
     * Handles dialog close - checks if we need to delete a new Firebase account
     */
    private void handleDialogClose() {
        // If we have pending Google account and Firebase user, it means we created a new account
        // that the user is now canceling - we should delete it
        if (pendingGoogleAccount != null && pendingFirebaseUser != null) {
            System.out.println("🗑️ ACCOUNT: User canceled username setup, deleting new Firebase account...");
            
            // Delete the Firebase account
            pendingFirebaseUser.delete()
                    .addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            System.out.println("ACCOUNT: New Firebase account deleted successfully");
                        } else {
                            System.out.println("ACCOUNT: Failed to delete Firebase account: " +
                                (deleteTask.getException() != null ? deleteTask.getException().getMessage() : "Unknown error"));
                        }
                        // Clear data and dismiss regardless of delete success
                        clearAllData();
                        super.dismiss();
                    });
        } else {
            // No new account to delete, just clear and dismiss
            clearAllData();
            super.dismiss();
        }
    }

    /**
     * Clears all input fields and error messages when dialog is closed
     */
    private void clearAllData() {
        // Clear all EditText fields
        if (etSignInEmail != null) etSignInEmail.setText("");
        if (etSignInPassword != null) etSignInPassword.setText("");
        if (etSignUpUsername != null) etSignUpUsername.setText("");
        if (etSignUpEmail != null) etSignUpEmail.setText("");
        if (etSignUpPassword != null) etSignUpPassword.setText("");
        if (etSignUpConfirmPassword != null) etSignUpConfirmPassword.setText("");
        if (etUsername != null) etUsername.setText("");
        
        // Clear pending Google account
        pendingGoogleAccount = null;
        pendingGoogleIdToken = null;
        pendingFirebaseUser = null;
        
        // Clear error message
        hideError();
        
        // Reset to login section
        showLoginSection();
    }




    // Interfaces
    public interface OnUsernameCheckListener {
        void onResult(boolean available);
    }

    public interface OnEmailCheckListener {
        void onResult(boolean available);
    }

    // Public Methods
    public void setAccountDialogListener(AccountDialogListener listener) {
        this.listener = listener;
    }

    public static int getGoogleSignInRequestCode() {
        return RC_SIGN_IN;
    }
    
}
