package com.example.epic.ui.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.epic.R;
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
import com.google.firebase.auth.GoogleAuthProvider;

/*
    LoginActivity handles all the login logic of the application
    If the user is not logged in they will be sent to this activity
 */
public class LoginActivity extends AppCompatActivity {

    // Debugging Tag
    private static final String TAG = "LoginDebug";

    // Initialise firebase auth connection
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Signup Codes, one for email and password, the other for google.
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 1;

    /*
        Generic filler layout resources
     */
    private EditText email;
    private EditText password;
    private Button login;
    private TextView register;
    private ImageView googleSignin;
    private TextView privacyPolicy;
    private TextView terms_service;

    private View dView;

    // Dialog Frag view
    private Dialog dialog;
    private CheckBox gdpr_consent = null;
    private TextView privacy_Policy_Option2;
    private TextView terms;

    // Initialise google sign in client
    private GoogleSignInClient mGoogleSignInClient;

    // Check to see whether fields meet validation criteria
    private boolean valid;
    private boolean gdprConsent = false;

    // Password and email entered by user.
    private String emailPassed;
    private String passwordPassed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*
            Initialise each widget with their associated layout resource id
         */
        email = findViewById(R.id.email_input);
        password = findViewById(R.id.password_input);
        login = findViewById(R.id.login_button);
        register = findViewById(R.id.signup);
        googleSignin = findViewById(R.id.google_signin);
        privacyPolicy = findViewById(R.id.privacy_policy);
        terms_service = findViewById(R.id.terms_service);


        dialog = new Dialog(LoginActivity.this);
        dView = getLayoutInflater().inflate(R.layout.dialog_gdpr_compliance,null);
        gdpr_consent = dView.findViewById(R.id.gdpr_compliance);
        privacy_Policy_Option2 = dView.findViewById(R.id.Checkout_policy);
        terms = dView.findViewById(R.id.terms);

                // This prevents the keyboard from displaying when launching the app
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Calls method to check whether account exists within database
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountExists();
            }
        });

        // If the user needs to sign up, start registration activity
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Privacy policy check
                if(!gdprConsent){
                    dialog.setContentView(dView);
                    privacy_Policy_Option2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                            startActivity(intent);
                        }
                    });
                    terms.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), TermsActivity.class);
                            startActivity(intent);
                        }
                    });
                    gdpr_consent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if(b){
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle("Confirm");
                                builder.setMessage("Are you sure?");

                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing, but close the dialog
                                        gdprConsent = true;
                                        dialog.dismiss();
                                    }
                                });

                                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                AlertDialog alert = builder.create();
                                alert.show();
                                dialog.dismiss();
                            }
                            else{
                                dialog.dismiss();
                            }
                        }
                    });
                    dialog.create();
                    dialog.show();
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivityForResult(intent, REQUEST_SIGNUP);
                }
            }
        });

        // If the user wishes to use google login, call correct method
        googleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!gdprConsent){
                    dialog.setContentView(dView);
                    privacy_Policy_Option2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                            startActivity(intent);
                        }
                    });
                    terms.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), TermsActivity.class);
                            startActivity(intent);
                        }
                    });
                    gdpr_consent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if(b){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setTitle("Confirm");
                                    builder.setMessage("Are you sure?");

                                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Do nothing, but close the dialog
                                            gdprConsent = true;
                                            dialog.dismiss();
                                        }
                                    });

                                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                                    AlertDialog alert = builder.create();
                                    alert.show();
                                dialog.dismiss();
                            }
                            else{
                                dialog.dismiss();
                            }
                        }
                    });
                    dialog.create();
                    dialog.show();
                }
                else {
                    googleLogin();
                }
            }
        });

        /*
            default google sign in options
         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Initialise a new google client for this activity.
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                startActivity(intent);
            }
        });

        terms_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TermsActivity.class);
                startActivity(intent);
            }
        });


    }

    /**
     * Majority of this logic was adapted from the following link
     * https://sourcey.com/articles/beautiful-android-login-and-signup-screens-with-material-design
     * It validates the users input and ensures a smooth transition via the use of a progress dialog box
     */
    public void accountExists(){

        if (!validateFields()) {
            LoginFailure();
            return;
        }

        login.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        emailPassed = email.getText().toString();
        passwordPassed = password.getText().toString();

        /*
           Dependant on the given parameters transition to home fragment(main activity)
           Or, deny access
         */
        mAuth.signInWithEmailAndPassword(emailPassed, passwordPassed).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If login is successful, close activity
                if(task.isSuccessful()) {
                    LoginSuccess();
                    progressDialog.dismiss();
                    //do something with user
                } else {
                    // If it was not successful, show error text.
                    LoginFailure();
                    progressDialog.dismiss();
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
         *
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

        return valid;
    }

    /*
        Initialise google sign in method.
     */
    public void googleLogin(){
        // Gives the option of changing account.
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /*
        If the user failed to login in, notify them
     */
    public void LoginFailure() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        login.setEnabled(true);
    }

    /*
        User was successfully authenticated, close activity
     */
    public void LoginSuccess() {
        login.setEnabled(true);
        finish();
    }

    /*
        Prevent the user from being able to press the back button. Must log in first.
     */
    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity to prevent unauthenticated access
        moveTaskToBack(true);
    }

    /*
        Checks the result of the previous method.
        Dependent on which was used.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                this.finish();
            }
        } else if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    /*
        Default google sign in method
        Same logic as before but with google parameters.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            LoginSuccess();
                            progressDialog.dismiss();
                        } else {
                            // If sign in fails, display a message to the user.
                            LoginFailure();
                            progressDialog.dismiss();
                        }

                        // ...
                    }
                });
    }

}
