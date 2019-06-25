package com.balanstudios.showerly;


import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Chronometer chronometer;
    private boolean isTimerRunning = false;
    private FloatingActionButton buttonToggleTimer;
    private Button buttonSave;
    private Button buttonReset;
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

    private Drawable toggleTimer;
    private DecimalFormat formatVolume = new DecimalFormat("0.0");
    private DecimalFormat formatCost = new DecimalFormat("0.00");

    private long elapsedTimeMillis = 0;
    private long goalTimeMillis = 600000;
    private double elapsedTimeMinutes = 0;
    private double instanceVolume = 0;
    private double instanceCost = 0;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
    private String instanceDate = DateFormat.getInstance().format(calendar.getTime()).substring(0, DateFormat.getInstance().format(calendar.getTime()).indexOf(" "));
    private String instanceTime = "";

    //firebase and user data storage. NOT INCLUDED IN MAIN ACTIVITY BECAUSE THERE IS DIFFERENT BEHAVIOUR DURING SAVE AND LOAD TO FIREBASE DEPENDING ON FRAGMENT. DON'T @ ME. I HATE REPEATING CODE TOO BUT IT'S NECESSARY
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    private Gson gson = new Gson();
    String userShowersJson = "";
    public static final String KEY_USER_SHOWERS = "USER_SHOWERS";

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        chronometer = v.findViewById(R.id.textTimer); chronometer.setOnChronometerTickListener(onChronometerTickListener);
        buttonToggleTimer = v.findViewById(R.id.floatingActionButtonToggleTimer); buttonToggleTimer.setOnClickListener(onClickListener);
        buttonSave = v.findViewById(R.id.buttonSave); buttonSave.setOnClickListener(onClickListener);
        buttonReset = v.findViewById(R.id.buttonReset); buttonReset.setOnClickListener(onClickListener);
        progressBarGoal = v.findViewById(R.id.progressBarGoal);
        progressBarOverflow = v.findViewById(R.id.progressBarOverflow);

        textViewDate = v.findViewById(R.id.textViewDate); textViewDate.setText(instanceDate);
        textViewQuickVolume = v.findViewById(R.id.textViewQuickVolume);
        textViewQuickCost = v.findViewById(R.id.textViewQuickCost);
        textViewGoal = v.findViewById(R.id.textViewGoal);
        textViewTimeSpent = v.findViewById(R.id.textViewTimeSpent);
        textViewAvgShowerLength = v.findViewById(R.id.textViewAvgShowerLength);
        textViewTotalVolume = v.findViewById(R.id.textViewTotalVolume);
        textViewTotalCost = v.findViewById(R.id.textViewTotalCost);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        documentReference = db.collection("Users").document(currentUser.getEmail()); //refers to document with user's shower list
        loadUserShowersFromFireStore();

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
                    resetTimer(view);
                    break;
                case R.id.buttonSave:
                    saveShower();
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

            //update progress bars
            int progress = (int)((double) elapsedTimeMillis /(double)goalTimeMillis * 100);
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
                if (((MainActivity) getActivity()).getMainNavBar().getVisibility() == View.VISIBLE) {
                    ((MainActivity) getActivity()).getMainNavBar().startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_down));
                }
                ((MainActivity) getActivity()).getMainNavBar().setVisibility(View.GONE);
                instanceTime = sdf.format(calendar.getTime());
            }
        }
        isTimerRunning = !isTimerRunning;
    }

    public void resetTimer(View v){
        // insert 'are you sure?' dialog here
        elapsedTimeMillis = 0;
        isTimerRunning = false;
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.stop();

        //reset stats here
        progressBarOverflow.setProgress(0);

        ((MainActivity) getActivity()).getMainNavBar().setVisibility(View.VISIBLE);
    }

    public void saveShower(){
        if (elapsedTimeMillis > 15000){

            //add shower to user's account (getUserShower used so that showers are stored locally rather than only on database ---> fewer required accesses from client)
            ((MainActivity) getActivity()).getUserShowers().add(new Shower(elapsedTimeMillis, instanceDate, instanceTime, elapsedTimeMillis <= goalTimeMillis));
            saveToFireStore(((MainActivity) getActivity()).getUserShowers());
        }
        else if (isTimerRunning){
            Toast.makeText(getActivity(), "Pause timer before saving.", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getActivity(), "Shower is too short to be saved!", Toast.LENGTH_SHORT).show();
    }

    private void saveToFireStore(ArrayList<Shower> userShowers) {

        userShowersJson = gson.toJson(userShowers); //convert user showers list to storable format

        Map<String, Object> note = new HashMap<>();
        note.put(KEY_USER_SHOWERS, userShowersJson);

        documentReference.set(note)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) { 
                    resetTimer(buttonSave);
                    Toast.makeText(getActivity(), "Saved to my showers!", Toast.LENGTH_SHORT).show();
                }
            })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error: shower not saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserShowersFromFireStore(){
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            userShowersJson = documentSnapshot.getString(KEY_USER_SHOWERS);
                            Type typeList = new TypeToken<ArrayList<Shower>>() {
                            }.getType();

                            ((MainActivity) getActivity()).setUserShowers((ArrayList<Shower>)gson.fromJson(userShowersJson, typeList));

                        }
                        else {
                            Toast.makeText(getActivity(), "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Something went wrong when loading your showers!", Toast.LENGTH_SHORT).show();
                    }
                });
    }




}
