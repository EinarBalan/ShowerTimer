package com.balanstudios.showerly;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class LocalLeaderboardsFragment extends Fragment {

    private MainActivity mainActivity;

    private RecyclerView leaderboardsRecycler;
    private RecyclerView.Adapter leaderboardsAdapter;
    private RecyclerView.LayoutManager leaderboardsLayoutManager;

    private TextView textViewNotEnoughUsers;

    public LocalLeaderboardsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_local_leaderboards, container, false);

        mainActivity = (MainActivity) getActivity();
        textViewNotEnoughUsers = v.findViewById(R.id.textViewNotEnoughUsers);

        leaderboardsRecycler = v.findViewById(R.id.recyclerViewLocalLeaderboards);
        new Handler().post(new LeaderboardsRecyclerRunnable(v));

        if (mainActivity.getLocalTop25Users().size() > 3) {
            leaderboardsRecycler.setVisibility(View.VISIBLE);
            leaderboardsRecycler.startAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.fade_in_button));
            textViewNotEnoughUsers.setVisibility(View.GONE);
        }
        else {
            leaderboardsRecycler.setVisibility(View.GONE);
            textViewNotEnoughUsers.setVisibility(View.VISIBLE);
        }

        if (mainActivity.getCity().length() == 0){
            textViewNotEnoughUsers.setText("In order to see the local leaderboards you need to set your location in the settings.");
        }

        if (mainActivity.getDisplayName() != null && mainActivity.getDisplayName().length() > 0 && mainActivity.getUserShowers().size() > 0) {
            ShowerlyUser user = new ShowerlyUser(mainActivity.getEmail(), mainActivity.getDisplayName(), mainActivity.getAvgShowerLengthMinutes());
            mainActivity.addUserGlobal(user);
            mainActivity.addUserLocal(user);
//            Log.d("D", " " + mainActivity.getLocalTop25Users());
            mainActivity.saveSettings();
            mainActivity.saveSettingsToFirestore();

            mainActivity.sortLowToHigh(mainActivity.getLocalTop25Users());

            mainActivity.saveLeaderboards();

        }

        return v;
    }

    class LeaderboardsRecyclerRunnable implements Runnable {

        View v;

        public LeaderboardsRecyclerRunnable(View v) {
            this.v = v;
        }

        @Override
        public void run() {
            leaderboardsAdapter = new LeaderboardsAdapter(mainActivity.getLocalTop25Users());
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try {

                        leaderboardsRecycler.setHasFixedSize(false);
                        leaderboardsRecycler.setNestedScrollingEnabled(false);
                        leaderboardsLayoutManager = new LinearLayoutManager(getActivity());

                        leaderboardsRecycler.setLayoutManager(leaderboardsLayoutManager);
                        leaderboardsRecycler.setAdapter(leaderboardsAdapter);

                        leaderboardsRecycler.addItemDecoration(new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
