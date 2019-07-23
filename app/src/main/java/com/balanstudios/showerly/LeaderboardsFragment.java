package com.balanstudios.showerly;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderboardsFragment extends Fragment {

    private MainActivity mainActivity;

    private Handler handler = new Handler();

    private RecyclerView leaderboardsRecycler;
    private RecyclerView.Adapter leaderboardsAdapter;
    private RecyclerView.LayoutManager leaderboardsLayoutManager;

    private Button buttonAdd;
    private TextView textViewAvgShowerLength;
    private LinearLayout linearLayoutButton;

    public LeaderboardsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_leaderboards, container, false);

        mainActivity = (MainActivity) getActivity();

        leaderboardsRecycler = v.findViewById(R.id.recyclerViewLeaderboards);
        new Thread(new LeaderboardsRecyclerRunnable(v)).start();

        buttonAdd = v.findViewById(R.id.buttonAdd); buttonAdd.setOnClickListener(onClickListener);
        textViewAvgShowerLength = v.findViewById(R.id.textViewShortestAvgShower);
        linearLayoutButton = v.findViewById(R.id.linearLayoutButton);

        if (mainActivity.isDataShared()){
            leaderboardsRecycler.setVisibility(View.VISIBLE);
            leaderboardsRecycler.startAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.fade_in_button));
            textViewAvgShowerLength.setVisibility(View.VISIBLE);
            linearLayoutButton.setVisibility(View.GONE);
        }


        return v;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonAdd:
                if (mainActivity.getDisplayName() != null && mainActivity.getDisplayName().length() > 0) {

                    if (mainActivity.addUser(new ShowerlyUser(mainActivity.getEmail(), mainActivity.getDisplayName(), mainActivity.getAvgShowerLengthMinutes()))){
                        mainActivity.setDataShared(true);
                        mainActivity.saveSettings();
                        mainActivity.saveSettingsToFirestore();

                        mainActivity.saveLeaderboards();

                        leaderboardsRecycler.setVisibility(View.VISIBLE);
                        textViewAvgShowerLength.setVisibility(View.VISIBLE);
                        linearLayoutButton.setVisibility(View.GONE);

                        leaderboardsAdapter.notifyDataSetChanged();
                    }

                }

                break;
        }
        }
    };

    class LeaderboardsRecyclerRunnable implements Runnable {

        View v;

        public LeaderboardsRecyclerRunnable(View v) {
            this.v = v;
        }

        @Override
        public void run() {
            leaderboardsAdapter = new LeaderboardsAdapter(mainActivity.getTop25Users());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {

                        leaderboardsRecycler.setHasFixedSize(false);
                        leaderboardsLayoutManager = new LinearLayoutManager(getActivity());

                        leaderboardsRecycler.setLayoutManager(leaderboardsLayoutManager);
                        leaderboardsRecycler.setAdapter(leaderboardsAdapter);

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}



class LeaderboardsAdapter extends RecyclerView.Adapter<LeaderboardsAdapter.LeaderboardsViewHolder>{

    private ArrayList<ShowerlyUser> top25users;
    private DecimalFormat timeFormat = new DecimalFormat("0");


    LeaderboardsAdapter(ArrayList<ShowerlyUser> top25users) {
        this.top25users = top25users;
    }

    public static class LeaderboardsViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewPos;
        public TextView textViewDisplayName;
        public TextView textViewAvgShowerLength;

        public LeaderboardsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPos = itemView.findViewById(R.id.textViewPos);
            textViewDisplayName = itemView.findViewById(R.id.textViewDisplayName);
            textViewAvgShowerLength = itemView.findViewById(R.id.textViewAvgShowerLength);
        }
    }

    @NonNull
    @Override
    public LeaderboardsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_leaderboard_user, viewGroup, false);
        LeaderboardsViewHolder leaderboardsViewHolder = new LeaderboardsViewHolder(v);
        return leaderboardsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardsViewHolder leaderboardsViewHolder, int i) {
        ShowerlyUser user = top25users.get(i);

        leaderboardsViewHolder.textViewPos.setText("" + (i + 1));
        leaderboardsViewHolder.textViewDisplayName.setText(user.getDisplayName());

        String avgShowerLength = String.format(Locale.getDefault(), "%sm %ss",
                (int)(user.getAvgShowerLength()),
                timeFormat.format(user.getAvgShowerLength() % 1 * 60));

        leaderboardsViewHolder.textViewAvgShowerLength.setText(avgShowerLength);
    }

    @Override
    public int getItemCount() {
        return top25users.size();
    }
}
