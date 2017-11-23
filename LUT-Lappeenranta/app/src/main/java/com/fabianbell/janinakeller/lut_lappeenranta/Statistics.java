package com.fabianbell.janinakeller.lut_lappeenranta;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleValueListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Map;

public class Statistics extends AppCompatActivity {

    //Firebase
    Firebase mRootRef;
    FirebaseAuth mAuth;

    BarChart mStatisticsBarChart;
    ArrayList<BarEntry> barEntries;

    //data
    ArrayList<String> modelData;

    //statistic
    Map<String, String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();

        String base = "MODEL";
        modelData = new ArrayList<>();
        for(int i = 1; getIntent().getStringExtra(base + i) != null; i++){
            String model = getIntent().getStringExtra(base + 1);
            modelData.add(model);
        }

        barEntries = new ArrayList<>();

        for (String modelId : modelData){
            mRootRef.child("Statistics").child("Models").child(modelId).addListenerForSingleValueEvent(new SimpleValueListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    data.put(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
                    
                }
            });
        }

        mStatisticsBarChart = findViewById(R.id.statisticBarChart);

        barEntries.add(new BarEntry(1, 0));
        BarDataSet barDataSet = new BarDataSet(barEntries, "years");

        //TODO Fabian modelle hinzufügen, die der User in den Graphiken sehen möchte


        BarData theData = new BarData();
        mStatisticsBarChart.setData (theData);

        mStatisticsBarChart.setTouchEnabled(true);
        mStatisticsBarChart.setDragEnabled(true);
        mStatisticsBarChart.setScaleEnabled(true);


    }
}
