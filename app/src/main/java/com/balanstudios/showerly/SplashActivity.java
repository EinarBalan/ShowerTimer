package com.balanstudios.showerly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    //shared prefs
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";

    public FirebaseAuth firebaseAuth;
    public String email = "";
    public String password = "";

    long backPressedTime = 0;
    private LinearLayout buttonLayout;
    private Button buttonLogIn;
    private Button buttonSignUp;
    private ProgressBar progressBarSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorBlack));

        buttonLayout = findViewById(R.id.buttonLayout);
        buttonLogIn = findViewById(R.id.buttonLogIn); buttonLogIn.setOnClickListener(onClickListener);
        buttonSignUp = findViewById(R.id.buttonSignUp); buttonSignUp.setOnClickListener(onClickListener);
        progressBarSplash = findViewById(R.id.progressBarSplash);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();
        email = sharedPreferences.getString(EMAIL, "");
        password = sharedPreferences.getString(PASSWORD, "");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (email.length() > 0 && password.length() > 0){
                    progressBarSplash.setVisibility(View.VISIBLE);
                    logIn(email, password);
                }
                else {
                    buttonLayout.setVisibility(View.VISIBLE);
                    buttonLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_button));
                }
            }
        }, 500);



    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.buttonLogIn:
                    Intent logInIntent = new Intent(SplashActivity.this, LogInActivity.class);
                    startActivity(logInIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;

                case R.id.buttonSignUp:
                    Intent signUpIntent = new Intent(SplashActivity.this, SignUpActivity.class);
                    startActivity(signUpIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
            }


        }
    };

    //prevents accidental exits by user if presses back
    public void onBackPressed() {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            }
            backPressedTime = System.currentTimeMillis();
    }

    public void logIn(String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
                else {
                    progressBarSplash.setVisibility(View.GONE);
                    buttonLayout.setVisibility(View.VISIBLE);
                    buttonLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_button));
                }
            }
        });
    }

}
