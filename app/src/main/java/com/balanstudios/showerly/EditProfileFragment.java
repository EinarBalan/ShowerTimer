package com.balanstudios.showerly;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    private MainActivity mainActivity;

    private TextView textViewAddCity;
    private TextView textViewSendEmail;

    private ImageButton buttonSettings;
    private ImageButton buttonBack;
    private Button buttonApplyChanges;
    private Button buttonLogOut;

    private EditText editTextDisplayName;
    private EditText editTextGoalTime;

    private ProgressBar progressBar;

    //firebase

    //location
    private int COURSE_LOCATION_PERMISSION_CODE = 1;

    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mainActivity = (MainActivity) getActivity();
//        TimeFormat timeFormat = new TimeFormat(editTextGoalTime, getActivity()); //initialize TimeFormat


        textViewAddCity = v.findViewById(R.id.textViewAddCity); textViewAddCity.setOnClickListener(onClickListener);
        textViewSendEmail = v.findViewById(R.id.textViewSendEmail); textViewSendEmail.setOnClickListener(onClickListener);

        progressBar = v.findViewById(R.id.progressBar);

        buttonSettings = v.findViewById(R.id.buttonSettings); buttonSettings.setOnClickListener(onClickListener);
        buttonBack = v.findViewById(R.id.buttonBack); buttonBack.setOnClickListener(onClickListener);
        buttonApplyChanges = v.findViewById(R.id.buttonApplyChanges); buttonApplyChanges.setOnClickListener(onClickListener);
        buttonLogOut = v.findViewById(R.id.buttonLogOut); buttonLogOut.setOnClickListener(onClickListener);

        editTextDisplayName = v.findViewById(R.id.editTextDisplayName);
        editTextGoalTime= v.findViewById(R.id.editTextGoalTime); editTextGoalTime.addTextChangedListener(new TimeFormat(editTextGoalTime, getActivity()).getTimeFormatter());

        //set edit text to saved values
        if (mainActivity.getDisplayName() != null){
            editTextDisplayName.setText(mainActivity.getDisplayName());
        }
        double sec = (double)mainActivity.getGoalTimeMillis() / 1000;
        int min = (int)sec / 60;
        int sec_int = (int)sec % 60;
        editTextGoalTime.setText(String.format(Locale.getDefault(), "%dm %02ds", min, sec_int));

        if (mainActivity.getCity() != null && mainActivity.getCity().length() > 0){
            textViewAddCity.setText(mainActivity.getCity());
        }

        //prevent leaving without saving
        editTextDisplayName.addTextChangedListener(textWatcher);
        editTextGoalTime.addTextChangedListener(textWatcher);

        new Handler().postDelayed(new Runnable() { //has to be delayed. Won't work otherwise for some reason
            @Override
            public void run() {
                mainActivity.setSettingsChanged(false);

            }
        }, 150);

        return v;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.buttonBack:
                    mainActivity.onBackPressed();
                    break;
                case R.id.buttonSettings:
                    mainActivity.setFragmentReturnableSlide(new SettingsFragment());
                    break;
                case R.id.buttonApplyChanges:
                    applyChanges();
                    mainActivity.setSettingsChanged(false);
                    break;
                case R.id.buttonLogOut:
                    mainActivity.showAlertDialog("Log out?", "Your shower data and settings will be saved if you log out. Proceed?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mainActivity.logOut();
                        }
                    });
                    break;
                case R.id.textViewAddCity:
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if (mainActivity.findCity().length() > 0) {
                            textViewAddCity.setText(mainActivity.findCity());
                            mainActivity.setSettingsChanged(true);
                        }

                    }
                    else {
                        requestLocationPermission();
                        if (mainActivity.findCity().length() > 0) {
                            textViewAddCity.setText(mainActivity.findCity());
                            mainActivity.setSettingsChanged(true);

                        }
                    }
                    break;
                case R.id.textViewSendEmail:
                    mainActivity.showAlertDialog("Send email?", "You will be automatically logged out. Proceed?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mainActivity.sendResetPasswordEmail();
                            mainActivity.logOut();
                        }
                    });
                    break;
            }
        }
    };

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mainActivity.setSettingsChanged(true);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };



    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

            new AlertDialog.Builder(getActivity())
                    .setTitle("Permission Needed")
                    .setMessage("Granting location permission gives you access to the leaderboards, which are ranked by city.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COURSE_LOCATION_PERMISSION_CODE);

                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
            .create().show();
        }
        else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COURSE_LOCATION_PERMISSION_CODE);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == COURSE_LOCATION_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getActivity(), "Permission was granted.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "Permission was not granted.", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void applyChanges(){
        if (TimeFormat.textToValue(editTextGoalTime) > 0) {
            long instanceGoalTimeMillis = TimeFormat.textToValue(editTextGoalTime) * 1000;

            if (mainActivity.saveToFireStore(MainActivity.KEY_GOAL_TIME, instanceGoalTimeMillis)) {
                Toast.makeText(getActivity(), "Changes saved!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "Changes not saved. Network connection required.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getActivity(), "Goal time must be greater than 0 seconds.", Toast.LENGTH_SHORT).show();
        }

        if (!textViewAddCity.getText().toString().equals("Add City") && textViewAddCity.getText().toString().length() > 0) {
            mainActivity.saveToFireStore(MainActivity.KEY_CITY, textViewAddCity.getText().toString());
        }


        if (!(mainActivity.getDisplayName() == editTextDisplayName.getText().toString()) && editTextDisplayName.getText().toString().length() > 0) { //only apply if a different name is entered
            mainActivity.setUserDisplayName(editTextDisplayName.getText().toString());
            mainActivity.loadUserDisplayName();
        }


        mainActivity.loadUserPreferencesFirebase();

    }



}
