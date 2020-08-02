package com.example.epic.ui.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.epic.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/*
    Deals with users registering using email and passwords.
 */
public class RegisterActivity extends AppCompatActivity {

    // Initialise firebase auth connection
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final String TAG = "RegFrag";
    /*
        Generic filler layout resources
     */
    private EditText email;
    private EditText password;
    private Button createButton;
    private TextView login;
    private EditText confirmPassword;


    // Check to see whether fields meet validation criteria
    private boolean valid;

    // Password and email entered by user.
    private String emailPassed;
    private String passwordPassed;
    private String confirmPasswordPassed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        /*
            Initialise each widget with their associated layout resource id
         */
        email = findViewById(R.id.email_input_register);
        password = findViewById(R.id.password_input_register);
        createButton = findViewById(R.id.create_account);
        confirmPassword = findViewById(R.id.password_input_register_confirm);
        login = findViewById(R.id.return_to_login);

        // This prevents the keyboard from displaying when launching the app
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


    /**
     * Similar logic to login activity
     */
    public void register() {

        if (!validateFields()) {
            registerFailure();
            return;
        }

        createButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(emailPassed, passwordPassed).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If login is successful, close activity
                if(task.isSuccessful()) {
                    registerSuccess();
                } else {
                    // If it was not successful, show error text.
                    registerFailure();
                }
            }
        });
    }

    /*
    Method to valid the users input
    */
    public boolean validateFields() {
        valid = true;

        emailPassed = email.getText().toString();
        passwordPassed = password.getText().toString();
        confirmPasswordPassed = confirmPassword.getText().toString();

        /*
         * Simple logic to check that the email address field is not empty and matches a valid email.
         */
        if (emailPassed.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailPassed).matches()) {
            email.setError("Enter a valid email address");
            valid = false;
        } else {
            email.setError(null);
        }

        /*
         * Checks password field is not empty
         * Checks that the password has at least 1 capital, 1 number and is 7 or more characters long.
         * Chose 7 due to differences in bruteforce hacking..
         * (?=.*[0-9])       # a digit must occur at least once
            (?=.*[a-z])       # a lower case letter must occur at least once
            (?=.*[A-Z])       # an upper case letter must occur at least once
            (?=.*[@#$%^&+=])  # a special character must occur at least once
            (?=\S+$)          # no whitespace allowed in the entire string
            .{8,}             # anything, at least eight places though
            $                 # end-of-string
         */
        if (passwordPassed.isEmpty() ||!passwordPassed.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$")) {
            password.setError("Must be contain 8 or more characters, and include at least one capital letter,one number,1 special character with no spaces");
            valid = false;
        } else {
            password.setError(null);
        }


        if(!passwordPassed.equals(confirmPasswordPassed)){
            valid = false;
            confirmPassword.setError("Passwords do not match");
            Log.d(TAG, "validateFields: " + passwordPassed);
            Log.d(TAG, "validateFields: " + confirmPasswordPassed);
        }

        return valid;
    }

    /*
        If the user failed to login in, notify them
     */
    public void registerFailure() {
        createButton.setEnabled(true);
    }


    /*
        User was successfully authenticated, close activity
     */
    public void registerSuccess() {
        createButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }
}
