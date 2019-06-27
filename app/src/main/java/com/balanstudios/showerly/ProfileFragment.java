package com.balanstudios.showerly;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private MainActivity mainActivity;

    private ProfileSectionsPageAdapter sectionsPageAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private TextView textViewDisplayName;
    private TextView textViewQuickStats;
    private Button buttonEditProfile;

    private DecimalFormat formatVolume = new DecimalFormat("0");

    //firebase
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        mainActivity = (MainActivity)getActivity();

        sectionsPageAdapter = new ProfileSectionsPageAdapter(getChildFragmentManager());

        viewPager = v.findViewById(R.id.container);
        setUpViewPager(viewPager);

        tabLayout = v.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //tab icons
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_poll_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_calendar_24dp);
//        tabLayout.getTabAt(2).setIcon(R.drawable.ic_goal_24dp);

        textViewDisplayName = v.findViewById(R.id.textViewDisplayName);
        textViewQuickStats = v.findViewById(R.id.textViewQuickStats);
        buttonEditProfile = v.findViewById(R.id.buttonEditProfile); buttonEditProfile.setOnClickListener(onClickListener);

        mainActivity.loadUserDisplayName();
        mainActivity.loadUserShowersFromFireStore();

        if (mainActivity.getDisplayName() != null){
            textViewDisplayName.setText(mainActivity.getDisplayName());
        }
        else {
            textViewDisplayName.setText(mainActivity.getEmail().substring(0, mainActivity.getEmail().indexOf("@")));
        }

        mainActivity.loadCache();
        updateQuickStatsTextCached();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mainActivity.saveCache();
                updateQuickStatsText();
            }
        }, 3000);

        //show nav bar if hidden
        if (mainActivity.getMainNavBar().getVisibility() != View.VISIBLE) {
            mainActivity.getMainNavBar().setVisibility(View.VISIBLE);
        }


        return v;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.buttonEditProfile:
                    mainActivity.setFragmentReturnableSubtle(new EditProfileFragment());
                    if (mainActivity.getMainNavBar().getVisibility() == View.VISIBLE) {
                        mainActivity.getMainNavBar().startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_down));
                    }
                    mainActivity.getMainNavBar().setVisibility(View.GONE);
                    break;
            }
        }
    };

    private void setUpViewPager(ViewPager viewPager){
        ProfileSectionsPageAdapter adapter = new ProfileSectionsPageAdapter(getChildFragmentManager());
        adapter.addFragment(new ProfileStatsFragment(), "Stats");
        adapter.addFragment(new ProfileCalendarFragment(), "Calendar");
//        adapter.addFragment(new EditProfileFragment(), "Goals");
        viewPager.setAdapter(adapter);
    }

    private void updateQuickStatsText(){
        textViewQuickStats.setText(String.format(Locale.getDefault(), "%d showers | %d goals met | %s gallons", mainActivity.getNumShowers(), mainActivity.getGoalsMet(), formatVolume.format(mainActivity.getTotalVolume())));
    }

    private void updateQuickStatsTextCached() {
        textViewQuickStats.setText(String.format(Locale.getDefault(), "%d showers | %d goals met | %s gallons", mainActivity.getNumShowersCache(), mainActivity.getGoalsMetCache(), formatVolume.format(mainActivity.getTotalVolume())));
    }

}

class ProfileSectionsPageAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitleList = new ArrayList<>();

    ProfileSectionsPageAdapter(FragmentManager fm){
        super(fm);
    }

    public void addFragment(Fragment fragment, String title){
        fragmentList.add(fragment);
        fragmentTitleList.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
