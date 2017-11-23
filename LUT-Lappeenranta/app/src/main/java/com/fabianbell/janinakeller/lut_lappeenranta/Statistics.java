package com.fabianbell.janinakeller.lut_lappeenranta;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class Statistics extends AppCompatActivity {

    BarChart mStatisticsBarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mStatisticsBarChart = (BarChart) findViewById(R.id.statisticBarChart);

        ArrayList<BarEntry> barEntries = new ArrayList<>();

        // TODO Fabian BarEntrys festlegen
        barEntries.add(new BarEntry(1,0));
        BarDataSet barDataSet = new BarDataSet(barEntries, "years");

        ArrayList<String> modelData = new ArrayList<>();
        modelData.add("iPhone");

        //TODO Fabian modelle hinzufügen, die der User in den Graphiken sehen möchte


        BarData theData = new BarData();
        mStatisticsBarChart.setData (theData);

        mStatisticsBarChart.setTouchEnabled(true);
        mStatisticsBarChart.setDragEnabled(true);
        mStatisticsBarChart.setScaleEnabled(true);


    }
}
