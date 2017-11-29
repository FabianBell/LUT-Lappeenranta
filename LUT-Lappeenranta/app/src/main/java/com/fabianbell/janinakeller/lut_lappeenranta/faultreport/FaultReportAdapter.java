package com.fabianbell.janinakeller.lut_lappeenranta.faultreport;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.fabianbell.janinakeller.lut_lappeenranta.R;

import java.util.List;

/**
 * Created by Fabian on 27.11.2017.
 */

public class FaultReportAdapter extends ArrayAdapter<FaultReport> {

    Activity context;
    List<FaultReport> items;

    @SuppressWarnings("unchecked")
    public FaultReportAdapter(final Activity context, final List<FaultReport> result) {
        super(context, R.layout.faultreport, result);
        this.context = context;
        this.items = result;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = this.context.getLayoutInflater().inflate(R.layout.faultreport, null);
        final FaultReport item = this.items.get(position);
        ((TextView) view.findViewById(R.id.brokenParts)).setText(item.getBrokenParts());
        ((TextView) view.findViewById(R.id.lifetime)).setText(item.getLifetime());
        ((TextView) view.findViewById(R.id.reason)).setText(item.getReason());
        if (item.getGuarantee().equals("true")){
            ((TextView) view.findViewById(R.id.guarentee)).setText("Yes");
        }else{
            ((TextView) view.findViewById(R.id.guarentee)).setText("Yes");
        }
        return view;
    }
}
