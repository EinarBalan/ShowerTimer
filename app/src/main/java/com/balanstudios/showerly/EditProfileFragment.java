package com.balanstudios.showerly;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    public static final String KEY_GOAL_TIME = "GOAL_TIME_MILLIS";

    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mainActivity = (MainActivity) getActivity();

        textViewAddCity = v.findViewById(R.id.textViewAddCity); textViewAddCity.setOnClickListener(onClickListener);
        textViewSendEmail = v.findViewById(R.id.textViewSendEmail); textViewSendEmail.setOnClickListener(onClickListener);

        progressBar = v.findViewById(R.id.progressBar);

        buttonSettings = v.findViewById(R.id.buttonSettings); buttonSettings.setOnClickListener(onClickListener);
        buttonBack = v.findViewById(R.id.buttonBack); buttonBack.setOnClickListener(onClickListener);
        buttonApplyChanges = v.findViewById(R.id.buttonApplyChanges); buttonApplyChanges.setOnClickListener(onClickListener);
        buttonLogOut = v.findViewById(R.id.buttonLogOut); buttonLogOut.setOnClickListener(onClickListener);

        editTextDisplayName = v.findViewById(R.id.editTextDisplayName);
        editTextGoalTime= v.findViewById(R.id.editTextGoalTime); editTextGoalTime.addTextChangedListener(new TimeFormat(editTextGoalTime).getTimeFormatter());

        //set edit text to saved values
        if (mainActivity.getDisplayName() != null){
            editTextDisplayName.setText(mainActivity.getDisplayName());
        }
        double sec = (double)mainActivity.getGoalTimeMillis() / 1000;
        int min = (int)sec / 60;
        int sec_int = (int)sec % 60;
        editTextGoalTime.setText(String.format(Locale.getDefault(), "%dm %02ds", min, sec_int));

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
                    //open settings fragment
                    break;
                case R.id.buttonApplyChanges:
                    applyChanges();
                    break;
                case R.id.buttonLogOut:
                    //insert are you sure dialog here
                    mainActivity.logOut();
                    break;
                case R.id.textViewAddCity:
                    //find geolocation and convert to city
                    break;
                case R.id.textViewSendEmail:
                    //are you sure?
                    //send reset password email
                    mainActivity.sendResetPasswordEmail();
                    //log out
                    break;
            }
        }
    };

    public void applyChanges(){
        if (textToValue(editTextGoalTime) > 0) {
            long instanceGoalTimeMillis = textToValue(editTextGoalTime) * 1000;

            if (mainActivity.saveToFireStore(KEY_GOAL_TIME, instanceGoalTimeMillis)) {
                Toast.makeText(getActivity(), "Changes saved!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "Changes not saved. Network connection required.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getActivity(), "Goal time must be greater than 0 seconds.", Toast.LENGTH_SHORT).show();
        }

        if (mainActivity.getDisplayName() != editTextDisplayName.getText().toString()) { //only apply if a different name is entered
            mainActivity.setUserDisplayName(editTextDisplayName.getText().toString());
            mainActivity.loadUserDisplayName();
        }

        mainActivity.loadGoalTime();

    }


    //formats time inputs while user is typing using Text Watcher and textToValue()
    private class TimeFormat {
        EditText et;
        String raw;
        private TextWatcher timeFormatter = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() > 0) {
                    raw += charSequence.toString();
                    if (raw.length() > charSequence.length()) {
                        raw = charSequence.toString().replaceAll("m", "");
                        raw = raw.replaceAll(" ", "");
                        raw = raw.replaceAll("s", "");
                        et.removeTextChangedListener(this);
                        et.setText(textToValue(raw));
                        et.addTextChangedListener(this);
                        et.setSelection(textToValue(raw).length() - 1);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        public TimeFormat(EditText et) {
            this.et = et;
        }

        public TextWatcher getTimeFormatter() {
            return timeFormatter;
        }
    }

    //user can input 010 to get 0m 10s or 2030 to get 20m 30s, also accounts for m and s being in the text. Returns seconds
    public int textToValue(EditText et) {
        String raw = et.getText().toString().trim();
        int min, sec, index = 2;

        try { //prone to crashing if user deletes only part of the text but not all and then enters
            if (raw.contains("m") || raw.contains("s")) {
                index = 5;


                min = Integer.parseInt(
                        raw.substring(0, raw.length() - index));
                sec = Integer.parseInt(
                        raw.substring(raw.length() - index + 2, raw.length() - 1));
                if (sec >= 60) {
                    min += sec / 60;
                    sec = sec % 60;
                }


            } else {
                if (raw.length() < 3) {
                    min = 0;
                    sec = Integer.parseInt(raw);
                } else {
                    min = Integer.parseInt(
                            raw.substring(0, raw.length() - index));
                    sec = Integer.parseInt(
                            raw.substring(raw.length() - index));
                }


                if (sec >= 60) {
                    min += sec / 60;
                    sec = sec % 60;
                }
            }

            et.setText(String.format(Locale.getDefault(), "%dm %02ds", min, sec));
            return (min * 60 + sec);

        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Input proper format", Toast.LENGTH_SHORT).show();
        } catch (StringIndexOutOfBoundsException e) {
            Toast.makeText(getActivity(), "Input proper format", Toast.LENGTH_SHORT).show();
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(getActivity(), "Input proper format", Toast.LENGTH_SHORT).show();
        }
        return 0;
    }

    public String textToValue(String raw) {
        int min, sec, index = 2;

        try { //prone to crashing if user deletes only part of the text but not all and then enters
            if (raw.contains("m") || raw.contains("s")) {
                index = 5;


                min = Integer.parseInt(
                        raw.substring(0, raw.length() - index));
                sec = Integer.parseInt(
                        raw.substring(raw.length() - index + 2, raw.length() - 1));
                if (sec >= 60) {
                    min += sec / 60;
                    sec = sec % 60;
                }


            } else {
                if (raw.length() < 3) {
                    min = 0;
                    sec = Integer.parseInt(raw);
                } else {
                    min = Integer.parseInt(
                            raw.substring(0, raw.length() - index));
                    sec = Integer.parseInt(
                            raw.substring(raw.length() - index));
                }


//                if (sec >= 60) {
//                    min += sec / 60;
//                    sec = sec % 60;
//                }
            }

            return String.format(Locale.getDefault(), "%dm %02ds", min, sec);

        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Input proper format", Toast.LENGTH_SHORT).show();
        } catch (StringIndexOutOfBoundsException e) {
            Toast.makeText(getActivity(), "Input proper format", Toast.LENGTH_SHORT).show();
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(getActivity(), "Input proper format", Toast.LENGTH_SHORT).show();
        }
        return "";
    }

}
