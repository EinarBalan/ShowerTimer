package com.balanstudios.showerly;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private MainActivity mainActivity;

    private ImageButton buttonBack;
    private ImageButton buttonMore;
    private Button buttonApply;
    private Button buttonLogIn;
    private Button buttonDeleteAccount;

    private EditText editTextFlowRate;
    private EditText editTextCost;
    private EditText editTextAlertFrequency;
    private EditText editTextGoalTime;

    private SeekBar seekBarVolume;

    private Switch switchVibrations;
    private Switch switchIntervalAlerts;
    private Switch switchDarkMode;

    private LinearLayout linearLayoutAlertFrequency;
    private LinearLayout linearLayoutGoal;
    private View dividerAlert;

    private TextView textViewPrivacyPolicy;

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
        buttonMore = v.findViewById(R.id.buttonMore); buttonMore.setOnClickListener(onClickListener);
        buttonApply = v.findViewById(R.id.buttonApplyChanges); buttonApply.setOnClickListener(onClickListener);
        buttonLogIn = v.findViewById(R.id.buttonLogIn); buttonLogIn.setOnClickListener(onClickListener);
        buttonDeleteAccount = v.findViewById(R.id.buttonDeleteAccount); buttonDeleteAccount.setOnClickListener(onClickListener);

        editTextFlowRate = v.findViewById(R.id.editTextFlowRate);  editTextFlowRate.setHint(Shower.getGallonsPerMinute() + " gallons per minute");
        editTextCost = v.findViewById(R.id.editTextCost); editTextCost.setHint(Shower.getDollarsPerGallon() * 100 + " cents per gallon");
        editTextAlertFrequency = v.findViewById(R.id.editTextAlertFrequency); editTextAlertFrequency.addTextChangedListener(new TimeFormat(editTextAlertFrequency, getActivity()).getTimeFormatter());
        editTextGoalTime = v.findViewById(R.id.editTextGoalTime); editTextGoalTime.addTextChangedListener(new TimeFormat(editTextGoalTime, getActivity()).getTimeFormatter());

        editTextAlertFrequency.setText(TimeFormat.valueToString(mainActivity.getAlertFrequencySeconds()));
        editTextGoalTime.setText(TimeFormat.valueToString(mainActivity.getGoalTimeMillis() / 1000));

        seekBarVolume = v.findViewById(R.id.seekBarVolume); seekBarVolume.setOnSeekBarChangeListener(onSeekBarChangeListener);

        switchVibrations = v.findViewById(R.id.switchVibrations); switchVibrations.setChecked(mainActivity.isVibrateEnabled()); switchVibrations.setOnCheckedChangeListener(onCheckedChangeListener);
        switchIntervalAlerts = v.findViewById(R.id.switchIntervalAlerts); switchIntervalAlerts.setChecked(mainActivity.isIntervalAlertsOn()); switchIntervalAlerts.setOnCheckedChangeListener(onCheckedChangeListener);
        switchDarkMode = v.findViewById(R.id.switchDarkMode); switchDarkMode.setChecked(mainActivity.isDarkMode());switchDarkMode.setOnCheckedChangeListener(onCheckedChangeListener);

        textViewPrivacyPolicy = v.findViewById(R.id.textViewPrivacyPolicy); textViewPrivacyPolicy.setOnClickListener(onClickListener);

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

        linearLayoutGoal = v.findViewById(R.id.linearlayoutGoal);
        if (mainActivity.isUserAnon()){
            linearLayoutGoal.setVisibility(View.VISIBLE);
            buttonLogIn.setVisibility(View.VISIBLE);
            buttonDeleteAccount.setVisibility(View.GONE);
        }

        //prevent leaving without applying changes by accident
        editTextFlowRate.addTextChangedListener(textWatcher);
        editTextGoalTime.addTextChangedListener(textWatcher);
        editTextAlertFrequency.addTextChangedListener(textWatcher);
        editTextCost.addTextChangedListener(textWatcher);

        mainActivity.setSettingsChanged(false);

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
                    mainActivity.setSettingsChanged(false);
                    break;
                case R.id.buttonLogIn:
                    mainActivity.logOutAnon();
                    break;
                case R.id.buttonDeleteAccount:
                    mainActivity.deleteUser();
                    break;
                case R.id.buttonMore:
                     PopupMenu menu = new PopupMenu(mainActivity, view);
                     menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                         @Override
                         public boolean onMenuItemClick(MenuItem menuItem) {
                             switch (menuItem.getItemId()) {
                                 case R.id.menuPrivacyPolicy:
                                     try {
                                         Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://app.termly.io/document/privacy-policy/6641d54f-707a-45a7-90b6-99ea13a40e98#DNT"));
                                         startActivity(browserIntent);
                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }
                                     break;
                                 case R.id.menuAbout:
                                     try {
                                         Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://youthforsustainability.org/"));
                                         startActivity(browserIntent);
                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }

                             }
                             return false;
                         }
                     });
                     MenuInflater inflater = menu.getMenuInflater();
                     inflater.inflate(R.menu.menu_privacy_policy, menu.getMenu());
                     menu.show();
                     break;
                case R.id.textViewPrivacyPolicy:
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://app.termly.io/document/privacy-policy/cf48d4b6-09ce-4631-83fe-b26d1e456a3d"));
                        startActivity(browserIntent);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
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
           mainActivity.setSettingsChanged(true);
           switch (compoundButton.getId()) {
               case R.id.switchDarkMode:
                   mainActivity.setDarkMode(switchDarkMode.isChecked());
                   applyChanges();
                   if (!mainActivity.isUserAnon()) {
                       mainActivity.saveSettingsToFirestore();
                   }
                   mainActivity.saveSettings();

                   Intent intent = new Intent(getActivity(), MainActivity.class);
                   startActivity(intent);
                   getActivity().finish();
                   break;
               case R.id.switchIntervalAlerts:
                   mainActivity.setSettingsChanged(true);
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

       if (TimeFormat.textToValue(editTextGoalTime) > 0){
           mainActivity.setGoalTimeMillis(TimeFormat.textToValue(editTextGoalTime) * 1000);
       }

       mainActivity.setDarkMode(switchDarkMode.isChecked());
       mainActivity.setIntervalAlertsOn(switchIntervalAlerts.isChecked());
       mainActivity.setVibrateEnabled(switchVibrations.isChecked());

       mainActivity.saveSettings();
       if (!mainActivity.isUserAnon()) {
           mainActivity.saveSettingsToFirestore();
       }
       Toast.makeText(getActivity(), "Changes saved!", Toast.LENGTH_SHORT).show();



   }

}
