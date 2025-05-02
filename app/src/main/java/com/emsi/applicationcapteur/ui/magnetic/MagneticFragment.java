package com.emsi.applicationcapteur.ui.magnetic;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import com.emsi.applicationcapteur.R;

/**
 * A simple {@link Fragment} subclass for displaying magnetic field sensor data.
 */
public class MagneticFragment extends Fragment implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    public static DecimalFormat DECIMAL_FORMATTER;
    static ArrayList<Entry> entries = new ArrayList<>();
    private LineChart chart;
    private TextView value;

    public MagneticFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(mMagneticSensor == null){
            Toast.makeText(getContext(), R.string.message_neg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_magnetic, container, false);
        value = root.findViewById(R.id.value);
        chart = root.findViewById(R.id.chart);

        // Define decimal formatter
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DECIMAL_FORMATTER = new DecimalFormat("#.000", symbols);

        // Configure chart
        setupChart();

        return root;
    }

    private void setupChart() {
        // Customize chart appearance
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);

        // Enable touch gestures
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // Configure X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1f);

        // Configure Y axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);

        // Set empty data initially
        LineData data = new LineData();
        chart.setData(data);
    }

    private void addEntry(double value) {
        entries.add(new Entry(entries.size(), (float) value));

        LineDataSet dataSet;
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            dataSet = (LineDataSet) chart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            chart.getData().notifyDataChanged();
        } else {
            dataSet = new LineDataSet(entries, "Magnetic Field (μT)");
            dataSet.setDrawCircles(false);
            dataSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
            dataSet.setLineWidth(2f);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getResources().getColor(android.R.color.holo_red_light));
            dataSet.setFillAlpha(50);

            LineData data = new LineData(dataSet);
            chart.setData(data);
        }

        // Limit data points to prevent memory issues
        if (entries.size() > 100) {
            entries.remove(0);
            // Shift x values
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).setX(i);
            }
        }

        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(50); // Show 50 values at a time
        chart.moveViewToX(entries.size());
        chart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMagneticSensor != null) {
            mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        entries.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        entries.clear();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float magX = event.values[0];
            float magY = event.values[1];
            float magZ = event.values[2];
            double magnitude = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));

            // Set value on the screen
            value.setText(DECIMAL_FORMATTER.format(magnitude) + " \u00B5Tesla");

            // Update chart
            addEntry(magnitude);
        }
    }
}
