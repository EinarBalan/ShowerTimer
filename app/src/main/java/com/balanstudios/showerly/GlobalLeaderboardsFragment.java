package com.balanstudios.showerly;


import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class GlobalLeaderboardsFragment extends Fragment {

    private MainActivity mainActivity;

    private RecyclerView leaderboardsRecycler;
    private RecyclerView.Adapter leaderboardsAdapter;
    private RecyclerView.LayoutManager leaderboardsLayoutManager;

    public GlobalLeaderboardsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_global_leaderboards, container, false);

        mainActivity = (MainActivity) getActivity();

        leaderboardsRecycler = v.findViewById(R.id.recyclerViewLeaderboards);
        new Handler().post(new LeaderboardsRecyclerRunnable(v));

        leaderboardsRecycler.setVisibility(View.VISIBLE);
        leaderboardsRecycler.startAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.fade_in_button));

        if (mainActivity.getDisplayName() != null && mainActivity.getDisplayName().length() > 0 && mainActivity.getUserShowers().size() >= 2 && mainActivity.getAvgShowerLengthMinutes() >= 1.5) {
            ShowerlyUser user = new ShowerlyUser(mainActivity.getEmail(), mainActivity.getDisplayName(), mainActivity.getAvgShowerLengthMinutes());
            mainActivity.addUserGlobal(user);
            mainActivity.addUserLocal(user);
//            Log.d("D", " " + mainActivity.getLocalTop25Users());
            mainActivity.saveSettings();
            mainActivity.saveSettingsToFirestore();

            mainActivity.sortLowToHigh(mainActivity.getTop25Users());

            mainActivity.saveLeaderboards();


        } else if (mainActivity.getDisplayName() == null || mainActivity.getDisplayName().length() == 0) {
            Toast.makeText(mainActivity, "Set display name to participate in leaderboards", Toast.LENGTH_SHORT).show();
        } else if (mainActivity.getUserShowers().size() < 5) {
            Toast.makeText(mainActivity, "Need more data to participate in leaderboards", Toast.LENGTH_SHORT).show();
        }
        else if (mainActivity.getAvgShowerLengthMinutes() < 2.5) {
            Toast.makeText(mainActivity, "Average shower length is too short to participate in leaderboards", Toast.LENGTH_SHORT).show();
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
            leaderboardsAdapter = new LeaderboardsAdapter(mainActivity.getTop25Users());
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

class LeaderboardsAdapter extends RecyclerView.Adapter<LeaderboardsAdapter.LeaderboardsViewHolder> {

    private ArrayList<ShowerlyUser> top25users;
    private DecimalFormat timeFormat = new DecimalFormat("0");


    LeaderboardsAdapter(ArrayList<ShowerlyUser> top25users) {
        this.top25users = top25users;
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
                (int) (user.getAvgShowerLength()),
                timeFormat.format(user.getAvgShowerLength() % 1 * 60));

        leaderboardsViewHolder.textViewAvgShowerLength.setText(avgShowerLength);
    }

    @Override
    public int getItemCount() {
        return top25users.size();
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
}

