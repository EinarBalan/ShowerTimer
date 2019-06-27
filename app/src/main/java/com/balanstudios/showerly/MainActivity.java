package com.balanstudios.showerly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mainNavBar;
    private ProfileFragment profileFragment;
    private HomeFragment homeFragment;
    private LeaderboardsFragment leaderboardsFragment;
    private FragmentManager fragmentManager;
    long backPressedTime = 0;

    //firebase and user data storage.
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    private Gson gson = new Gson();
    String userShowersJson = "";
    private String email;
    private String displayName;

    private ArrayList<Shower> userShowers = new ArrayList<>();
    private long goalTimeMillis = 600000;
    private double totalCost = 0;
    private double totalVolume = 0;
    private double totalTimeMinutes = 0;
    private double avgShowerLengthMinutes = 0;
    private int numShowers = 0;
    private int goalsMet = 0;

    //stats access keys
    public static final String KEY_USER_SHOWERS = "USER_SHOWERS";
    public static final String KEY_TOTAL_TIME = "TOTAL_TIME";
    public static final String KEY_TOTAL_COST = "TOTAL_COST";
    public static final String KEY_TOTAL_VOLUME = "TOTAL_VOLUME";
    public static final String KEY_AVG_SHOWER_LEN = "AVG_SHOWER_LEN";
    public static final String KEY_GOAL_TIME = "GOAL_TIME_MILLIS";
    public static final String KEY_NUM_SHOWERS = "KEY_NUM_SHOWERS";
    public static final String KEY_GOALS_MET = "KEY_GOALS_MET";

    //cache
    public static final String SHARED_PREFS = "sharedPrefs";
    private ArrayList<Shower> userShowersCache = new ArrayList<>();
    private long goalTimeMillisCache = 600000;
    private double totalCostCache = 0;
    private double totalVolumeCache = 0;
    private double totalTimeMinutesCache = 0;
    private double avgShowerLengthMinutesCache = 0;
    private int numShowersCache = 0;
    private int goalsMetCache = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        profileFragment = new ProfileFragment();
        homeFragment = new HomeFragment();
        leaderboardsFragment = new LeaderboardsFragment();

        fragmentManager = getSupportFragmentManager();

        //firebase code
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
            displayName = currentUser.getDisplayName();
        }
        documentReference = db.collection("Users").document(currentUser.getEmail()); //refers to document with user's shower list
        loadUserShowersFromFireStore();
        loadUserLifetimeStatsFromFireStore();
        loadUserDisplayName();
        loadGoalTime();
        loadCache();

        //navbar code
        mainNavBar = findViewById(R.id.mainNavBar);
        mainNavBar.setSelectedItemId(R.id.navItemHome);
        setFragment(homeFragment);

        mainNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.navItemHome:
                        if (mainNavBar.getSelectedItemId() != R.id.navItemHome){
                            setFragment(homeFragment);
                        }
                        return true;
                    case R.id.navItemProfile:
                        if (mainNavBar.getSelectedItemId() != R.id.navItemProfile){
                            setFragment(profileFragment);
                        }
                        return true;
                    case R.id.navItemLeaderboards:
                        if (mainNavBar.getSelectedItemId() != R.id.navItemLeaderboards){
                            setFragment(leaderboardsFragment);
                        }
                        return true;
                    default:
                        return false;
                }

            }
        });

    }

    public void logOut(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear().apply();

        Intent restart = new Intent(MainActivity.this, SplashActivity.class);
        startActivity(restart);
        finish();
    }

    public void sendResetPasswordEmail(){
        firebaseAuth.sendPasswordResetEmail(email)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Sent reset link to email.", Toast.LENGTH_SHORT).show();
                }
                else {
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
        }
        else
            return isNetworkConnected();
    }

    public boolean saveToFireStore(String KEY, long value) {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            Map<String, Object> note = new HashMap<>();
            note.put(KEY, value);

            documentReference.set(note, SetOptions.merge());
            return isNetworkConnected();
        }
        else
            return isNetworkConnected();
    }

    public boolean saveToFireStore(String KEY, double value) {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            Map<String, Object> note = new HashMap<>();
            note.put(KEY, value);

            documentReference.set(note, SetOptions.merge());
            return isNetworkConnected();
        }
        else
            return isNetworkConnected();
    }

    public boolean saveToFireStore(String KEY, String value) {
        if (isNetworkConnected()) { //will only attempt to save if network is connected

            Map<String, Object> note = new HashMap<>();
            note.put(KEY, value);

            documentReference.set(note, SetOptions.merge());
            return isNetworkConnected();
        }
        else
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
        }
        else
            return isNetworkConnected();
    }

    public void loadUserShowersFromFireStore(){
            documentReference.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                userShowersJson = documentSnapshot.getString(KEY_USER_SHOWERS);
                                Type typeList = new TypeToken<ArrayList<Shower>>() {
                                }.getType();
                                userShowers = gson.fromJson(userShowersJson, typeList);
                                if (userShowers == null){ //will be null if user is new
                                    userShowers = new ArrayList<>();
                                }

                                numShowers = userShowers.size();
                                goalsMet = 0;
                                for (Shower shower : userShowers){
                                    if (shower.isGoalMet()){
                                        goalsMet++;
                                    }
                                }
                                //Log.d("Debug", "inital load from firestore" + userShowers);
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

    public void loadUserLifetimeStatsFromFireStore(){
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

    public void loadGoalTime(){
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getLong(KEY_GOAL_TIME) != null){
                            goalTimeMillis = documentSnapshot.getLong(KEY_GOAL_TIME);
                            saveCache();
                        }
                    }
                });
    }

    public void setUserDisplayName(String displayName){
        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        currentUser.updateProfile(profile);
    }

    public void loadUserDisplayName(){
        displayName = currentUser.getDisplayName();
    }

    public void saveCache(){ //only call after retrieving data from firebase
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
        editor.apply();

    }

    public void loadCache() { // load before firestore so it appears to load faster
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        Type typeList = new TypeToken<ArrayList<Shower>>() {
        }.getType();
        userShowersCache = gson.fromJson(sharedPreferences.getString(KEY_USER_SHOWERS, ""), typeList);
        if (userShowersCache == null){
            userShowersCache = new ArrayList<>();
        }

        avgShowerLengthMinutesCache = Double.longBitsToDouble(sharedPreferences.getLong(KEY_AVG_SHOWER_LEN, Double.doubleToLongBits(0)));
        totalCostCache = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_COST, Double.doubleToLongBits(0)));
        totalTimeMinutesCache = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_TIME, Double.doubleToLongBits(0)));
        totalVolumeCache = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_VOLUME, Double.doubleToLongBits(0)));
        goalTimeMillisCache = sharedPreferences.getLong(KEY_GOAL_TIME, 600000);


        numShowersCache = userShowersCache.size(); //Log.d("DEBUG", "" + userShowers); Log.d("DEBUG", numShowersCache + " " + numShowers);
        goalsMetCache = 0;
        for (Shower shower : userShowersCache){
            if (shower.isGoalMet()){
                goalsMetCache++;
            }
        }
    }

    //use to replace current fragment with new
    public void setFragment(Fragment f) {
        fragmentManager.beginTransaction()
                .replace(R.id.mainFrame, f, "NAV_FRAGMENT")
                .commit();
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

    public BottomNavigationView getMainNavBar(){
        return mainNavBar;
    }

    //prevents accidental exits by user if presses back
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
        else if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finish();
            moveTaskToBack(true);
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public double calculateAvgShowerLengthMinutes() {
        double sum = 0;
        for (Shower shower: userShowers){
            sum += shower.getShowerLengthMinutes();
        }
        avgShowerLengthMinutes = sum / (double)(userShowers.size());
        return avgShowerLengthMinutes;
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
        return email;
    }

    public String getDisplayName() {
        return displayName;
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
}
