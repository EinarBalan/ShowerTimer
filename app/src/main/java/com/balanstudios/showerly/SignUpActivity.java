package com.balanstudios.showerly;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    //shared prefs
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPasswordConfirm;
    private Button buttonCreateAccount;
    private TextView textViewLogIn;
    private ProgressBar progressBar;
    private ImageView imageViewBackground;

    private FirebaseAuth firebaseAuth;
    private String email;
    private String password;

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

        setContentView(R.layout.activity_sign_up);

        imageViewBackground = findViewById(R.id.imageViewBackground);

        Drawable background = getResources().getDrawable(R.drawable.slide2_bg);
        Glide.with(this)
                .load(background)
                .apply(new RequestOptions().centerCrop())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageViewBackground);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordConfirm = findViewById(R.id.editText3PasswordConfirm);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount); buttonCreateAccount.setOnClickListener(onClickListener);
        textViewLogIn = findViewById(R.id.textViewLogIn); textViewLogIn.setOnClickListener(onClickListener);
        progressBar = findViewById(R.id.progressBarLogIn);


        firebaseAuth = FirebaseAuth.getInstance();

    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonCreateAccount:
                registerUser();
                break;
            case R.id.textViewLogIn:
                Intent logInIntent = new Intent(SignUpActivity.this, LogInActivity.class);
                startActivity(logInIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

        }
        }
    };

    private void registerUser() {
        // make sure all required conditions are met
        if (!(isValidEmail(editTextEmail.getText().toString()))) {
            Toast.makeText(this, "Invalid email.", Toast.LENGTH_SHORT).show();
        }
        else if (!(isValidPassword(editTextPassword.getText().toString()))){
            Toast.makeText(this, "Password must be greater than 8 characters.", Toast.LENGTH_SHORT).show();
        }
        else if (!(editTextPassword.getText().toString().equals(editTextPasswordConfirm.getText().toString()))){
            Toast.makeText(this, "Password confirmation does not match.", Toast.LENGTH_SHORT).show();
        }
        else { //if all good then register the user
            email = editTextEmail.getText().toString(); //both final because used in static method
            password = editTextPassword.getText().toString();

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Successfully signed up! Logging in.", Toast.LENGTH_SHORT).show();
                            logIn();
                        }
                        else { //display reason for not working to user
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
        }


    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static boolean isValidPassword(CharSequence target){
        return target.length() >= 8;
    }

    public void logIn(){
        firebaseAuth.signInWithEmailAndPassword(getEmail(), getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    saveUser(email, password);
                    Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
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
