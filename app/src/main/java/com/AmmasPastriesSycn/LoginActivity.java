package com.AmmasPastriesSycn;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends FragmentActivity {

    private EditText editTextEmail, editTextPassword;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeViews();

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Check if network is available
        if (!NetworkUtils.isNetworkAvailable(LoginActivity.this)) {
            // Display a toast message if there's no internet connection
            Toast.makeText(LoginActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
        } else {
            setupUIElements();
        }
    }

    // Helper method to initialize UI elements
    private void initializeViews() {
        loginButton = findViewById(R.id.button2);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        imageView = findViewById(R.id.imageView);
    }

    // Helper method to set up UI elements and event listeners
    private void setupUIElements() {
        // Set next focus relationships
        editTextEmail.setNextFocusDownId(R.id.editTextTextPassword);
        editTextPassword.setNextFocusUpId(R.id.editTextTextEmailAddress);
        editTextPassword.setNextFocusDownId(R.id.button2);
        loginButton.setNextFocusUpId(R.id.editTextTextPassword);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call the method to authenticate the user
                authenticateUser();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Amma's Pastries Sync", Toast.LENGTH_SHORT).show();
            }
        });

        // Add DPad navigation for the login button
        loginButton.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            // Handle selection or button press
                            authenticateUser();
                            return true;
                        case KeyEvent.KEYCODE_BACK:
                            // Handle the back button press
                            finish();
                            finishAffinity();
                            return true;
                    }
                }
                return false;
            }
        });
    }

    // Method to authenticate the user
    private void authenticateUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Sign in with email and password using Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Authentication success, log and navigate to the main activity
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            navigateToMain();
                        } else {
                            // Authentication failed, display a toast message
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Authentication failed due to an exception, display a toast message
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to navigate to the main activity
    private void navigateToMain() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if a user is logged in; if not, navigate to the login screen
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
        }
    }

    private void navigateToHome() {
        // Navigate to the LoginActivity if no user is logged in
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
