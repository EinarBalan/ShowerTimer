package com.balanstudios.showerly;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.sql.Time;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private MainActivity mainActivity;

    private ImageButton buttonBack;
    private Button buttonApply;

    private EditText editTextFlowRate;
    private EditText editTextCost;
    private EditText editTextAlertFrequency;

    private SeekBar seekBarVolume;

    private Switch switchVibrations;
    private Switch switchIntervalAlerts;
    private Switch switchDarkMode;

    private LinearLayout linearLayoutAlertFrequency;
    private View dividerAlert;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        mainActivity = (MainActivity) getActivity();

        mainActivity.loadSettings();

        buttonBack = v.findViewById(R.id.buttonBack);  buttonBack.setOnClickListener(onClickListener);
        buttonApply = v.findViewById(R.id.buttonApplyChanges); buttonApply.setOnClickListener(onClickListener);

        editTextFlowRate = v.findViewById(R.id.editTextFlowRate);  editTextFlowRate.setHint(Shower.getGallonsPerMinute() + " gallons per minute");
        editTextCost = v.findViewById(R.id.editTextCost); editTextCost.setHint(Shower.getDollarsPerGallon() * 100 + " cents per gallon");
        editTextAlertFrequency = v.findViewById(R.id.editTextAlertFrequency); editTextAlertFrequency.addTextChangedListener(new TimeFormat(editTextAlertFrequency, getActivity()).getTimeFormatter());
        long sec = mainActivity.getAlertFrequencySeconds();
        int min = (int)sec / 60;
        sec %= 60;
        editTextAlertFrequency.setText(String.format(Locale.getDefault(), "%dm %02ds", min, sec));


        seekBarVolume = v.findViewById(R.id.seekBarVolume); seekBarVolume.setOnSeekBarChangeListener(onSeekBarChangeListener);

        switchVibrations = v.findViewById(R.id.switchVibrations); switchVibrations.setChecked(mainActivity.isVibrateEnabled()); switchVibrations.setOnCheckedChangeListener(onCheckedChangeListener);
        switchIntervalAlerts = v.findViewById(R.id.switchIntervalAlerts); switchIntervalAlerts.setChecked(mainActivity.isIntervalAlertsOn()); switchIntervalAlerts.setOnCheckedChangeListener(onCheckedChangeListener);
        switchDarkMode = v.findViewById(R.id.switchDarkMode); switchDarkMode.setChecked(mainActivity.isDarkMode());switchDarkMode.setOnCheckedChangeListener(onCheckedChangeListener);

        linearLayoutAlertFrequency = v.findViewById(R.id.linearLayoutAlertFrequency);
        dividerAlert = v.findViewById(R.id.dividerAlert);
        if (switchIntervalAlerts.isChecked()){
            linearLayoutAlertFrequency.setVisibility(View.VISIBLE);
            dividerAlert.setVisibility(View.VISIBLE);
        }
        else {
            linearLayoutAlertFrequency.setVisibility(View.GONE);
            dividerAlert.setVisibility(View.GONE);
        }

        return v;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.buttonBack:
                    mainActivity.onBackPressed();
                    break;
                case R.id.buttonApplyChanges:
                    applyChanges();
                    break;
            }
        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

   CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
       @Override
       public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
           switch (compoundButton.getId()) {
               case R.id.switchDarkMode:
                   mainActivity.setDarkMode(switchDarkMode.isChecked());
                   applyChanges();
                   mainActivity.saveSettingsToFirestore();
                   mainActivity.saveSettings();

                   Intent intent = new Intent(getActivity(), MainActivity.class);
                   startActivity(intent);
                   getActivity().finish();
                   break;
               case R.id.switchIntervalAlerts:
                   if (switchIntervalAlerts.isChecked()){
                       linearLayoutAlertFrequency.setVisibility(View.VISIBLE);
                       dividerAlert.setVisibility(View.VISIBLE);
                   }
                   else {
                       linearLayoutAlertFrequency.setVisibility(View.GONE);
                       dividerAlert.setVisibility(View.GONE);
                   }
                   break;
           }
       }
   };

   private void applyChanges(){
       if (editTextFlowRate.getText().toString().length() > 0){
           Shower.setGallonsPerMinute(Double.parseDouble(editTextFlowRate.getText().toString()));
       }

       if (editTextCost.getText().toString().length() > 0){
           Shower.setDollarsPerGallon(Double.parseDouble(editTextCost.getText().toString()) / 100); //converts from cents to dollars
       }

       if (TimeFormat.textToValue(editTextAlertFrequency) > 0){
           mainActivity.setAlertFrequencySeconds(TimeFormat.textToValue(editTextAlertFrequency));
       }

       mainActivity.setDarkMode(switchDarkMode.isChecked());
       mainActivity.setIntervalAlertsOn(switchIntervalAlerts.isChecked());
       mainActivity.setVibrateEnabled(switchVibrations.isChecked());

       mainActivity.saveSettings();
       mainActivity.saveSettingsToFirestore();
       Toast.makeText(getActivity(), "Changes saved!", Toast.LENGTH_SHORT).show();



   }

}
