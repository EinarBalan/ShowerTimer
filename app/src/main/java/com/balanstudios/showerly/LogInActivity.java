package com.balanstudios.showerly;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogInActivity extends AppCompatActivity {

    //shared prefs
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";

    private Button buttonDone;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private ProgressBar progressBarLogIn;
    private TextView textViewGetHelp;

    private FirebaseAuth firebaseAuth;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        buttonDone = findViewById(R.id.buttonDone); buttonDone.setOnClickListener(onClickListener);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        progressBarLogIn = findViewById(R.id.progressBarLogIn);
        textViewGetHelp = findViewById(R.id.textViewPasswordHelp); textViewGetHelp.setOnClickListener(onClickListener);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.buttonDone:
                    email = editTextEmail.getText().toString();
                    password = editTextPassword.getText().toString();
                    if (!SignUpActivity.isValidEmail(email)){
                        Toast.makeText(getApplicationContext(), "Invalid email.", Toast.LENGTH_SHORT).show();
                    }
                    else if (password.length() == 0){
                        Toast.makeText(getApplicationContext(), "Invalid password.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        logIn();
                    }
                    break;
                case R.id.textViewPasswordHelp:
                    Intent intent = new Intent(LogInActivity.this, RecoverAccountActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
            }
        }
    };

    public void logIn(){
        firebaseAuth.signInWithEmailAndPassword(getEmail(), getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    progressBarLogIn.setVisibility(View.VISIBLE);
                    saveUser(email, password);

                    Intent mainIntent = new Intent(LogInActivity.this, MainActivity.class);
                    startActivity(mainIntent);
//                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void saveUser(String email, String password){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(EMAIL, email);
        editor.putString(PASSWORD, password);
        editor.apply();
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
