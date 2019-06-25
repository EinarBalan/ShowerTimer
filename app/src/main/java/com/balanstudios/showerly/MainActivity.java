package com.balanstudios.showerly;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mainNavBar;
    private ProfileFragment profileFragment;
    private HomeFragment homeFragment;
    private LeaderboardsFragment leaderboardsFragment;
    private FragmentManager fragmentManager;
    long backPressedTime = 0;

    private ArrayList<Shower> userShowers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        profileFragment = new ProfileFragment();
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


    public ArrayList<Shower> getUserShowers() {
        return userShowers;
    }

    public void setUserShowers(ArrayList<Shower> userShowers) {
        this.userShowers = userShowers;
    }
}
