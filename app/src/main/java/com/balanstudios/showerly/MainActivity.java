package com.balanstudios.showerly;

import android.Manifest;

import androidx.lifecycle.Lifecycle;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity {

    //stats access keys
    public static final String KEY_USER_SHOWERS = "USER_SHOWERS";
    public static final String KEY_TOP_25 = "TOP_25";
    public static final String KEY_LOCAL_TOP_25 = "LOCAL_TOP_25";
    public static final String KEY_TOTAL_TIME = "TOTAL_TIME";
    public static final String KEY_TOTAL_COST = "TOTAL_COST";
    public static final String KEY_TOTAL_VOLUME = "TOTAL_VOLUME";
    public static final String KEY_AVG_SHOWER_LEN = "AVG_SHOWER_LEN";
    public static final String KEY_GOAL_TIME = "GOAL_TIME_MILLIS";
    public static final String KEY_NUM_SHOWERS = "KEY_NUM_SHOWERS";
    public static final String KEY_GOALS_MET = "KEY_GOALS_MET";
    public static final String KEY_CITY = "CITY";
    public static final String KEY_GPM = "GALLONS_PER_MINUTE";
    public static final String KEY_DPG = "DOLLARS_PER_GALLON";
    public static final String KEY_DARK_MODE = "DARK_MODE";
    public static final String KEY_IS_INTERVAL_ALERTS = "IS_INTERVAL ALERTS";
    public static final String KEY_ALERT_FREQUENCY = "ALERT_FREQUENCY";
    public static final String KEY_VIBRATE = "VIBRATE";
    public static final String KEY_DATA_SHARED = "DATA_SHARED";
    //cache
    public static final String SHARED_PREFS = "sharedPrefs";
    public static int interval_end_ID;
    public static int timer_end_ID;
    long backPressedTime = 0;
    String userShowersJson = "";
    String top25UsersJson = "";
    String localTop25UsersJson = "";
    private BottomNavigationView mainNavBar;
    private HomeFragment homeFragment;
    private LeaderboardsFragment leaderboardsFragment;
    private FragmentManager fragmentManager;
    private boolean settingsChanged = false;
    //sound
    private SoundPool soundPool;
    //firebase and user data storage.
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    private DocumentReference globalLeaderboardsReference;
    private DocumentReference localLeaderboardsReference;
    private Gson gson = new Gson();
    private String email;
    private String displayName;
    private String city = "";
    private ArrayList<Shower> userShowers = new ArrayList<>();
    private ArrayList<ShowerlyUser> top25Users = new ArrayList<>();
    private ArrayList<ShowerlyUser> localTop25Users = new ArrayList<>();
    private long goalTimeMillis = 600000;
    private double totalCost = 0;
    private double totalVolume = 0;
    private double totalTimeMinutes = 0;
    private double avgShowerLengthMinutes = 0;
    private int numShowers = 0;
    private int goalsMet = 0;
    //location services
    private double latitude = 0.0;
    private double longitude = 0.0;
    private Location network_loc = null, final_loc = null;
    private LocationManager locationManager;
    //settings
    private boolean isDarkMode = false;
    private boolean isIntervalAlertsOn = true;
    private boolean isVibrateEnabled = false;
    private boolean isDataShared = false;
    private long alertFrequencySeconds = 300;
    private ArrayList<Shower> userShowersCache = new ArrayList<>();
    private long goalTimeMillisCache = 600000;
    private double totalCostCache = 0;
    private double totalVolumeCache = 0;
    private double totalTimeMinutesCache = 0;
    private double avgShowerLengthMinutesCache = 0;
    private int numShowersCache = 0;
    private int goalsMetCache = 0;
    private String cityCache;
    public static final long minShowerLength = 15000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //firebase code
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (!firebaseAuth.getCurrentUser().isAnonymous()) {

            if (currentUser != null) {
                email = currentUser.getEmail();
                displayName = currentUser.getDisplayName();
            }

            documentReference = db.collection("Users").document(currentUser.getEmail()); //refers to document with user's shower list
            globalLeaderboardsReference = db.collection("Leaderboards").document("Top 25 Users");

            loadUserShowersFromFireStore();
            loadUserLifetimeStatsFromFireStore();
            loadUserDisplayName();
            loadUserPreferencesFirebase(); //loads leaderboards (because city info needed) and edit profile information
            loadCache();
            loadSettings(); //load device setting information
            loadSettingsFromFirestore(); //load firestore account settings information

            //leaderboards are loaded from within loadUserPreferencesFirebase() to give time to load city info

            //location
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            commonInit();

        } else {
            loadAnonShowers();
            loadAnonStats();
            loadSettings();
            commonInit();
            mainNavBar.setVisibility(View.GONE);


        }
    }

    @Override
    protected void onDestroy() {
        if (firebaseAuth.getCurrentUser() != null) {
            if (isUserAnon()) {
                firebaseAuth.getCurrentUser().delete();
            }
        }
        soundPool.release();
        soundPool = null;
        super.onDestroy();
    }


    public void commonInit() {
        //sound
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .build();
        interval_end_ID = soundPool.load(this, R.raw.interval_end, 1);
        timer_end_ID = soundPool.load(this, R.raw.timer_end, 1);

        if (isDarkMode) {
            setTheme(R.style.ShowerTimerDark);
        }

        setContentView(R.layout.activity_main);


        homeFragment = new HomeFragment();
        leaderboardsFragment = new LeaderboardsFragment();

        fragmentManager = getSupportFragmentManager();


        //navbar code
        mainNavBar = findViewById(R.id.mainNavBar);
        mainNavBar.setSelectedItemId(R.id.navItemHome);
        setFragment(homeFragment);

        mainNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navItemHome:
                        if (mainNavBar.getSelectedItemId() != R.id.navItemHome) {
                            setFragment(homeFragment);
                        }
                        return true;
                    case R.id.navItemProfile:
                        if (mainNavBar.getSelectedItemId() != R.id.navItemProfile) {
                            setFragment(new ProfileCollapseFragment());
                        }
                        return true;
                    case R.id.navItemLeaderboards:
                        if (mainNavBar.getSelectedItemId() != R.id.navItemLeaderboards) {
                            setFragment(leaderboardsFragment);
                        }
                        return true;
                    default:
                        return false;
                }

            }
        });

        AppRate.with(this) //ask if user wants to rate app
                .setInstallDays(10)
                .setLaunchTimes(7)
                .setRemindInterval(5)
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);
    }

    public void logOut() {

        saveSettingsToFirestore();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.putBoolean(SplashActivity.FIRST_RUN, false); //don't show intro again
        editor.apply();

        editor.putBoolean(SplashActivity.FIRST_RUN, false); //don't show intro again

        Intent restart = new Intent(MainActivity.this, SplashActivity.class);
        restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(restart);
        finish();
    }

    public void logOutAnon() {
        showAlertDialog("Are you sure?", "Proceeding will clear your guest data. Go to log in screen?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.clear();
                editor.putBoolean(SplashActivity.FIRST_RUN, false); //don't show intro again
                editor.apply();

                Intent restart = new Intent(MainActivity.this, SplashActivity.class);
                startActivity(restart);
                finish();

                firebaseAuth.getCurrentUser().delete();

            }
        });

    }

    public void sendResetPasswordEmail() {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Sent reset link to email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public boolean saveToFireStore(ArrayList<Shower> userShowers) {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            userShowersJson = gson.toJson(userShowers); //convert user showers list to storable format

            Map<String, Object> note = new HashMap<>();
            note.put(KEY_USER_SHOWERS, userShowersJson);

            documentReference.set(note, SetOptions.merge());
            return isNetworkConnected();
        } else
            return isNetworkConnected();
    }

    public boolean saveToFireStore(String KEY, long value) {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            Map<String, Object> note = new HashMap<>();
            note.put(KEY, value);

            documentReference.set(note, SetOptions.merge());
            return isNetworkConnected();
        } else
            return isNetworkConnected();
    }

    public boolean saveToFireStore(String KEY, double value) {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            Map<String, Object> note = new HashMap<>();
            note.put(KEY, value);

            documentReference.set(note, SetOptions.merge());
            return isNetworkConnected();
        } else
            return isNetworkConnected();
    }

    public boolean saveToFireStore(String KEY, String value) {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            Map<String, Object> note = new HashMap<>();
            note.put(KEY, value);

            documentReference.set(note, SetOptions.merge());
            return isNetworkConnected();
        } else
            return isNetworkConnected();
    }

    public boolean saveToFireStore(String KEY, Boolean value) {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            Map<String, Object> note = new HashMap<>();
            note.put(KEY, value);

            documentReference.set(note, SetOptions.merge());
            return isNetworkConnected();
        } else
            return isNetworkConnected();
    }

    public boolean saveToFireStore(String KEY01, double value01, String KEY02, double value02, String KEY03, double value03, String KEY04, double value04) {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            Map<String, Object> note = new HashMap<>();
            note.put(KEY01, value01);
            note.put(KEY02, value02);
            note.put(KEY03, value03);
            note.put(KEY04, value04);

            documentReference.set(note, SetOptions.merge());
            return isNetworkConnected();
        } else
            return isNetworkConnected();
    }

    public void saveAnonShowers(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        userShowersJson = gson.toJson(userShowers);
        editor.putString(KEY_USER_SHOWERS, userShowersJson);

        editor.apply();
    }

    public void saveAnonStats(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(KEY_AVG_SHOWER_LEN, Double.doubleToRawLongBits(avgShowerLengthMinutes));
        editor.putLong(KEY_TOTAL_COST, Double.doubleToRawLongBits(totalCost));
        editor.putLong(KEY_TOTAL_TIME, Double.doubleToRawLongBits(totalTimeMinutes));
        editor.putLong(KEY_TOTAL_VOLUME, Double.doubleToRawLongBits(totalVolume));
        editor.putLong(KEY_GOAL_TIME, goalTimeMillis);
        editor.putInt(KEY_NUM_SHOWERS, numShowers);

        editor.apply();
    }

    public void loadUserShowersFromFireStore() {
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            userShowersJson = documentSnapshot.getString(KEY_USER_SHOWERS);
                            Type typeList = new TypeToken<ArrayList<Shower>>() {
                            }.getType();
                            userShowers = gson.fromJson(userShowersJson, typeList);
                            if (userShowers == null) { //will be null if user is new
                                userShowers = new ArrayList<>();
                            }

                            numShowers = userShowers.size();
                            goalsMet = 0;
                            for (Shower shower : userShowers) {
                                if (shower.isGoalMet()) {
                                    goalsMet++;
                                }
                            }
                            saveCache();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Something went wrong when loading your showers!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loadAnonShowers() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        userShowersJson = sharedPreferences.getString(KEY_USER_SHOWERS, "");
        if (userShowersJson.length() > 0){
            Type typeList = new TypeToken<ArrayList<Shower>>() {
            }.getType();
            userShowers = gson.fromJson(userShowersJson, typeList);
        }

    }

    public void loadAnonStats() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        avgShowerLengthMinutes = Double.longBitsToDouble(sharedPreferences.getLong(KEY_AVG_SHOWER_LEN, Double.doubleToLongBits(0)));
        totalCost = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_COST, Double.doubleToLongBits(0)));
        totalTimeMinutes = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_TIME, Double.doubleToLongBits(0)));
        totalVolume = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_VOLUME, Double.doubleToLongBits(0)));
        goalTimeMillis = sharedPreferences.getLong(KEY_GOAL_TIME, 0);
        numShowers = sharedPreferences.getInt(KEY_NUM_SHOWERS, 0);
    }

    public void loadUserLifetimeStatsFromFireStore() {
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getDouble(KEY_TOTAL_TIME) != null && documentSnapshot.getDouble(KEY_TOTAL_COST) != null && documentSnapshot.getDouble(KEY_TOTAL_VOLUME) != null && documentSnapshot.getDouble(KEY_AVG_SHOWER_LEN) != null) {
                            totalTimeMinutes = documentSnapshot.getDouble(KEY_TOTAL_TIME);
                            totalCost = documentSnapshot.getDouble(KEY_TOTAL_COST);
                            totalVolume = documentSnapshot.getDouble(KEY_TOTAL_VOLUME);
                            avgShowerLengthMinutes = documentSnapshot.getDouble(KEY_AVG_SHOWER_LEN);
                            loadCache();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Something went wrong when loading your showers!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loadUserPreferencesFirebase() {
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getLong(KEY_GOAL_TIME) != null) {
                            goalTimeMillis = documentSnapshot.getLong(KEY_GOAL_TIME);
                        }
                        if (documentSnapshot.getString(KEY_CITY) != null) {
                            city = documentSnapshot.getString(KEY_CITY);
                        }
                        saveCache();
                        loadLeaderboards();
                    }
                });
    }

    public void setUserDisplayName(String displayName) {
        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        currentUser.updateProfile(profile);
    }


    public void loadUserDisplayName() {
        displayName = currentUser.getDisplayName();
    }

    public void saveCache() { //only call after retrieving data from firebase
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_USER_SHOWERS, gson.toJson(userShowers));
        editor.putLong(KEY_AVG_SHOWER_LEN, Double.doubleToRawLongBits(avgShowerLengthMinutes));
        editor.putLong(KEY_TOTAL_COST, Double.doubleToRawLongBits(totalCost));
        editor.putLong(KEY_TOTAL_TIME, Double.doubleToRawLongBits(totalTimeMinutes));
        editor.putLong(KEY_TOTAL_VOLUME, Double.doubleToRawLongBits(totalVolume));
        editor.putLong(KEY_GOAL_TIME, goalTimeMillis);
        editor.putInt(KEY_NUM_SHOWERS, numShowers);
        editor.putInt(KEY_GOALS_MET, goalsMet);
        editor.putString(KEY_CITY, city);
        editor.apply();

    }

    public void loadCache() { // load before firestore so it appears to load faster
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        Type typeList = new TypeToken<ArrayList<Shower>>() {
        }.getType();
        userShowersCache = gson.fromJson(sharedPreferences.getString(KEY_USER_SHOWERS, ""), typeList);
        if (userShowersCache == null) {
            userShowersCache = new ArrayList<>();
        }

        avgShowerLengthMinutesCache = Double.longBitsToDouble(sharedPreferences.getLong(KEY_AVG_SHOWER_LEN, Double.doubleToLongBits(0)));
        totalCostCache = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_COST, Double.doubleToLongBits(0)));
        totalTimeMinutesCache = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_TIME, Double.doubleToLongBits(0)));
        totalVolumeCache = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_VOLUME, Double.doubleToLongBits(0)));
        goalTimeMillisCache = sharedPreferences.getLong(KEY_GOAL_TIME, 600000);
        city = sharedPreferences.getString(KEY_CITY, "");


        numShowersCache = userShowersCache.size(); //Log.d("DEBUG", "" + userShowers); Log.d("DEBUG", numShowersCache + " " + numShowers);
        goalsMetCache = 0;
        for (Shower shower : userShowersCache) {
            if (shower.isGoalMet()) {
                goalsMetCache++;
            }
        }
    }

    public void saveSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(KEY_GPM, Double.doubleToRawLongBits(Shower.getGallonsPerMinute()));
        editor.putLong(KEY_DPG, Double.doubleToRawLongBits(Shower.getDollarsPerGallon()));
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);
        editor.putBoolean(KEY_IS_INTERVAL_ALERTS, isIntervalAlertsOn);
        editor.putBoolean(KEY_VIBRATE, isVibrateEnabled);
//        editor.putBoolean(KEY_DATA_SHARED, isDataShared);
        editor.putLong(KEY_ALERT_FREQUENCY, alertFrequencySeconds);

        if (isUserAnon()) {
            editor.putLong(KEY_GOAL_TIME, goalTimeMillis);
        }

        editor.apply();
    }

    public void saveSettingsToFirestore() {
        saveToFireStore(KEY_GPM, Shower.getGallonsPerMinute());
        saveToFireStore(KEY_DPG, Shower.getDollarsPerGallon());
        saveToFireStore(KEY_DARK_MODE, isDarkMode);
        saveToFireStore(KEY_IS_INTERVAL_ALERTS, isIntervalAlertsOn);
        saveToFireStore(KEY_VIBRATE, isVibrateEnabled);
        saveToFireStore(KEY_DATA_SHARED, isDataShared);
        saveToFireStore(KEY_ALERT_FREQUENCY, alertFrequencySeconds);
    }

    public void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        Shower.setGallonsPerMinute(Double.longBitsToDouble(sharedPreferences.getLong(KEY_GPM, Double.doubleToLongBits(2.1))));
        Shower.setDollarsPerGallon(Double.longBitsToDouble(sharedPreferences.getLong(KEY_DPG, Double.doubleToLongBits(.0015))));
        isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        isIntervalAlertsOn = sharedPreferences.getBoolean(KEY_IS_INTERVAL_ALERTS, true);
        isVibrateEnabled = sharedPreferences.getBoolean(KEY_VIBRATE, false);
//        isDataShared = sharedPreferences.getBoolean(KEY_DATA_SHARED, false);
        alertFrequencySeconds = sharedPreferences.getLong(KEY_ALERT_FREQUENCY, 300);

        if (isUserAnon()) {
            goalTimeMillis = sharedPreferences.getLong(KEY_GOAL_TIME, 600000);
        }

    }

    public void loadSettingsFromFirestore() {
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getDouble(KEY_GPM) != null && documentSnapshot.getDouble(KEY_DPG) != null && documentSnapshot.getBoolean(KEY_DARK_MODE) != null
                                && documentSnapshot.getBoolean(KEY_IS_INTERVAL_ALERTS) != null && documentSnapshot.getBoolean(KEY_VIBRATE) != null && documentSnapshot.getLong(KEY_ALERT_FREQUENCY) != null) {

                            Shower.setGallonsPerMinute(documentSnapshot.getDouble(KEY_GPM));
                            Shower.setDollarsPerGallon(documentSnapshot.getDouble(KEY_DPG));
//                            isIntervalAlertsOn = documentSnapshot.getBoolean(KEY_IS_INTERVAL_ALERTS);
                            isVibrateEnabled = documentSnapshot.getBoolean(KEY_VIBRATE);
                            alertFrequencySeconds = documentSnapshot.getLong(KEY_ALERT_FREQUENCY);

                            if (documentSnapshot.getBoolean(KEY_DATA_SHARED) != null) {
                                isDataShared = documentSnapshot.getBoolean(KEY_DATA_SHARED);
                            }

                            saveSettings();
                        }
                    }
                });

    }

    public boolean saveLeaderboards() {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            if (city.length() > 0) { //save local leaderboards
                localLeaderboardsReference = db.collection("Leaderboards").document(city);
                for (int i = 0; i < localTop25Users.size(); i++) {
                    localTop25Users.get(i).setPosition(i + 1);
                }

                localTop25UsersJson = gson.toJson(localTop25Users);

                Map<String, Object> note = new HashMap<>();
                note.put(KEY_LOCAL_TOP_25, localTop25UsersJson);

                localLeaderboardsReference.set(note);

            }

            for (int i = 0; i < top25Users.size(); i++) {
                top25Users.get(i).setPosition(i + 1);
            }

            top25UsersJson = gson.toJson(top25Users); //convert user list to storable format

            Map<String, Object> note = new HashMap<>();
            note.put(KEY_TOP_25, top25UsersJson);

            globalLeaderboardsReference.set(note);
            return isNetworkConnected();
        } else
            return isNetworkConnected();
    }

    public void loadLeaderboards() {
        //load local
        if (city.length() > 0) {
            localLeaderboardsReference = db.collection("Leaderboards").document(city);
            localLeaderboardsReference.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.getString(KEY_LOCAL_TOP_25) != null) {
                                Type typeList = new TypeToken<ArrayList<ShowerlyUser>>() {
                                }.getType();
                                localTop25Users = gson.fromJson(documentSnapshot.getString(KEY_LOCAL_TOP_25), typeList);
//                                Log.d("D", "Post load:" + localTop25Users);
                            }
                        }
                    });
        }

        globalLeaderboardsReference.get() //load global
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getString(KEY_TOP_25) != null) {
                            Type typeList = new TypeToken<ArrayList<ShowerlyUser>>() {
                            }.getType();
                            top25Users = gson.fromJson(documentSnapshot.getString(KEY_TOP_25), typeList);
                        }
                    }
                });
    }

    public void playAlertSound(int ALERT_ID) {
        soundPool.play(ALERT_ID, 1, 1, 0, 0, 1);
    }

    public void vibrate(int numVibrations) {
        if (isVibrateEnabled && ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            switch (numVibrations) {
                case 1:
                    long[] pattern01 = {0, 250};
                    v.vibrate(pattern01, -1);
                    break;
                case 2:
                    long[] pattern02 = {0, 150, 50, 150};
                    v.vibrate(pattern02, -1);

                    break;
                case 3:
                    long[] pattern03 = {0, 400, 50, 400, 50, 400};
                    v.vibrate(pattern03, -1);
                    break;
                default:
                    long[] pattern = {0, 250};
                    v.vibrate(pattern, -1);
                    break;
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Vibrate permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    //use to replace current fragment with new
    public void setFragment(Fragment f) {
        new Handler().post(new FragmentRunnable(fragmentManager, f) { //prevents initial response lag in navbar
        });

    }

    public void setFragmentReturnableSlide(Fragment f) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.mainFrame, f, "FRAGMENT")
                .addToBackStack(null)
                .commit();
    }

    public void setFragmentReturnableSubtle(Fragment f) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.transition_subtle_in, R.anim.fade_out, R.anim.fade_in, R.anim.transition_subtle_out)
                .replace(R.id.mainFrame, f, "FRAGMENT")
                .addToBackStack(null)
                .commit();
    }

    public BottomNavigationView getMainNavBar() {
        return mainNavBar;
    }

    //prevents accidental exits by user if presses back
    public void onBackPressed() {
        if (isSettingsChanged()) { //prevents user leaving without applying settings
            new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogStyle))
                    .setTitle("Are you sure?")
                    .setMessage("Do you want to leave without applying your changes?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            settingsChanged = false;
                            onBackPressed();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create().show();
        } else {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else if (backPressedTime + 2000 > System.currentTimeMillis()) {
                finish();
                moveTaskToBack(true);
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }

    public void showAlertDialog(String title, String message, DialogInterface.OnClickListener positiveOnClickListener) {
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogStyle))
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", positiveOnClickListener)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create().show();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public double calculateAvgShowerLengthMinutes() {
        double sum = 0;
        for (Shower shower : userShowers) {
            sum += shower.getShowerLengthMinutes();
        }
        avgShowerLengthMinutes = sum / (double) (userShowers.size());
        return avgShowerLengthMinutes;
    }

    public String findCity() {
        try {
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Cannot find location. Permission rejection.", Toast.LENGTH_SHORT).show();
        }

        if (network_loc != null) {
            final_loc = network_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        }
        else {
            Toast.makeText(this, "Cannot find location.", Toast.LENGTH_SHORT).show();
        }

        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                if (addresses.size() > 0) {
                    Toast.makeText(getApplicationContext(), "City found!", Toast.LENGTH_SHORT).show();
                    return addresses.get(0).getLocality();
                }
            }
        } catch (Exception e) {

        }
        return "";
    }

    public double getLatitude() {
        try {
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "Cannot find location. Permission rejection.", Toast.LENGTH_SHORT).show();
        }

        if (network_loc != null) {
            return network_loc.getLatitude();
        }
        return 0;
    }

    public double getLongitude() {
        try {
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "Cannot find location. Permission rejection.", Toast.LENGTH_SHORT).show();
        }

        if (network_loc != null) {
            return network_loc.getLongitude();
        }
        return 0;
    }

    public boolean isUserAnon() {
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().isAnonymous();
        }
        else {
            return true;
        }
    }

    public void deleteUser() {
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int j = 0;
                ShowerlyUser compUser = new ShowerlyUser(getEmail(), getDisplayName(), getAvgShowerLengthMinutes());
                while (j < top25Users.size()) {
                    if (top25Users.get(j).equals(compUser)) {
                        top25Users.remove(j);
                        j++;
                        if (j > 0 ) {
                            j--;
                        }
                    }
                    else {
                        j++;
                    }
                }
                while (j < localTop25Users.size()) {
                    if (localTop25Users.get(j).equals(compUser)) {
                        localTop25Users.remove(j);
                        j++;
                        if (j > 0 ) {
                            j--;
                        }
                    }
                    else {
                        j++;
                    }
                }

                saveLeaderboards();
                db.collection("Users").document(email).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            currentUser.delete();
                            Intent restart = new Intent(MainActivity.this, SplashActivity.class);
                            restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(restart);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        showAlertDialog("Are You Sure?", "Deleting your account will completely remove all of your saved data and it cannot be undone. Proceed?", onClickListener);
    }

    public void sortLowToHigh(ArrayList<ShowerlyUser> users){
        ShowerlyUser tempUser;
        for (int i = 0; i < users.size() - 1; i++){
            for (int j = i + 1; j < users.size(); j++){
                if (users.get(i).getAvgShowerLength() > users.get(j).getAvgShowerLength()){
                    tempUser = users.get(j);
                    users.set(j, users.get(i));
                    users.set(i, tempUser);
                }
            }
        }
    }

    /*
    GETTERS AND SETTERS
    */



    public ArrayList<Shower> getUserShowers() {
        return userShowers;
    }

    public void setUserShowers(ArrayList<Shower> userShowers) {
        this.userShowers = userShowers;
    }

    public long getGoalTimeMillis() {
        return goalTimeMillis;
    }

    public void setGoalTimeMillis(long goalTimeMillis) {
        this.goalTimeMillis = goalTimeMillis;
    }

    public double getTotalTimeMinutes() {
        return totalTimeMinutes;
    }

    public void addTotalTimeMinutes(double totalTimeMinutes) {
        this.totalTimeMinutes += totalTimeMinutes;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void addTotalCost(double totalCost) {
        this.totalCost += totalCost;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public void addTotalVolume(double totalVolume) {
        this.totalVolume += totalVolume;
    }

    public double getAvgShowerLengthMinutes() {
        return avgShowerLengthMinutes;
    }

    public String getEmail() {
        return currentUser.getEmail();
    }

    public String getDisplayName() {
        return currentUser.getDisplayName();
    }

    public ArrayList<Shower> getUserShowersCache() {
        return userShowersCache;
    }

    public void setUserShowersCache(ArrayList<Shower> userShowersCache) {
        this.userShowersCache = userShowersCache;
    }

    public long getGoalTimeMillisCache() {
        return goalTimeMillisCache;
    }

    public void setGoalTimeMillisCache(long goalTimeMillisCache) {
        this.goalTimeMillisCache = goalTimeMillisCache;
    }

    public double getTotalCostCache() {
        return totalCostCache;
    }

    public void setTotalCostCache(double totalCostCache) {
        this.totalCostCache = totalCostCache;
    }

    public double getTotalVolumeCache() {
        return totalVolumeCache;
    }

    public void setTotalVolumeCache(double totalVolumeCache) {
        this.totalVolumeCache = totalVolumeCache;
    }

    public double getTotalTimeMinutesCache() {
        return totalTimeMinutesCache;
    }

    public void setTotalTimeMinutesCache(double totalTimeMinutesCache) {
        this.totalTimeMinutesCache = totalTimeMinutesCache;
    }

    public double getAvgShowerLengthMinutesCache() {
        return avgShowerLengthMinutesCache;
    }

    public void setAvgShowerLengthMinutesCache(double avgShowerLengthMinutesCache) {
        this.avgShowerLengthMinutesCache = avgShowerLengthMinutesCache;
    }

    public int getNumShowers() {
        return numShowers;
    }

    public void setNumShowers(int numShowers) {
        this.numShowers = numShowers;
    }

    public int getGoalsMet() {
        return goalsMet;
    }

    public void setGoalsMet(int goalsMet) {
        this.goalsMet = goalsMet;
    }

    public int getNumShowersCache() {
        return numShowersCache;
    }

    public void setNumShowersCache(int numShowersCache) {
        this.numShowersCache = numShowersCache;
    }

    public int getGoalsMetCache() {
        return goalsMetCache;
    }

    public void setGoalsMetCache(int goalsMetCache) {
        this.goalsMetCache = goalsMetCache;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityCache() {
        return cityCache;
    }

    public void setCityCache(String cityCache) {
        this.cityCache = cityCache;
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }

    public long getAlertFrequencySeconds() {
        return alertFrequencySeconds;
    }

    public void setAlertFrequencySeconds(long alertFrequencySeconds) {
        this.alertFrequencySeconds = alertFrequencySeconds;
    }

    public boolean isIntervalAlertsOn() {
        return isIntervalAlertsOn;
    }

    public void setIntervalAlertsOn(boolean intervalAlertsOn) {
        isIntervalAlertsOn = intervalAlertsOn;
    }

    public boolean isVibrateEnabled() {
        return isVibrateEnabled;
    }

    public void setVibrateEnabled(boolean vibrateEnabled) {
        isVibrateEnabled = vibrateEnabled;
    }

    public boolean isSettingsChanged() {
        return settingsChanged;
    }

    public void setSettingsChanged(boolean settingsChanged) {
        this.settingsChanged = settingsChanged;
    }

    public ArrayList<ShowerlyUser> getTop25Users() {
        return top25Users;
    }

    public void setTop25Users(ArrayList<ShowerlyUser> top25Users) {
        this.top25Users = top25Users;
    }

    public ArrayList<ShowerlyUser> getLocalTop25Users() {
        return localTop25Users;
    }

    public void setLocalTop25Users(ArrayList<ShowerlyUser> localTop25Users) {
        this.localTop25Users = localTop25Users;
    }

    public boolean addUserGlobal(ShowerlyUser user) { //awful terrible ugly
        for (ShowerlyUser u : top25Users) { //don't add users if they're already there
            if (u.equals(user)) {
                return false;
            }
        }

        if (user.getAvgShowerLength() > 0) {
            if (top25Users.size() == 20 && user.getAvgShowerLength() > top25Users.get(top25Users.size() - 1).getAvgShowerLength()){
                return false;
            }
            
            if (top25Users.size() > 1) {
                int index = 0;
                for (int i = top25Users.size() - 1; i >= 0; i--) {
                    if (user.getAvgShowerLength() < top25Users.get(i).getAvgShowerLength()) {
                        index = i;
                    }

                }
                top25Users.add(index, user);
            } else if (top25Users.size() == 1) {
                if (user.getAvgShowerLength() < top25Users.get(0).getAvgShowerLength()) {
                    top25Users.add(0, user);
                } else {
                    top25Users.add(user);
                }
            } else if (top25Users.size() == 0) {
                top25Users.add(user);
            } else {
                Toast.makeText(this, "Must set display name in settings in order to access share showers.", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (top25Users.size() > 20) {
                top25Users.remove(top25Users.size() - 1);

            }
            return true;
        }
        return false;
    }

    public boolean addUserLocal(ShowerlyUser user) {
        for (ShowerlyUser u : localTop25Users) { //don't read users if they're already there
            if (u.equals(user)) {
                return false;
            }
        }

        if (user.getAvgShowerLength() > 0 && city.length() > 0) {
            if (localTop25Users.size() == 20 && user.getAvgShowerLength() > localTop25Users.get(localTop25Users.size() - 1).getAvgShowerLength()){
                return false;
            }
            
            if (localTop25Users.size() > 1) {
                int index = 0;
                for (int i = localTop25Users.size() - 1; i >= 0; i--) {
                    if (user.getAvgShowerLength() < localTop25Users.get(i).getAvgShowerLength()) {
                        index = i;
                    }

                }
                localTop25Users.add(index, user);
            } else if (localTop25Users.size() == 1) {
                if (user.getAvgShowerLength() < localTop25Users.get(0).getAvgShowerLength()) {
                    localTop25Users.add(0, user);
                } else {
                    localTop25Users.add(user);
                }
            } else if (localTop25Users.size() == 0) {
                localTop25Users.add(user);
            } else {
//                Toast.makeText(this, "Must set display name in settings in order to access share showers.", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (localTop25Users.size() > 20) {
                localTop25Users.remove(localTop25Users.size() - 1);

            }
            return true;
        }
        return false;
    }
    
    
    public boolean isDataShared() {
        return isDataShared;
    }

    public void setDataShared(boolean dataShared) {
        isDataShared = dataShared;
    }
}

class FragmentRunnable implements Runnable {
    FragmentManager fragmentManager;
    Fragment f;

    public FragmentRunnable(FragmentManager fm, Fragment f) {
        fragmentManager = fm;
        this.f = f;
    }

    @Override
    public void run() {
        fragmentManager.beginTransaction()
                .replace(R.id.mainFrame, f, "NAV_FRAGMENT")
                .commit();
    }


}