package com.balanstudios.showerly;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RecoverySystem;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private MainActivity mainActivity;

    private Handler handler = new Handler();

    private RecyclerView recyclerViewHistory;
    private RecyclerView.Adapter adapterHistory;
    private RecyclerView.LayoutManager layoutManagerHistory;

    private ImageButton buttonBack;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history, container, false);

        mainActivity = (MainActivity) getActivity();

        new Thread(new HistoryRecyclerRunnable(v)).start();

        buttonBack = v.findViewById(R.id.buttonBack); buttonBack.setOnClickListener(onClickListener);

        if (mainActivity.getMainNavBar().getVisibility() == View.VISIBLE) {
            mainActivity.getMainNavBar().startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_down));
        }
        mainActivity.getMainNavBar().setVisibility(View.GONE);

        return v;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.buttonBack:
                    mainActivity.onBackPressed();
            }
        }
    };

    public static ArrayList<Shower> reverseArrayList(ArrayList<Shower> arrayList){
        ArrayList<Shower> reversed = new ArrayList<>();
        for (int i = arrayList.size() - 1; i >= 0; i--){
            reversed.add(arrayList.get(i));
        }
        return reversed;
    }

    class HistoryRecyclerRunnable implements Runnable{

        View v;

        HistoryRecyclerRunnable(View v) {
            this.v = v;
        }

        @Override
        public void run() {
            //reverse so that can be displayed in reverse chronological order
            adapterHistory = new HistoryAdapter(HistoryFragment.reverseArrayList(mainActivity.getUserShowers()));
            handler.post(new Runnable() { //handler necessary otherwise will crash
                @Override
                public void run() {
                    try {

                        recyclerViewHistory = v.findViewById(R.id.recyclerViewHistory);

                        recyclerViewHistory.setHasFixedSize(false);
                        layoutManagerHistory = new LinearLayoutManager(getActivity());


                        recyclerViewHistory.setLayoutManager(layoutManagerHistory);
                        recyclerViewHistory.setAdapter(adapterHistory);

                        recyclerViewHistory.addItemDecoration(new SpaceItemDecoration(16));

                        recyclerViewHistory.setVisibility(View.VISIBLE);
                        recyclerViewHistory.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in));
                    }
                    catch (Exception e) {
                        Log.d("DEBUG", e.getMessage());
                    }
                }
            });

        }
    }


}

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private ArrayList<Shower> userShowers;
    private DecimalFormat formatVolume = new DecimalFormat("0.0");
    private DecimalFormat formatCost = new DecimalFormat("0.00");

    HistoryAdapter(ArrayList<Shower> userShowers){
        this.userShowers = userShowers;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTime;
        public TextView textViewLength;
        public TextView textViewVolume;
        public TextView textViewCost;
        public TextView textViewGoalMet;


        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewLength = itemView.findViewById(R.id.textViewLength);
            textViewVolume = itemView.findViewById(R.id.textViewVolume);
            textViewCost = itemView.findViewById(R.id.textViewCost);
            textViewGoalMet = itemView.findViewById(R.id.textViewGoalMet);
        }
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_shower, viewGroup, false);
        HistoryViewHolder viewHolder = new HistoryViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder historyViewHolder, int i) {
        Shower currentShower = userShowers.get(i);

        historyViewHolder.textViewTime.setText(currentShower.getDate() + " at " + currentShower.getTime());
        historyViewHolder.textViewLength.setText("Length: " + TimeFormat.valueToString((int)(currentShower.getShowerLengthMillis() / 1000)));
        historyViewHolder.textViewVolume.setText("Gallons consumed: " + formatVolume.format(currentShower.getVolume()));
        historyViewHolder.textViewCost.setText("Cost: $" + formatCost.format(currentShower.getCost()));
        if (currentShower.isGoalMet()){
            historyViewHolder.textViewGoalMet.setText("Goal Met: Yes");
        }
        else {
            historyViewHolder.textViewGoalMet.setText("Goal Met: No");
        }

    }


    @Override
    public int getItemCount() {
        return userShowers.size();
    }
}

