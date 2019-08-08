package com.balanstudios.showerly;


import android.content.DialogInterface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private MainActivity mainActivity;

    private Chronometer chronometer;
    private boolean isTimerRunning = false;
    private FloatingActionButton buttonToggleTimer;
    private Button buttonSave;
    private Button buttonReset;
    private ImageButton imageButtonRefreshStats;
    private ProgressBar progressBarGoal;
    private ProgressBar progressBarOverflow;

    private TextView textViewDate;
    private TextView textViewQuickVolume;
    private TextView textViewQuickCost;
    private TextView textViewGoal;
    private TextView textViewTimeSpent;
    private TextView textViewAvgShowerLength;
    private TextView textViewTotalVolume;
    private TextView textViewTotalCost;
    private TextView textViewDisplayName;

    private Drawable toggleTimer;
    private DecimalFormat formatVolume = new DecimalFormat("0.0");
    private DecimalFormat formatCost = new DecimalFormat("0.00");
    private DecimalFormat formatTime = new DecimalFormat("0");


    private long elapsedTimeMillis = 0;
    private long timeUntilAlertSeconds = 0;
    private double elapsedTimeMinutes = 0;
    private double instanceVolume = 0;
    private double instanceCost = 0;
    private SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
    private Calendar calendar = Calendar.getInstance();
    private String instanceDate = DateFormat.getInstance().format(calendar.getTime()).substring(0, DateFormat.getInstance().format(calendar.getTime()).indexOf(" "));
    private String instanceTime = "";

    //stats access keys
    public static final String KEY_GOAL = "GOAL_TIME_MILLIS";
    public static final String KEY_TOTAL_TIME = "TOTAL_TIME";
    public static final String KEY_TOTAL_COST = "TOTAL_COST";
    public static final String KEY_TOTAL_VOLUME = "TOTAL_VOLUME";
    public static final String KEY_AVG_SHOWER_LEN = "AVG_SHOWER_LEN";


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mainActivity = ((MainActivity) getActivity());

        chronometer = v.findViewById(R.id.textTimer); chronometer.setOnChronometerTickListener(onChronometerTickListener);
        buttonToggleTimer = v.findViewById(R.id.floatingActionButtonToggleTimer); buttonToggleTimer.setOnClickListener(onClickListener);
        buttonSave = v.findViewById(R.id.buttonSave); buttonSave.setOnClickListener(onClickListener);
        buttonReset = v.findViewById(R.id.buttonReset); buttonReset.setOnClickListener(onClickListener);
        imageButtonRefreshStats = v.findViewById(R.id.imageButtonRefreshStats); imageButtonRefreshStats.setOnClickListener(onClickListener);
        progressBarGoal = v.findViewById(R.id.progressBarGoal);
        progressBarOverflow = v.findViewById(R.id.progressBarOverflow);

        textViewDate = v.findViewById(R.id.textViewDate); textViewDate.setText(instanceDate);
        textViewQuickVolume = v.findViewById(R.id.textViewQuickVolume);
        textViewQuickCost = v.findViewById(R.id.textViewQuickCost);
        textViewGoal = v.findViewById(R.id.textViewVibrations);
        textViewTimeSpent = v.findViewById(R.id.textViewTimeSpent);
        textViewAvgShowerLength = v.findViewById(R.id.textViewAvgShowerLength);
        textViewTotalVolume = v.findViewById(R.id.textViewTotalVolume);
        textViewTotalCost = v.findViewById(R.id.textViewTotalCost);
        textViewDisplayName = v.findViewById(R.id.textViewDisplayName);

        mainActivity.loadCache();
        updateCachedText();

        if (!mainActivity.isUserAnon()) {
            new Handler().postDelayed(new Runnable() { //delayed because otherwise firestore variables won't update in time
                @Override
                public void run() {
                    updateLifetimeStatsTextViews();
                }
            }, 3000);
        }
        else {
            buttonSave.setText("Settings");
            textViewTimeSpent.setText("---");
            textViewAvgShowerLength.setText("---");
            textViewTotalVolume.setText("---");
            textViewTotalCost.setText("---");

        }

        return v;
    }



    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.floatingActionButtonToggleTimer:
                    toggleTimer(view);
                    break;
                case R.id.buttonReset:
                    if (elapsedTimeMillis > 0) {
                        mainActivity.showAlertDialog("Reset timer?", "The data for this timer will be lost. Are you sure you want to reset?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                resetTimer(buttonReset);
                            }
                        });
                    }
                    break;
                case R.id.buttonSave:
                    saveShower();
                    break;
                case R.id.imageButtonRefreshStats:
                    mainActivity.loadUserLifetimeStatsFromFireStore();
                    updateLifetimeStatsTextViews();
                    Toast.makeText(getActivity(), "Refreshed lifetime stats.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    Chronometer.OnChronometerTickListener onChronometerTickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            //update elapsed time
            elapsedTimeMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            elapsedTimeMinutes = (double)elapsedTimeMillis / 1000 / 60;

            if (mainActivity.isIntervalAlertsOn()) { //interval alert system
                if (elapsedTimeMillis - mainActivity.getGoalTimeMillis() >= 0 && elapsedTimeMillis - mainActivity.getGoalTimeMillis() <= 300){
                    mainActivity.playAlertSound(MainActivity.timer_end_ID);
                    mainActivity.vibrate(3);
                }
                else if ((elapsedTimeMillis / 1000) % mainActivity.getAlertFrequencySeconds() == 0 && elapsedTimeMillis > 1000) {
                    mainActivity.playAlertSound(MainActivity.interval_end_ID);
                    mainActivity.vibrate(2);
//                    Toast.makeText(getActivity(), "Alert", Toast.LENGTH_SHORT).show();
                }


            }

            //update progress bars
            int progress = (int)((double) elapsedTimeMillis /(double)mainActivity.getGoalTimeMillis() * 100);
            if (progress <= 100){ // if goal is currently being met
                progressBarGoal.setProgress(progress);
            }
            else { //if goal is not being met
                progressBarOverflow.setProgress(progress - 100);
            }

            //update stats
            instanceVolume = Shower.calculateVolume(elapsedTimeMinutes);
            instanceCost = Shower.calculateCost(instanceVolume);

            textViewQuickVolume.setText(formatVolume.format(instanceVolume) + " gallons");
            textViewQuickCost.setText("$" + formatCost.format(instanceCost));


            if (elapsedTimeMillis > 3600000){ //shower can't be longer than one hour
                mainActivity.playAlertSound(MainActivity.timer_end_ID);
                mainActivity.vibrate(3);
                toggleTimer(buttonToggleTimer);
                saveShower();
                Toast.makeText(getActivity(), "Shower too long! Get out!", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void toggleTimer(View v){
        if (isTimerRunning){ //pause timer
            chronometer.stop();
            elapsedTimeMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //button show animation
            buttonReset.setVisibility(View.VISIBLE);
            buttonReset.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_fast));
            buttonSave.setVisibility(View.VISIBLE);
            buttonSave.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_fast));

            //pause to play toggle animation, makes sure android version is compatible
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                toggleTimer = getResources().getDrawable(R.drawable.pause_to_play, null);
                buttonToggleTimer.setImageDrawable(toggleTimer);
                ((AnimatedVectorDrawable) toggleTimer).start();
            } else {
                toggleTimer = getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp, null);
                buttonToggleTimer.setImageDrawable(toggleTimer);
            }
        }
        else { //start timer
            chronometer.setBase(SystemClock.elapsedRealtime() - elapsedTimeMillis);
            chronometer.start();
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //button hide animation

            if (mainActivity.isUserAnon()) {
                buttonSave.setAlpha(.5f);
            }

            buttonReset.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_fast));
            buttonReset.setVisibility(View.GONE);
            buttonSave.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_fast));
            buttonSave.setVisibility(View.GONE);

            //play to pause toggle animation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                toggleTimer = getResources().getDrawable(R.drawable.play_to_pause, null);
                buttonToggleTimer.setImageDrawable(toggleTimer);
                ((AnimatedVectorDrawable) toggleTimer).start();
            } else {
                toggleTimer = getResources().getDrawable(R.drawable.ic_pause_black_24dp, null);
                buttonToggleTimer.setImageDrawable(toggleTimer);
            }

            if (elapsedTimeMillis <= 100){ //only hide navbar if timer just started, 100 because elapsedTime will never be zero
                if (mainActivity.getMainNavBar().getVisibility() == View.VISIBLE) {
                    mainActivity.getMainNavBar().startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_down));
                }
                mainActivity.getMainNavBar().setVisibility(View.GONE);
                Calendar calendar = Calendar.getInstance();
                instanceDate = DateFormat.getInstance().format(calendar.getTime()).substring(0, DateFormat.getInstance().format(calendar.getTime()).indexOf(" "));
                instanceTime = sdf.format(calendar.getTime());
            }
        }
        isTimerRunning = !isTimerRunning;
    }

    public void resetTimer(View v){
        // insert 'are you sure?' dialog here
        elapsedTimeMillis = 0;
        timeUntilAlertSeconds = 0;
        isTimerRunning = false;
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.stop();

        //reset stats here
        progressBarOverflow.setProgress(0);
        if (mainActivity.isUserAnon()){
            buttonSave.setAlpha(1);
        }
        else {
            mainActivity.getMainNavBar().setVisibility(View.VISIBLE);
        }
    }

    public void saveShower(){
        if (!mainActivity.isUserAnon()) {
            if (elapsedTimeMillis > 15000) {

                //add shower to user's account (getUserShower used so that showers are stored locally rather than only on database ---> fewer required accesses from client)
                mainActivity.getUserShowers().add(new Shower(elapsedTimeMillis, instanceDate, instanceTime, elapsedTimeMillis <= mainActivity.getGoalTimeMillis()));

                //save shower
                if (mainActivity.saveToFireStore(mainActivity.getUserShowers())) { //checks network connection

                    //update lifetime stats (before reset timer you moron)
                    mainActivity.addTotalTimeMinutes(elapsedTimeMinutes);
                    mainActivity.addTotalCost(instanceCost);
                    mainActivity.addTotalVolume(instanceVolume);


                    resetTimer(buttonSave);
                    Toast.makeText(getActivity(), "Saved to my showers!", Toast.LENGTH_SHORT).show();

                    mainActivity.saveToFireStore(KEY_TOTAL_TIME, mainActivity.getTotalTimeMinutes(), KEY_TOTAL_COST, mainActivity.getTotalCost(), KEY_TOTAL_VOLUME, mainActivity.getTotalVolume(), KEY_AVG_SHOWER_LEN, mainActivity.calculateAvgShowerLengthMinutes());
                    mainActivity.loadCache();
                    updateCachedText();

                    ShowerlyUser currentUser = new ShowerlyUser(mainActivity.getEmail(), mainActivity.getDisplayName(), mainActivity.getAvgShowerLengthMinutes());
                    for (int i = 0; i < mainActivity.getTop25Users().size(); i++){ //update leaderboards with new avg time
                        if (currentUser.equals(mainActivity.getTop25Users().get(i))){
                            mainActivity.getTop25Users().set(i, currentUser);

                            if (i > 0 ) {
                                if (currentUser.getAvgShowerLength() < mainActivity.getTop25Users().get(i - 1).getAvgShowerLength()) { //reorder leaderboards if there are changes
                                    ShowerlyUser tempUser = mainActivity.getTop25Users().get(i - 1);
                                    mainActivity.getTop25Users().set(i - 1, currentUser);
                                    mainActivity.getTop25Users().set(i, tempUser);
                                }
                            }
                            if (i < mainActivity.getTop25Users().size() - 1){
                                if (currentUser.getAvgShowerLength() > mainActivity.getTop25Users().get(i + 1).getAvgShowerLength()){
                                    ShowerlyUser tempUser = mainActivity.getTop25Users().get(i + 1);
                                    mainActivity.getTop25Users().set(i + 1, currentUser);
                                    mainActivity.getTop25Users().set(i, tempUser);
                                }
                            }
                            break;
                        }
                    }
                    if (mainActivity.getCity().length() > 0){
                        for (int i = 0; i < mainActivity.getLocalTop25Users().size(); i++){ //update leaderboards with new avg time
                            if (currentUser.equals(mainActivity.getLocalTop25Users().get(i))){
                                mainActivity.getLocalTop25Users().set(i, currentUser);

                                if (i > 0) {
                                    if (currentUser.getAvgShowerLength() < mainActivity.getLocalTop25Users().get(i - 1).getAvgShowerLength()) { //reorder leaderboards if there are changes
                                        ShowerlyUser tempUser = mainActivity.getLocalTop25Users().get(i - 1);
                                        mainActivity.getLocalTop25Users().set(i - 1, currentUser);
                                        mainActivity.getLocalTop25Users().set(i, tempUser);
                                    }
                                }
                                if (i < mainActivity.getLocalTop25Users().size() - 1){
                                    if (currentUser.getAvgShowerLength() > mainActivity.getLocalTop25Users().get(i + 1).getAvgShowerLength()){
                                        ShowerlyUser tempUser = mainActivity.getLocalTop25Users().get(i + 1);
                                        mainActivity.getLocalTop25Users().set(i + 1, currentUser);
                                        mainActivity.getLocalTop25Users().set(i, tempUser);
                                    }
                                }
                                break;
                            }
                        }
                    }
                    mainActivity.saveLeaderboards();

                    new Handler().postDelayed(new Runnable() { //delayed because otherwise firestore variables won't update in time
                        @Override
                        public void run() {
                            updateLifetimeStatsTextViews();
                            mainActivity.saveCache();
                        }
                    }, 3000);
                } else {
                    Toast.makeText(getActivity(), "Shower not saved. Network connection required.", Toast.LENGTH_SHORT).show();
                }
            } else if (isTimerRunning) {
                Toast.makeText(getActivity(), "Pause timer before saving.", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getActivity(), "Shower is too short to be saved!", Toast.LENGTH_SHORT).show();
        }

        else if (elapsedTimeMillis == 0) { //if anon
            mainActivity.setFragmentReturnableSlide(new SettingsFragment());
        }
        else {
            Toast.makeText(getActivity(), "Cannot open settings while timer is running.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLifetimeStatsTextViews() {

        double sec = (double)mainActivity.getGoalTimeMillis() / 1000;
        int min = (int)sec / 60;
        int sec_int = (int)sec % 60;
        textViewGoal.setText(String.format(Locale.getDefault(), "%d:%02d", min, sec_int));

        textViewTotalCost.setText("$" + formatCost.format(mainActivity.getTotalCost()));
        textViewTotalVolume.setText(formatVolume.format(mainActivity.getTotalVolume()) + " gallons");
        textViewTimeSpent.setText((int)(mainActivity.getTotalTimeMinutes() / 60) + " hours " + formatTime.format(mainActivity.getTotalTimeMinutes() % 60)  + " minutes");
        textViewAvgShowerLength.setText((int)(mainActivity.getAvgShowerLengthMinutes()) + " minutes " + formatTime.format(mainActivity.getAvgShowerLengthMinutes() % 1 * 60) + " seconds");
    }

    private void updateCachedText(){
        double sec = (double)mainActivity.getGoalTimeMillisCache() / 1000;
        int min = (int)sec / 60;
        int sec_int = (int)sec % 60;
        textViewGoal.setText(String.format(Locale.getDefault(), "%d:%02d", min, sec_int));

        textViewTotalCost.setText("$" + formatCost.format(mainActivity.getTotalCostCache()));
        textViewTotalVolume.setText(formatVolume.format(mainActivity.getTotalVolumeCache()) + " gallons");
        textViewTimeSpent.setText((int)(mainActivity.getTotalTimeMinutesCache() / 60) + " hours " + formatTime.format(mainActivity.getTotalTimeMinutesCache() % 60)  + " minutes");
        textViewAvgShowerLength.setText((int)(mainActivity.getAvgShowerLengthMinutesCache()) + " minutes " + formatTime.format(mainActivity.getAvgShowerLengthMinutesCache() % 1 * 60) + " seconds");
    }
}
