package com.balanstudios.showerly;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private ArrayList<Shower> userShowers = new ArrayList<>();
    private long goalTimeMillis = 600000;
    private double totalCost = 0;
    private double totalVolume = 0;
    private double totalTimeMinutes = 0;
    private double avgShowerLengthMinutes = 0;

    //firebase and user data storage.
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    private Gson gson = new Gson();
    String userShowersJson = "";
    public static final String KEY_USER_SHOWERS = "USER_SHOWERS";
    private String email;
    private String displayName;

    //stats access keys
    public static final String KEY_GOAL = "GOAL_TIME_MILLIS";
    public static final String KEY_TOTAL_TIME = "TOTAL_TIME";
    public static final String KEY_TOTAL_COST = "TOTAL_COST";
    public static final String KEY_TOTAL_VOLUME = "TOTAL_VOLUME";
    public static final String KEY_AVG_SHOWER_LEN = "AVG_SHOWER_LEN";

    //cache
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String CACHE_SHOWERS = "user showers";
    public static final String CACHE_TOTAL_TIME = "total time";
    public static final String CACHE_TOTAL_COST = "total cost";
    public static final String CACHE_TOTAL_VOLUME = "total volume";
    public static final String CACHE_AVG_SHOWER_LEN = "avg shower len";



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

    public void setCacheLifetimeStats(){ //only call after retrieving data from firebase
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(KEY_AVG_SHOWER_LEN, Double.doubleToRawLongBits(avgShowerLengthMinutes));
        editor.putLong(KEY_TOTAL_COST, Double.doubleToRawLongBits(totalCost));
        editor.putLong(KEY_TOTAL_TIME, Double.doubleToRawLongBits(totalTimeMinutes));
        editor.putLong(KEY_TOTAL_VOLUME, Double.doubleToRawLongBits(totalVolume));
        editor.apply();
    }

    public void loadCacheLifetimeStats() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        avgShowerLengthMinutes = Double.longBitsToDouble(sharedPreferences.getLong(KEY_AVG_SHOWER_LEN, Double.doubleToLongBits(0)));
        totalCost = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_COST, Double.doubleToLongBits(0)));
        totalTimeMinutes = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_TIME, Double.doubleToLongBits(0)));
        totalVolume = Double.longBitsToDouble(sharedPreferences.getLong(KEY_TOTAL_VOLUME, Double.doubleToLongBits(0)));
    }

    //use to replace current fragment with new
    public void setFragment(Fragment f) {
        fragmentManager.beginTransaction()
                .replace(R.id.mainFrame, f, "NAV_FRAGMENT")
                .commit();
    }

    public BottomNavigationView getMainNavBar(){
        return mainNavBar;
    }

    //prevents accidental exits by user if presses back
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
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
}
