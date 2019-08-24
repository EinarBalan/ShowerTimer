package com.balanstudios.showerly;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    //shared prefs
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String FIRST_RUN = "first run";

    public FirebaseAuth firebaseAuth;
    public String email = "";
    public String password = "";
    private boolean isFirstRun = true;

    long backPressedTime = 0;
    private LinearLayout buttonLayout;
    private Button buttonLogIn;
    private Button buttonSignUp;
    private Button buttonGuest;
    private ProgressBar progressBarSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0x00000000);  // transparent
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            window.addFlags(flags);
        }
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        setContentView(R.layout.activity_splash);

        buttonLayout = findViewById(R.id.buttonLayout);
        buttonLogIn = findViewById(R.id.buttonLogIn); buttonLogIn.setOnClickListener(onClickListener);
        buttonSignUp = findViewById(R.id.buttonSignUp); buttonSignUp.setOnClickListener(onClickListener);
        buttonGuest = findViewById(R.id.buttonGuest); buttonGuest.setOnClickListener(onClickListener);
        progressBarSplash = findViewById(R.id.progressBarSplash);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();
        email = sharedPreferences.getString(EMAIL, "");
        password = sharedPreferences.getString(PASSWORD, "");
        isFirstRun = sharedPreferences.getBoolean(FIRST_RUN, true);


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

        //start intro activity on first run
        if (isFirstRun) {
            Intent introActivity = new Intent(this, IntroActivity.class);
            startActivity(introActivity);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            //don't show intro after first run
            SharedPreferences.Editor editor = sharedPreferences.edit();
            isFirstRun = false;
            editor.putBoolean(FIRST_RUN, isFirstRun);
            editor.apply();
        }

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

                case R.id.buttonGuest:
                    proceedAsGuest();
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

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public void proceedAsGuest(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("Guest users do not have access to many features such as shower history, usage graphs, and the community leaderboards.\n\nWe highly recommend that you sign up with your email to experience all that the app has to offer. \n\nAre you sure you want to continue without making an account?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressBarSplash.setVisibility(View.VISIBLE);
                        firebaseAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Couldn't sign in. Check your network connection.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();

        alertDialog.show();


    }


}
