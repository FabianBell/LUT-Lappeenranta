package com.fabianbell.janinakeller.lut_lappeenranta;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.fabianbell.janinakeller.lut_lappeenranta.listener.Callable;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableForFirebase;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableValueEventListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.Condition;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.Trigger;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics extends AppCompatActivity {

    //Firebase
    Firebase mRootRef;
    FirebaseAuth mAuth;

    //elements
    BarChart mStatisticsBarChart;
    TextView mModelsTextView;
    TextView mBrandsTextView;

    //lifetimeData
    ArrayList<String> modelData;

    //statistic
    Map<String, Integer> lifetimeData; //modelId - lifetime
    Map<String, String> modelIdMap; // modelId - modelName
    Map<String, String> brandModelMap; // modelId - brandName

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();

        mStatisticsBarChart = findViewById(R.id.statisticBarChart);
        mModelsTextView = findViewById(R.id.statisticsModelsTextView);
        mBrandsTextView = findViewById(R.id.statisticsBrandsTextView);

        mStatisticsBarChart.setTouchEnabled(true);
        mStatisticsBarChart.setDragEnabled(true);
        mStatisticsBarChart.setScaleEnabled(true);

        String modelBase = "MODEL";
        String brandBase = "BRAND";
        modelData = new ArrayList<>();
        brandModelMap = new HashMap<>();
        for(int i = 1; (getIntent().getStringExtra(modelBase + i) != null) && (getIntent().getStringExtra(brandBase + i) != null) ; i++){
            String modelId = getIntent().getStringExtra(modelBase + i);
            String brandName = getIntent().getStringExtra(brandBase + i);
            modelData.add(modelId);
            brandModelMap.put(modelId, brandName);
        }

        lifetimeData = new HashMap<>();
        modelIdMap = new HashMap<>();
        final Trigger<Map<String, String>> mapTrigger = new Trigger<>(new Condition<Map<String, String>>(modelIdMap) {
            @Override
            public boolean isTrue() {
                return (getObserver().size() == modelData.size());
            }
        }, new Callable<Map<String, String>>() {
            @Override
            public void call(Map<String, String> data) {
                ArrayList<String> models = new ArrayList<>();
                String model = "";
                ArrayList<String> brands = new ArrayList<>();
                String brand = "";
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                BarEntry barEntry;
                float i = 0.5f;
                for (Map.Entry<String, Integer> entry : lifetimeData.entrySet()) {
                    model = modelIdMap.get(entry.getKey());
                    brand = brandModelMap.get(entry.getKey());
                    barEntry = new BarEntry(i, entry.getValue(), model);
                    barEntries.add(barEntry);
                    if (!models.contains(model)) {
                        models.add(modelIdMap.get(entry.getKey()));
                    }
                    if(!brands.contains(brand)) {
                        brands.add(brandModelMap.get(entry.getKey()));
                    }
                    i++;
                }
                mModelsTextView.setText(models.toString().replace("[", "").replace("]", ""));
                mBrandsTextView.setText(brands.toString().replace("[", "").replace("]", ""));
                YAxis right = mStatisticsBarChart.getAxisRight();
                right.setEnabled(false);
                XAxis head = mStatisticsBarChart.getXAxis();
                head.setEnabled(false);
                IBarDataSet iBarDataSet = new BarDataSet(barEntries, "Models");
                iBarDataSet.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        return entry.getData().toString();
                    }
                });
                BarData barData = new BarData(iBarDataSet);
                mStatisticsBarChart.setData(barData);
                mStatisticsBarChart.callOnClick();
            }
        });
        for (String modelId : modelData){
            mRootRef.child("Statistics").child("Models").child(modelId).addListenerForSingleValueEvent(new CallableValueEventListener<>(modelId, new CallableForFirebase<String>() {
                @Override
                public void call(String param, DataSnapshot data) {
                    if(data.getValue() != null) {
                        int lifetime = Integer.parseInt(data.getValue().toString());
                        String id = data.getKey();
                        lifetimeData.put(id, lifetime);
                        mRootRef.child("Brand").child(brandModelMap.get(param)).child("Model").child(param).addListenerForSingleValueEvent(new CallableValueEventListener<>(param, new CallableForFirebase<String>() {
                            @Override
                            public void call(String param, DataSnapshot data) {
                                modelIdMap.put(param, data.getValue().toString());
                                mapTrigger.onChange();
                            }
                        }));
                    }else{
                        Toast.makeText(Statistics.this, " ", Toast.LENGTH_LONG).show();
                    }
                }
            }));
        }

        /*barEntries.add(new BarEntry(1, 0));
        BarDataSet barDataSet = new BarDataSet(barEntries, "years");*/

        //TODO Fabian modelle hinzufügen, die der User in den Graphiken sehen möchte


        BarData theData = new BarData();
        mStatisticsBarChart.setData (theData);



    }
}
