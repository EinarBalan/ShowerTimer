package com.balanstudios.showerly;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileCalendarFragment extends Fragment {

    private MainActivity mainActivity;

    private BarChart chartWeek;
    private BarChart chartMonth;
    private BarChart chartYear;
    
    private LinearLayout linearLayoutYear;
    private CardView cardViewYear;
    private LinearLayout linearLayoutMonth;
    private CardView cardViewMonth;
    private LinearLayout linearLayoutWeek;
    private CardView cardViewWeek;

    private TextView textViewNoShowerData;
    
    public ProfileCalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile_calendar, container, false);

        mainActivity = (MainActivity) getActivity();
        
        linearLayoutYear = v.findViewById(R.id.linearLayoutYear);
        cardViewYear = v.findViewById(R.id.cardViewYear);
        linearLayoutMonth = v.findViewById(R.id.linearLayoutMonth);
        cardViewMonth = v.findViewById(R.id.cardViewMonth);
        linearLayoutWeek = v.findViewById(R.id.linearLayoutWeek);
        cardViewWeek = v.findViewById(R.id.cardViewWeek);

        textViewNoShowerData = v.findViewById(R.id.textViewNoShowerData);

        
        if (mainActivity.getUserShowers().size() > 0) { //only show graph if there are recorded showers
            textViewNoShowerData.setVisibility(View.GONE);
            chartWeek = v.findViewById(R.id.chartWeek);
            chartWeek.setDrawBarShadow(false);
            chartWeek.setDrawValueAboveBar(false);
            chartWeek.setMaxVisibleValueCount(60);
            chartWeek.setPinchZoom(false);
            chartWeek.setDrawGridBackground(true);
            chartWeek.getDescription().setEnabled(false);
            initWeekChart();
        }
        else {
            linearLayoutWeek.setVisibility(View.GONE);
            cardViewWeek.setVisibility(View.GONE);
        }
        
        if (mainActivity.getUserShowers().size() > 7) { // only show month chart if it is significantly different from week chart
            chartMonth = v.findViewById(R.id.chartMonth);
            chartMonth.setDrawBarShadow(false);
            chartMonth.setDrawValueAboveBar(false);
            chartMonth.setMaxVisibleValueCount(60);
            chartMonth.setPinchZoom(false);
            chartMonth.setDrawGridBackground(true);
            chartMonth.getDescription().setEnabled(false);
            initMonthChart();
        }
        else {
            linearLayoutMonth.setVisibility(View.GONE);
            cardViewMonth.setVisibility(View.GONE);
        }

        if (mainActivity.getUserShowers().size() > 60) { // only show year chart if it is significantly different from month chart
            chartYear = v.findViewById(R.id.chartYear);
            chartYear.setDrawBarShadow(false);
            chartYear.setDrawValueAboveBar(false);
            chartYear.setMaxVisibleValueCount(60);
            chartYear.setPinchZoom(false);
            chartYear.setDrawGridBackground(true);
            chartYear.getDescription().setEnabled(false);
            initYearChart();
        }
        else {
            linearLayoutYear.setVisibility(View.GONE);
            cardViewYear.setVisibility(View.GONE);
        }
        
        return v;
    }

    public void initWeekChart(){
        ArrayList<BarEntry> weekEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 1; i < 8; i++) {
            try {
                weekEntries.add(new BarEntry(7-i, (float) mainActivity.getUserShowers().get(mainActivity.getUserShowers().size() - i).getShowerLengthMinutes()));
                labels.add(mainActivity.getUserShowers().get(mainActivity.getUserShowers().size() - i).getDate());
            }
            catch (Exception e){
                break;
            }
        }

        BarDataSet barDataSet = new BarDataSet(weekEntries, "Minutes Spent Showering");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(mainActivity, R.color.colorPrimaryDark));
        barDataSet.setColors(colors);


        BarData data = new BarData(barDataSet);
        data.setHighlightEnabled(false);
        data.setValueTextColor(colors.get(0));
        data.setBarWidth(.9f);


        chartWeek.setData(data);

        chartWeek.getAxisRight().setEnabled(false);
        XAxis xAxis = chartWeek.getXAxis();
        xAxis.setValueFormatter(new XAxisFormatter(reverseArrayList(labels)));
        xAxis.setPosition(XAxis.XAxisPosition.TOP);

        if (mainActivity.isDarkMode()){
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartWeek.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartWeek.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartWeek.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorDarkBackground));
            chartWeek.setGridBackgroundColor(ContextCompat.getColor(mainActivity, R.color.headerTextColor));
        }

    }
    
    public void initMonthChart(){
        ArrayList<BarEntry> monthEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 1; i < 30; i++) {
            try {
                monthEntries.add(new BarEntry(30 - i, (float) mainActivity.getUserShowers().get(mainActivity.getUserShowers().size() - i).getShowerLengthMinutes()));
//                labels.add("");
            }
            catch (Exception e){
                break;
            }
        }

        BarDataSet barDataSet = new BarDataSet(monthEntries, "Minutes Spent Showering");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(mainActivity, R.color.colorPrimaryDark));
        barDataSet.setColors(colors);

        BarData data = new BarData(barDataSet);
        data.setHighlightEnabled(false);
        data.setValueTextColor(colors.get(0));
        data.setValueTextSize(0f);
        data.setBarWidth(.3f);


        chartMonth.setData(data);

        chartMonth.getAxisRight().setEnabled(false);
        XAxis xAxis = chartMonth.getXAxis();
        xAxis.setValueFormatter(new XAxisFormatterNone());

        if (mainActivity.isDarkMode()){
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartMonth.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartMonth.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartMonth.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorDarkBackground));
            chartMonth.setGridBackgroundColor(ContextCompat.getColor(mainActivity, R.color.headerTextColor));
        }
    }
    
    public void initYearChart() {
        ArrayList<BarEntry> yearEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 1; i < 365; i++) {
            try {
                yearEntries.add(new BarEntry(365 - i, (float) mainActivity.getUserShowers().get(mainActivity.getUserShowers().size() - i).getShowerLengthMinutes()));
//                labels.add("");
            }
            catch (Exception e){
                break;
            }
        }

        BarDataSet barDataSet = new BarDataSet(yearEntries, "Minutes Spent Showering");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(mainActivity, R.color.colorPrimaryDark));
        barDataSet.setColors(colors);

        BarData data = new BarData(barDataSet);
        data.setHighlightEnabled(false);
        data.setValueTextColor(colors.get(0));
        data.setBarWidth(.025f);


        chartYear.setData(data);

        chartYear.getAxisRight().setEnabled(false);
        XAxis xAxis = chartYear.getXAxis();
        xAxis.setValueFormatter(new XAxisFormatterNone());

        if (mainActivity.isDarkMode()){
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartYear.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartYear.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartYear.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorDarkBackground));
            chartYear.setGridBackgroundColor(ContextCompat.getColor(mainActivity, R.color.headerTextColor));
        }
    }

    public static ArrayList<String> reverseArrayList(ArrayList<String> arrayList){
        ArrayList<String> reversed = new ArrayList<>();
        for (int i = arrayList.size() - 1; i >= 0; i--){
            reversed.add(arrayList.get(i));
        }
        return reversed;
    }


}



class XAxisFormatter extends IndexAxisValueFormatter {
    ArrayList<String> labels;
    public XAxisFormatter(ArrayList<String> labels) {
        this.labels = labels;
    }

    @Override
    public String getFormattedValue(float value) {
        if (labels.size() > value) {
            return labels.get((int) value);
        }
        return "";
    }
}


class XAxisFormatterNone extends IndexAxisValueFormatter {
    public XAxisFormatterNone() {
    }

    @Override
    public String getFormattedValue(float value) {
        return "";
    }
}

