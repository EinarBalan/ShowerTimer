package com.balanstudios.showerly;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * To anyone who may be reading this code: I'm sorry. It's pure spaghetti. But hey, it works! That's all that matters, right? Right???
 */

public class ProfileCalendarFragment extends Fragment {

    private static int dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
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
    private TextView textViewMonth;
    private TextView textViewYear;
    private TextView textViewWeek;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    private String month;
    private String year;
    private String yearNum;
    private String monthNum;
    private int monthCode = Calendar.getInstance().get(Calendar.MONTH) + 1;
    private boolean isWeekChartSelected = false;
    private boolean isMonthChartSelected = false;
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
                case R.id.chartMonth:
                    isMonthChartSelected = !isMonthChartSelected;
                    if (isMonthChartSelected) {
                        chartMonth.getData().setValueTextSize(6f);
                    } else {
                        chartMonth.getData().setValueTextSize(0f);
                    }
                    chartMonth.setDrawValueAboveBar(isMonthChartSelected);
                    break;

            }
        }
    };
    private boolean isYearChartSelected = false;
    private ArrayList<ArrayList<Double>> months = new ArrayList<>();


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

        linearLayoutYear = v.findViewById(R.id.linearLayoutYear);
        cardViewYear = v.findViewById(R.id.cardViewYear);
        linearLayoutMonth = v.findViewById(R.id.linearLayoutMonth);
        cardViewMonth = v.findViewById(R.id.cardViewMonth);
        linearLayoutWeek = v.findViewById(R.id.linearLayoutWeek);
        cardViewWeek = v.findViewById(R.id.cardViewWeek);

        textViewNoShowerData = v.findViewById(R.id.textViewNoShowerData);
        textViewMonth = v.findViewById(R.id.textViewMonth);
        textViewYear = v.findViewById(R.id.textViewYear);
        textViewWeek = v.findViewById(R.id.textViewWeek);

        if (mainActivity.getUserShowers().size() > 0) {
            yearNum = mainActivity.getUserShowers().get(mainActivity.getUserShowers().size() - 1).getDate().substring(mainActivity.getUserShowers().get(0).getDate().length() - 2);
            monthNum = mainActivity.getUserShowers().get(mainActivity.getUserShowers().size() - 1).getDate().substring(0, 1);
        }

        if (mainActivity.getUserShowers().size() > 0) { //only show graph if there are recorded showers

            textViewNoShowerData.setVisibility(View.GONE);
            chartWeek = v.findViewById(R.id.chartWeek);
            chartWeek.setDrawBarShadow(false);
            chartWeek.setDrawValueAboveBar(false);
            chartWeek.setMaxVisibleValueCount(60);
            chartWeek.setPinchZoom(false);
            chartWeek.setScaleEnabled(false);
            chartWeek.setDrawGridBackground(true);
            chartWeek.getDescription().setEnabled(false);
//            chartWeek.setTouchEnabled(false);
//            chartWeek.setOnClickListener(onClickListener);
            initWeekChart();
        } else {
            linearLayoutWeek.setVisibility(View.GONE);
            cardViewWeek.setVisibility(View.GONE);
        }

        if (mainActivity.getUserShowers().size() > 7) { // only show month chart if it is significantly different from week chart

            //separate showers based on their months
            for (int i = 0; i < 12; i++) {
                months.add(new ArrayList<Double>());
            }

            for (int i = mainActivity.getUserShowers().size() - 1; i >= 0; i--) {
                Shower shower = mainActivity.getUserShowers().get(i);
                String currentYear = shower.getDate().substring(shower.getDate().length() - 2);
                String currentMonth = shower.getDate().substring(0, shower.getDate().indexOf("/"));

                if (currentYear.equals(yearNum)) {
                    months.get(Integer.parseInt(currentMonth) - 1).add(shower.getShowerLengthMinutes());
                } else {
                    break;
                }
            }

            chartMonth = v.findViewById(R.id.chartMonth);
            chartMonth.setDrawBarShadow(false);
            chartMonth.setDrawValueAboveBar(false);
            chartMonth.setMaxVisibleValueCount(60);
            chartMonth.setPinchZoom(false);
            chartMonth.setScaleEnabled(false);
            chartMonth.setDrawGridBackground(true);
            chartMonth.getDescription().setEnabled(false);
//            chartMonth.setTouchEnabled(false);
//            chartMonth.setOnClickListener(onClickListener);

            month = monthFormat.format(calendar.getTime());
            textViewMonth.setText(month + " - Monthly Trend");

            initMonthChart();
        } else {
            linearLayoutMonth.setVisibility(View.GONE);
            cardViewMonth.setVisibility(View.GONE);
        }

        if (mainActivity.getUserShowers().size() > 40) { // only show year chart if it is significantly different from month chart
            chartYear = v.findViewById(R.id.chartYear);
            chartYear.setDrawBarShadow(false);
            chartYear.setDrawValueAboveBar(false);
            chartYear.setMaxVisibleValueCount(12);
            chartYear.setPinchZoom(false);
            chartYear.setDrawGridBackground(true);
            chartYear.getDescription().setEnabled(false);
            chartYear.setTouchEnabled(false);
//            chartYear.setOnClickListener(onClickListener);

            year = yearFormat.format(calendar.getTime());
            textViewYear.setText(year + " - Yearly Trend");

            initYearChart();
        } else {
            linearLayoutYear.setVisibility(View.GONE);
            cardViewYear.setVisibility(View.GONE);
        }

        return v;
    }

    public void initWeekChart() {

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
        ArrayList<String> labels = new ArrayList<>();

        double[] days = new double[7];
        String[] dates = new String[7];
        Arrays.fill(days, 0);
        Arrays.fill(dates, "");


        for (Shower s : mainActivity.getUserShowers()) {
//            Log.d("D", "Shower Day: " + s.getDayOfMonth() + " Current Day: " + dayOfMonth);
            if (Math.abs(s.getDayOfMonth() - dayOfMonth) < 7 && s.getMonthNum() == monthCode) { //if shower is taken at most seven days from current day
                days[6 - Math.abs(s.getDayOfMonth() - dayOfMonth)] += s.getShowerLengthMinutes();
                dates[6 - Math.abs(s.getDayOfMonth() - dayOfMonth)] = s.getDate().substring(0, s.getDate().length() - 3);

            }
        }


        for (int i = 0; i < days.length; i++) {
            weekEntries.add(new BarEntry(i, (float) days[i]));
        }

        for (int i = 0; i < dates.length; i++) {
            if (dates[i].length() == 0) {
                dates[i] = monthCode + "/" + (dayOfMonth - 6 + i);
            }
            labels.add(dates[i]);
        }

        textViewWeek.setText(dates[0] + " to " + dates[6] + " - Weekly Trend");

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
        xAxis.setPosition(XAxis.XAxisPosition.TOP);

        if (mainActivity.isDarkMode()) {
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartWeek.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartWeek.getAxisLeft().setSpaceBottom(0);
            chartWeek.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartWeek.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorDarkBackground));
            chartWeek.setGridBackgroundColor(ContextCompat.getColor(mainActivity, R.color.headerTextColor));
        }

    }

    public void initMonthChart() {

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
                isMonthChartSelected = !isMonthChartSelected;
                if (isMonthChartSelected) {
                    chartMonth.getData().setValueTextSize(6f);
                } else {
                    chartMonth.getData().setValueTextSize(0f);
                }
                chartMonth.setDrawValueAboveBar(isMonthChartSelected);
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

        for (int i = 1; i < 31; i++) {
            try {
                monthEntries.add(new BarEntry(30 - i, (float) mainActivity.getUserShowers().get(mainActivity.getUserShowers().size() - i).getShowerLengthMinutes()));
                labels.add("");
            } catch (Exception e) {
                break;
            }
        }

//        ArrayList<Double> currentMonth = months.get(Integer.parseInt(monthNum) - 1);
//        for (double d: currentMonth){
//            monthEntries.add(new BarEntry(Float.parseFloat(monthNum) - 1, (float) d));
//        }


        BarDataSet barDataSet = new BarDataSet(monthEntries, "Minutes Spent Showering");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(mainActivity, R.color.colorPrimaryDark));
        barDataSet.setColors(colors);

        BarData data = new BarData(barDataSet);
        data.setHighlightEnabled(false);
        data.setValueTextColor(colors.get(0));
        data.setValueTextSize(0f);
        data.setBarWidth(.4f);


        chartMonth.setData(data);

        chartMonth.getAxisRight().setEnabled(false);
        chartMonth.getAxisLeft().setAxisMinimum(0f);
        XAxis xAxis = chartMonth.getXAxis();
        xAxis.setValueFormatter(new XAxisFormatterNone());

        if (mainActivity.isDarkMode()) {
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartMonth.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartMonth.getAxisLeft().setSpaceBottom(0);
            chartMonth.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartMonth.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorDarkBackground));
            chartMonth.setGridBackgroundColor(ContextCompat.getColor(mainActivity, R.color.headerTextColor));
        }
    }

    public void initYearChart() {

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
                    chartYear.getData().setValueTextSize(6f);
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
        ArrayList<String> labels = new ArrayList<>();


        for (int i = 1; i <= months.size(); i++) {
            if (months.get(i - 1).size() > 0) {
                yearEntries.add(new BarEntry(i, getAverage(months.get(i - 1)))); //add the average of each month to the graph, up to 12 months
            }
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
        xAxis.setValueFormatter(new XAxisFormatterNone());

        if (mainActivity.isDarkMode()) {
            xAxis.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartYear.getAxisLeft().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartYear.getAxisLeft().setSpaceBottom(0);
            chartYear.getLegend().setTextColor(ContextCompat.getColor(mainActivity, R.color.colorDarkText));
            chartYear.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorDarkBackground));
            chartYear.setGridBackgroundColor(ContextCompat.getColor(mainActivity, R.color.headerTextColor));
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

