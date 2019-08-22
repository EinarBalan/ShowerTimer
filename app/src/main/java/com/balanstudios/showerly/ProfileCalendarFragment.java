package com.balanstudios.showerly;


import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * To anyone who may be reading this code: I'm sorry. It's pure spaghetti. But hey, it works! That's all that matters, right? Right???
 */

public class ProfileCalendarFragment extends Fragment {

    private static int dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    private MainActivity mainActivity;
    private BarChart chartDay;
    private BarChart chartWeek;
    private BarChart chartMonth;
    private BarChart chartYear;
    private CardView cardViewDay;
    private CardView cardViewYear;
    private CardView cardViewMonth;
    private CardView cardViewWeek;
    private TextView textViewNoShowerData;
    private TextView textViewDay;
    private TextView textViewMonth;
    private TextView textViewYear;
    private TextView textViewWeek;
    private boolean isDayChartSelected = false;
    private boolean isWeekChartSelected = false;
    private boolean isMonthChartSelected = false;
    private boolean isYearChartSelected = false;
    private ShowerDataOrganizer graphDataOrganizer;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.chartWeek:
                    isWeekChartSelected = !isWeekChartSelected;
                    if (isWeekChartSelected) {
                        chartWeek.getData().setValueTextSize(10f);
                    } else {
                        chartWeek.getData().setValueTextSize(0f);
                    }
                    chartWeek.setDrawValueAboveBar(isWeekChartSelected);
                    break;

            }
        }
    };


    public ProfileCalendarFragment() {
        // Required empty public constructor
    }

    public static ArrayList<String> reverseArrayList(ArrayList<String> arrayList) {
        ArrayList<String> reversed = new ArrayList<>();
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            reversed.add(arrayList.get(i));
        }
        return reversed;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile_calendar, container, false);

        mainActivity = (MainActivity) getActivity();

        cardViewDay = v.findViewById(R.id.cardViewDay);
        cardViewYear = v.findViewById(R.id.cardViewYear);
        cardViewMonth = v.findViewById(R.id.cardViewMonth);
        cardViewWeek = v.findViewById(R.id.cardViewWeek);

        textViewDay = v.findViewById(R.id.textViewDay);
        textViewNoShowerData = v.findViewById(R.id.textViewNoShowerData);
        textViewMonth = v.findViewById(R.id.textViewMonth);
        textViewYear = v.findViewById(R.id.textViewYear);
        textViewWeek = v.findViewById(R.id.textViewWeek);

        graphDataOrganizer = new ShowerDataOrganizer(mainActivity.getUserShowers());

        if (graphDataOrganizer.shouldShowDayChart()) { //only show graph if there's more than one shower in that day
            textViewNoShowerData.setVisibility(View.GONE);
            chartDay = v.findViewById(R.id.chartDay);
            initDayChart();
        } else {
            cardViewDay.setVisibility(View.GONE);
        }
        
        if (graphDataOrganizer.shouldShowWeekChart()) { //only show graph if there are recorded showers
            textViewNoShowerData.setVisibility(View.GONE);
            chartWeek = v.findViewById(R.id.chartWeek);
            initWeekChart();
        } else {
            cardViewWeek.setVisibility(View.GONE);
        }

        if (graphDataOrganizer.shouldShowMonthChart()) { //only show graph if there are recorded showers
            textViewNoShowerData.setVisibility(View.GONE);
            chartMonth = v.findViewById(R.id.chartMonth);
            initMonthChart();
        } else {
            cardViewMonth.setVisibility(View.GONE);
        }

        if (graphDataOrganizer.shouldShowYearChart()) { // only show year chart if it is significantly different from month chart
            textViewNoShowerData.setVisibility(View.GONE);
            chartYear = v.findViewById(R.id.chartYear);
            initYearChart();
        } else {
            cardViewYear.setVisibility(View.GONE);
        }

        return v;
    }

    public void initDayChart() {
        textViewDay.setText("Shower Length - " + DateHandler.currentDateString + " Trend");
        chartDay.setDrawBarShadow(false);
        chartDay.setDrawValueAboveBar(false);
        chartDay.setMaxVisibleValueCount(60);
        chartDay.setPinchZoom(false);
        chartDay.setScaleEnabled(false);
        chartDay.getDescription().setEnabled(false);
        chartDay.setBackgroundColor(Color.TRANSPARENT);
        chartDay.setDrawGridBackground(false);
        chartDay.getLegend().setEnabled(false);
        chartDay.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                isDayChartSelected = !isDayChartSelected;
                if (isDayChartSelected) {
                    chartDay.getData().setValueTextSize(6f);
                } else {
                    chartDay.getData().setValueTextSize(0f);
                }
                chartDay.setDrawValueAboveBar(isDayChartSelected);
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

        });

        ArrayList<BarEntry> dayEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>(DateHandler.hoursReadable);

        for (int i = 0; i < DateHandler.hours.size(); i++) { //add day data from ShowerDataOrganizer to dayEntries array
            Float showerLength = graphDataOrganizer.getDayMap().get(DateHandler.hours.get(i)).floatValue();
            BarEntry entry = new BarEntry(i, showerLength);
            dayEntries.add(entry);
        }

        BarDataSet barDataSet = new BarDataSet(dayEntries, "Minutes Spent Showering");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(mainActivity, R.color.colorPrimaryDark));
        barDataSet.setColors(colors);

        BarData data = new BarData(barDataSet);
        data.setHighlightEnabled(false);
        data.setValueTextColor(colors.get(0));
        data.setValueTextSize(0f);
        data.setBarWidth(.75f);

        chartDay.setData(data);

        chartDay.getAxisRight().setEnabled(false);
        chartDay.getAxisLeft().setAxisMinimum(0f);
        XAxis xAxis = chartDay.getXAxis();
        xAxis.setLabelCount(8);
        xAxis.setValueFormatter(new XAxisFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        if (mainActivity.isDarkMode()) {
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartDay.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartDay.getAxisLeft().setSpaceBottom(0);
            chartDay.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));

        }
    }

    public void initWeekChart() {
        chartWeek.setDrawBarShadow(false);
        chartWeek.setDrawValueAboveBar(false);
        chartWeek.setMaxVisibleValueCount(60);
        chartWeek.setPinchZoom(false);
        chartWeek.setScaleEnabled(false);
        chartWeek.getDescription().setEnabled(false);
        chartWeek.setBackgroundColor(Color.TRANSPARENT);
        chartWeek.setDrawGridBackground(false);
        chartWeek.getLegend().setEnabled(false);
        chartWeek.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                isWeekChartSelected = !isWeekChartSelected;
                if (isWeekChartSelected) {
                    chartWeek.getData().setValueTextSize(10f);
                } else {
                    chartWeek.getData().setValueTextSize(0f);
                }
                chartWeek.setDrawValueAboveBar(isWeekChartSelected);
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }


        });

        ArrayList<BarEntry> weekEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>(DateHandler.dayNames);

        for (int i = 0; i < DateHandler.dayNames.size(); i++) { //add week data from ShowerDataOrganizer to weekEntries array
            Float showerLength = graphDataOrganizer.getWeekMap().get(labels.get(i)).floatValue();
            BarEntry entry = new BarEntry(i, showerLength);
            weekEntries.add(entry);
        }
        
        BarDataSet barDataSet = new BarDataSet(weekEntries, "Minutes Spent Showering");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(mainActivity, R.color.colorPrimaryDark));
        barDataSet.setColors(colors);
        
        BarData data = new BarData(barDataSet);
        data.setHighlightEnabled(false);
        data.setValueTextColor(colors.get(0));
        data.setValueTextSize(0f);
        data.setBarWidth(.9f);
        
        chartWeek.setData(data);

        chartWeek.getAxisRight().setEnabled(false);
        chartWeek.getAxisLeft().setAxisMinimum(0f);
        XAxis xAxis = chartWeek.getXAxis();
        xAxis.setValueFormatter(new XAxisFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        if (mainActivity.isDarkMode()) {
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartWeek.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartWeek.getAxisLeft().setSpaceBottom(0);
            chartWeek.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));

        }

    }

    public void initMonthChart() {
        chartMonth.setDrawBarShadow(false);
        chartMonth.setDrawValueAboveBar(false);
        chartMonth.setMaxVisibleValueCount(60);
        chartMonth.setPinchZoom(false);
        chartMonth.setScaleEnabled(false);
        chartMonth.setDrawGridBackground(true);
        chartMonth.getDescription().setEnabled(false);
        chartMonth.setBackgroundColor(Color.TRANSPARENT);
        chartMonth.setDrawGridBackground(false);
        chartMonth.getLegend().setEnabled(false);
        textViewMonth.setText("Shower Length - " + DateHandler.currentMonthString + " Trend");
        chartMonth.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
//                isMonthChartSelected = !isMonthChartSelected;
//                if (isMonthChartSelected) {
//                    chartMonth.getData().setValueTextSize(6f);
//                } else {
//                    chartMonth.getData().setValueTextSize(0f);
//                }
//                chartMonth.setDrawValueAboveBar(isMonthChartSelected);
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

        });

        ArrayList<BarEntry> monthEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 1; i <= DateHandler.daysInMonth; i++) { //add month data from ShowerDataOrganizer to monthEntries array
            Float showerLength = graphDataOrganizer.getMonthMap().get(DateHandler.currentMonthCode + "/" + i).floatValue();
            labels.add(DateHandler.currentMonthCode + "/" + i);
            BarEntry entry = new BarEntry(i, showerLength);
            monthEntries.add(entry);
        }

        BarDataSet barDataSet = new BarDataSet(monthEntries, "Minutes Spent Showering");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(mainActivity, R.color.colorPrimaryDark));
        barDataSet.setColors(colors);

        BarData data = new BarData(barDataSet);
        data.setHighlightEnabled(false);
        data.setValueTextColor(colors.get(0));
        data.setValueTextSize(0f);
        data.setBarWidth(.75f);

        chartMonth.setData(data);

        chartMonth.getAxisRight().setEnabled(false);
        chartMonth.getAxisLeft().setAxisMinimum(0f);
        XAxis xAxis = chartMonth.getXAxis();
        xAxis.setValueFormatter(new XAxisFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        if (mainActivity.isDarkMode()) {
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartMonth.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartMonth.getAxisLeft().setSpaceBottom(0);
            chartMonth.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));

        }
    }

    public void initYearChart() {
        textViewYear.setText("Avg. Shower Length - " + DateHandler.currentYear + " Trend");
        chartYear.setDrawBarShadow(false);
        chartYear.setDrawValueAboveBar(false);
        chartYear.setMaxVisibleValueCount(60);
        chartYear.setPinchZoom(false);
        chartYear.setScaleEnabled(false);
        chartYear.getDescription().setEnabled(false);
        chartYear.setBackgroundColor(Color.TRANSPARENT);
        chartYear.setDrawGridBackground(false);
        chartYear.getLegend().setEnabled(false);
        chartYear.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                isYearChartSelected = !isYearChartSelected;
                if (isYearChartSelected) {
                    chartYear.getData().setValueTextSize(10f);
                } else {
                    chartYear.getData().setValueTextSize(0f);
                }
                chartYear.setDrawValueAboveBar(isYearChartSelected);
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }


        });

        ArrayList<BarEntry> yearEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>(DateHandler.monthNamesReadable);

        for (int i = 0; i < DateHandler.monthNames.size(); i++) { //get data from year map and add to graph
            Float showerLength = graphDataOrganizer.getYearMap().get(DateHandler.monthNames.get(i)).floatValue();
            BarEntry entry = new BarEntry(i, showerLength);
            yearEntries.add(entry);
        }

        BarDataSet barDataSet = new BarDataSet(yearEntries, "Average Minutes Spent Showering per Month");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(mainActivity, R.color.colorPrimaryDark));
        barDataSet.setColors(colors);

        BarData data = new BarData(barDataSet);
        data.setHighlightEnabled(false);
        data.setValueTextColor(colors.get(0));
        data.setValueTextSize(0f);
        data.setBarWidth(.65f);


        chartYear.setData(data);

        chartYear.getAxisRight().setEnabled(false);
        chartYear.getAxisLeft().setAxisMinimum(0f);
        XAxis xAxis = chartYear.getXAxis();
        xAxis.setLabelCount(12);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new XAxisFormatter(labels));

        if (mainActivity.isDarkMode()) {
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartYear.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartYear.getAxisLeft().setSpaceBottom(0);
            chartYear.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
        }
    }

    public float getAverage(ArrayList<Double> arrayList) {
        double sum = 0;
        for (double num : arrayList) {
            sum += num;
        }
        return (float) (sum / arrayList.size());
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

