package com.balanstudios.showerly;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderboardsFragment extends Fragment {

    private MainActivity mainActivity;

    private Handler handler = new Handler();

    private SectionsPageAdapter SectionsPageAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ProgressBar progressBar;

    public LeaderboardsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_leaderboards, container, false);

        mainActivity = (MainActivity) getActivity();

        viewPager = v.findViewById(R.id.container);
        tabLayout = v.findViewById(R.id.tabs);
        progressBar = v.findViewById(R.id.progressBarLeaderboards);

        new Handler().post(new TabsLeaderboardsInitRunnable(mainActivity, SectionsPageAdapter, getChildFragmentManager(), viewPager, tabLayout, progressBar));

        return v;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
//                case R.id.buttonAdd:
//                    if (mainActivity.getDisplayName() != null && mainActivity.getDisplayName().length() > 0 && mainActivity.getUserShowers().size() > 0) {
//
//                        if (mainActivity.addUserGlobal(new ShowerlyUser(mainActivity.getEmail(), mainActivity.getDisplayName(), mainActivity.getAvgShowerLengthMinutes()))) {
//                            mainActivity.setDataShared(true);
//                            mainActivity.saveSettings();
//                            mainActivity.saveSettingsToFirestore();
//
//                            mainActivity.saveLeaderboards();
//
//                            linearLayoutButton.setVisibility(View.GONE);
//
//                        }
//
//                    } else if (mainActivity.getDisplayName() == null || mainActivity.getDisplayName().length() == 0) {
//                        Toast.makeText(mainActivity, "Must set display name to share your showers", Toast.LENGTH_SHORT).show();
//                    } else if (mainActivity.getUserShowers().size() == 0) {
//                        Toast.makeText(mainActivity, "No shower data available", Toast.LENGTH_SHORT).show();
//                    }
//
//                    break;
            }
        }
    };


    class SectionsPageAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        SectionsPageAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
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

    class TabsLeaderboardsInitRunnable implements Runnable {

        Context context;
        SectionsPageAdapter SectionsPageAdapter;
        FragmentManager fragmentManager;
        ViewPager viewPager;
        TabLayout tabLayout;
        ProgressBar progressBar;

        public TabsLeaderboardsInitRunnable(Context context, SectionsPageAdapter SectionsPageAdapter, FragmentManager fragmentManager, ViewPager viewPager, TabLayout tabLayout, ProgressBar progressBar) {
            this.context = context;
            this.SectionsPageAdapter = SectionsPageAdapter;
            this.fragmentManager = fragmentManager;
            this.viewPager = viewPager;
            this.tabLayout = tabLayout;
            this.progressBar = progressBar;
        }

        @Override
        public void run() {
            SectionsPageAdapter = new SectionsPageAdapter(fragmentManager);

            setUpViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);

            if (mainActivity.isDarkMode()){
                tabLayout.setTabTextColors(ContextCompat.getColor(mainActivity, R.color.colorLightText), ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            }

            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in_button));
            viewPager.setVisibility(View.VISIBLE);
            viewPager.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in_button));
            progressBar.setVisibility(View.GONE);

        }

        private void setUpViewPager(ViewPager viewPager) {
            SectionsPageAdapter adapter = new SectionsPageAdapter(fragmentManager);
            String city = mainActivity.getCity();
            if (city.length() == 0){
                city = "Local";
            }
            adapter.addFragment(new LocalLeaderboardsFragment(), city);
            adapter.addFragment(new GlobalLeaderboardsFragment(), "Global");
            viewPager.setAdapter(adapter);
        }
    }
}



