package com.balanstudios.showerly;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


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
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ProfanityFilter.loadConfigs();
            }
        });
        networkThread.start();

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
                    .setMessage("Granting location permission gives you access to the leaderboards, which are ranked by city. Your location will only be accessed once and only the name of your city is stored. ")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COURSE_LOCATION_PERMISSION_CODE);
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
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COURSE_LOCATION_PERMISSION_CODE);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == COURSE_LOCATION_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getActivity(), "Permission was granted.", Toast.LENGTH_SHORT).show();
                if (mainActivity.findCity().length() > 0) {
                    textViewAddCity.setText(mainActivity.findCity());
                    mainActivity.setSettingsChanged(true);
                }
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
            if (!ProfanityFilter.hasProfanity(editTextDisplayName.getText().toString())) { //filter profanity
                mainActivity.setUserDisplayName(editTextDisplayName.getText().toString());
                mainActivity.loadUserDisplayName();
            }
            else {
                Toast.makeText(mainActivity, "Cannot use profanity in display name", Toast.LENGTH_SHORT).show();
            }
        }


        mainActivity.loadUserPreferencesFirebase();

    }

}

class ProfanityFilter {
    static Map<String, String[]> words = new HashMap<>();
    static int largestWordLength = 0;
    public static void loadConfigs() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://docs.google.com/spreadsheets/d/e/2PACX-1vTKOACo_baFNu9rn6RzhfU0A3IlZ9BCtBLaAN85g4s_9snBurIC-A8E62DWvzN1G2olQsOSfrUjkrW0/pub?output=csv").openConnection().getInputStream()));
            String line = "";
            int counter = 0;
            while((line = reader.readLine()) != null) {
                counter++;
                String[] content = null;
                try {
                    content = line.split(",");
                    if(content.length == 0) {
                        continue;
                    }
                    String word = content[0];
                    String[] ignore_in_combination_with_words = new String[]{};
                    if(content.length > 1) {
                        ignore_in_combination_with_words = content[1].split("_");
                    }

                    if(word.length() > largestWordLength) {
                        largestWordLength = word.length();
                    }
                    words.put(word.replaceAll(" ", ""), ignore_in_combination_with_words);

                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
//            Log.d("D","Loaded " + counter + " words to filter out");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Iterates over a String input and checks whether a cuss word was found in a list, then checks if the word should be ignored (e.g. bass contains the word *ss).
     * @param input
     * @return
     */
    public static ArrayList<String> badWordsFound(String input) {
        if(input == null) {
            return new ArrayList<>();
        }

        // remove leetspeak
        input = input.replaceAll("1","i");
        input = input.replaceAll("!","i");
        input = input.replaceAll("3","e");
        input = input.replaceAll("4","a");
        input = input.replaceAll("@","a");
        input = input.replaceAll("5","s");
        input = input.replaceAll("7","t");
        input = input.replaceAll("0","o");
        input = input.replaceAll("9","g");
        input = input.replaceAll("\\$", "s");

        ArrayList<String> badWords = new ArrayList<>();
        input = input.toLowerCase().replaceAll("[^a-zA-Z]", "");

        // iterate over each letter in the word
        for(int start = 0; start < input.length(); start++) {
            // from each letter, keep going to find bad words until either the end of the sentence is reached, or the max word length is reached.
            for(int offset = 1; offset < (input.length()+1 - start) && offset < largestWordLength; offset++)  {
                String wordToCheck = input.substring(start, start + offset);
                if(words.containsKey(wordToCheck)) {
                    // for example, if you want to say the word bass, that should be possible.
                    String[] ignoreCheck = words.get(wordToCheck);
                    boolean ignore = false;
                    for(int s = 0; s < ignoreCheck.length; s++ ) {
                        if(input.contains(ignoreCheck[s])) {
                            ignore = true;
                            break;
                        }
                    }
                    if(!ignore) {
                        badWords.add(wordToCheck);
                    }
                }
            }
        }

        return badWords;

    }


    public static boolean hasProfanity(String input) {
        ArrayList<String> badWords = badWordsFound(input);
        return badWords.size() > 0;
    }
}