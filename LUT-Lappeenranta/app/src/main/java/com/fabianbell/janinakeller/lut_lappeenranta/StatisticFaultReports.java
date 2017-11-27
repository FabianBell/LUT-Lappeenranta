package com.fabianbell.janinakeller.lut_lappeenranta;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.fabianbell.janinakeller.lut_lappeenranta.Utils.*;
import com.fabianbell.janinakeller.lut_lappeenranta.faultreport.*;
import com.fabianbell.janinakeller.lut_lappeenranta.faultreport.FaultReport;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleValueListener;
import com.firebase.client.DataSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class StatisticFaultReports extends AppCompatActivity {

    private ArrayList<FaultReport> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_fault_reports);

        String modelId = getIntent().getStringExtra("MODEL");

        ListView mReportList = findViewById(R.id.reportList);

        data = new ArrayList<>();
        final FaultReportAdapter faultReportAdapter = new FaultReportAdapter(StatisticFaultReports.this, data);

        mReportList.setAdapter(faultReportAdapter);

        Utils.mRootRef.child("FaultReport").child(modelId).addListenerForSingleValueEvent(new SimpleValueListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Map<String, String> report = snapshot.getValue(Map.class);
                    String reportId = snapshot.getKey();
                    com.fabianbell.janinakeller.lut_lappeenranta.faultreport.FaultReport faultReport = new FaultReport(reportId, report.get("BrokenParts"), report.get("Lifetime"), report.get("Reason"), report.get("guarantee"));
                    data.add(faultReport);
                    faultReportAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
