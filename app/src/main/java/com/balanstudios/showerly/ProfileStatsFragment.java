package com.balanstudios.showerly;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileStatsFragment extends Fragment {

    private MainActivity mainActivity;

    private CardView cardViewHistory;

    private TextView textViewTime;
    private TextView textViewLength;
    private TextView textViewGoalMet;
    private TextView textViewVolume;
    private TextView textViewCost;
    private TextView textViewGoalPercent;
    private TextView textViewGoalStreak;
    private TextView textViewTimeSpent;
    private TextView textViewAvgShowerLength;
    private TextView textViewTotalCost;
    private TextView textViewShortest;
    private TextView textViewLongest;
    private TextView textViewTotalVolume;

    private Button buttonHistory;

    private PieChart pieChartGoal;

    private DecimalFormat formatVolume = new DecimalFormat("0.0");
    private DecimalFormat formatCost = new DecimalFormat("0.00");
    private DecimalFormat formatTime = new DecimalFormat("0");

    public ProfileStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile_stats, container, false);

        mainActivity = (MainActivity) getActivity();

        cardViewHistory = v.findViewById(R.id.cardViewHistory);

        textViewTime = v.findViewById(R.id.textViewTime);
        textViewLength = v.findViewById(R.id.textViewLength);
        textViewGoalMet = v.findViewById(R.id.textViewGoalMet);
        textViewVolume = v.findViewById(R.id.textViewVolumeLast);
        textViewCost = v.findViewById(R.id.textViewCost);
        textViewGoalPercent = v.findViewById(R.id.textViewGoalPercent);
        textViewGoalStreak = v.findViewById(R.id.textViewGoalStreak);
        textViewTimeSpent = v.findViewById(R.id.textViewTimeSpent);
        textViewAvgShowerLength = v.findViewById(R.id.textViewAvgShowerLength);
        textViewTotalCost = v.findViewById(R.id.textViewTotalCost);
        textViewShortest = v.findViewById(R.id.textViewShortest);
        textViewLongest = v.findViewById(R.id.textViewLongest);
        textViewTotalVolume = v.findViewById(R.id.textViewTotalVolume);

        buttonHistory = v.findViewById(R.id.buttonHistory); buttonHistory.setOnClickListener(onClickListener);

        //pie chart init
        pieChartGoal = v.findViewById(R.id.pieChartGoal);
        initPieChart();

        updateLastShower();
        updateStats();

        return v;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.buttonHistory:
                    mainActivity.setFragmentReturnableSubtle(new HistoryFragment());
                    //hide nav bar
                    break;
            }
        }
    };

    public void updateLastShower(){
        if (mainActivity.getUserShowers() != null && mainActivity.getUserShowers().size() > 0){
            textViewTime.setText((mainActivity.getUserShowers()
                    .get((mainActivity.getUserShowers().size() - 1)).getDate()) + " at " +
                    (mainActivity.getUserShowers().get((mainActivity.getUserShowers().size() - 1)).getTime()));
            textViewLength.setText("Length: " + TimeFormat.valueToString((int)(mainActivity.getUserShowers()
                    .get((mainActivity.getUserShowers().size() - 1)).getShowerLengthMillis() / 1000)));
            textViewVolume.setText("Gallons consumed: " + formatVolume.format(mainActivity.getUserShowers()
                    .get((mainActivity.getUserShowers().size() - 1)).getVolume()));
            textViewCost.setText("Cost: $" + formatCost.format(mainActivity.getUserShowers()
                    .get((mainActivity.getUserShowers().size() - 1)).getCost()));

            if (mainActivity.getUserShowers().get((mainActivity.getUserShowers().size() - 1)).isGoalMet()){
                textViewGoalMet.setText("Goal Met: Yes");
            }
            else {
                textViewGoalMet.setText("Goal Met: No");
            }

        }
        else {
            cardViewHistory.setVisibility(View.GONE);

        }
    }

    public void updateStats(){
        if (mainActivity.getUserShowers().size() > 0) {
            textViewTotalCost.setText("Total Cost: $" + formatCost.format(mainActivity.getTotalCost()));
            textViewTotalVolume.setText("Total Volume Consumed: " + formatVolume.format(mainActivity.getTotalVolume()) + " gallons");
            textViewTimeSpent.setText("Time Spent Showering: " + (int) (mainActivity.getTotalTimeMinutes() / 60) + " hours " +
                    formatTime.format(mainActivity.getTotalTimeMinutes() % 60) + " minutes");
            textViewAvgShowerLength.setText("Average Shower Length: " + (int) (mainActivity.getAvgShowerLengthMinutes()) + " minutes " +
                    formatTime.format(mainActivity.getAvgShowerLengthMinutes() % 1 * 60) + " seconds");

            long high = Long.MIN_VALUE;
            String highDate = "";
            long low = Long.MAX_VALUE;
            String lowDate = "";

            ArrayList<Double> avgShowerLengths = new ArrayList<>();

            for (Shower shower : mainActivity.getUserShowers()) {
                if (shower.getShowerLengthMillis() > high) {
                    high = shower.getShowerLengthMillis();
                    highDate = shower.getDate();
                }
                if (shower.getShowerLengthMillis() < low) {
                    low = shower.getShowerLengthMillis();
                    lowDate = shower.getDate();
                }
            }
            textViewShortest.setText("Shortest Shower: " + formatTime.format(low / 1000 / 60) + " minutes " + formatTime.format(low / 1000 % 60) +
                    " seconds on " + lowDate);
            textViewLongest.setText("Longest Shower: " + formatTime.format(high / 1000 / 60) + " minutes " + formatTime.format(high / 1000 % 60) +
                    " seconds on " + highDate);


        }
    }

    public void initPieChart(){
        float numGoalMet = 0;
        float percentageGoalMet = 100;
        int goalStreak = 0;
        int goalStreakComp = 0;

        pieChartGoal.setUsePercentValues(true);
        pieChartGoal.getDescription().setEnabled(false);
        pieChartGoal.setTouchEnabled(false);
        pieChartGoal.setDrawHoleEnabled(false);

        ArrayList<PieEntry> entries = new ArrayList<>();

        for (Shower shower : mainActivity.getUserShowers()){
            if (shower.isGoalMet()){
                numGoalMet++;
                goalStreakComp++;
            }
            if (goalStreakComp > goalStreak){
                goalStreak = goalStreakComp;
            }
            if (!shower.isGoalMet()){
                goalStreakComp = 0;
            }
        }

        if (mainActivity.getUserShowers().size() > 0 ) {
            percentageGoalMet = numGoalMet / mainActivity.getUserShowers().size();

            entries.add(new PieEntry(percentageGoalMet, ""));
            entries.add(new PieEntry(1 - percentageGoalMet, ""));

            textViewGoalPercent.setText(formatTime.format(percentageGoalMet * 100) + "% of Showers with Goal Met");
            //change emojis with longer streak
            if (goalStreak >= 100){
                textViewGoalStreak.setText(getString(R.string.world_emoji) + " Longest Goal Streak: " + goalStreak + " showers");
            }
            else if (goalStreak >= 50){
                textViewGoalStreak.setText(getString(R.string.wave_emoji) + " Longest Goal Streak: " + goalStreak + " showers");
            }
            else if (goalStreak >= 10){
                textViewGoalStreak.setText(getString(R.string.splash_emoji)+ " Longest Goal Streak: " + goalStreak + " showers");
            }
            else if (goalStreak >= 1){
                textViewGoalStreak.setText(getString(R.string.water_emoji) + " Longest Goal Streak: " + goalStreak + " showers");
            }
            else {
                textViewGoalStreak.setText(" Longest Goal Streak: " + goalStreak + " showers");
            }

        }
        else {
            entries.add(new PieEntry(70, ""));
            entries.add(new PieEntry(30, ""));
        }


        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(0f);

        ArrayList<Integer> colors = new ArrayList<>();
        if (mainActivity.getUserShowers().size() > 0) {
            colors.add(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            if (mainActivity.isDarkMode()) {
                colors.add(ContextCompat.getColor(getActivity(), R.color.transparentDarkMode));
            } else {
                colors.add(ContextCompat.getColor(getActivity(), R.color.transparentLightMode));
            }
        }
        else {
            if (mainActivity.isDarkMode()) {
                colors.add(ContextCompat.getColor(getActivity(), R.color.transparentDarkMode));
                colors.add(ContextCompat.getColor(getActivity(), R.color.colorCardBackgroundDark));
            } else {
                colors.add(ContextCompat.getColor(getActivity(), R.color.transparentLightMode));
                colors.add(ContextCompat.getColor(getActivity(), R.color.colorCardBackgroundLight));
            }
        }

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(0f);

        pieChartGoal.setData(data);

        pieChartGoal.getLegend().setCustom(new ArrayList<LegendEntry>());


    }

}
