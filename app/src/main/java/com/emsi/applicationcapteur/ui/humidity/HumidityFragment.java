package com.emsi.applicationcapteur.ui.humidity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Random;

import com.emsi.applicationcapteur.R;

public class HumidityFragment extends Fragment implements SensorEventListener {
    private LineChart chart;
    private TextView humidityValueText;
    private SensorManager mSensorManager;
    private Sensor mHumidSensor;
    static ArrayList<Entry> entries = new ArrayList<>();
    private boolean useSimulatedData = false;
    private Handler simulationHandler = new Handler();
    private Runnable simulationRunnable;
    private Random random = new Random();
    private float baseHumidity = 50.0f; // Base humidity around 50%

    public HumidityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mHumidSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if(mHumidSensor == null){
            Toast.makeText(getContext(), "Using simulated humidity data", Toast.LENGTH_LONG).show();
            useSimulatedData = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_humidity, container, false);
        chart = root.findViewById(R.id.chart);
        humidityValueText = root.findViewById(R.id.humidity_value);
        
        if (humidityValueText == null) {
            // If the layout doesn't have the TextView, create one
            humidityValueText = new TextView(getContext());
            humidityValueText.setTextSize(24);
            humidityValueText.setPadding(20, 20, 20, 20);
            humidityValueText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            
            // Add the TextView to the layout
            if (root instanceof ViewGroup) {
                ((ViewGroup) root).addView(humidityValueText, 0);
            }
        }
        
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
        leftAxis.setAxisMaximum(100f); // Humidity is 0-100%
        
        chart.getAxisRight().setEnabled(false);
        
        // Set empty data initially
        LineData data = new LineData();
        chart.setData(data);
    }

    private void startSimulation() {
        simulationRunnable = new Runnable() {
            @Override
            public void run() {
                // Generate a random humidity fluctuation around the base humidity
                float randomHumidity = baseHumidity + (random.nextFloat() * 10 - 5); // +/- 5%
                // Keep humidity between 0-100%
                randomHumidity = Math.max(0, Math.min(100, randomHumidity));
                addEntry(randomHumidity);
                
                // Schedule the next update
                simulationHandler.postDelayed(this, 1000); // Update every second
            }
        };
        
        // Start the simulation
        simulationHandler.post(simulationRunnable);
    }

    private void stopSimulation() {
        if (simulationRunnable != null) {
            simulationHandler.removeCallbacks(simulationRunnable);
        }
    }

    private void addEntry(float humidityValue) {
        // Update text view with current humidity value
        humidityValueText.setText(String.format("%.1f%%", humidityValue));
        
        // Add entry to chart
        entries.add(new Entry(entries.size(), humidityValue));

        LineDataSet dataSet;
        if (chart.getData() != null && 
            chart.getData().getDataSetCount() > 0) {
            dataSet = (LineDataSet) chart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            chart.getData().notifyDataChanged();
        } else {
            dataSet = new LineDataSet(entries, "Humidity (%)");
            dataSet.setDrawCircles(false);
            dataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
            dataSet.setLineWidth(2f);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getResources().getColor(android.R.color.holo_blue_light));
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
        entries.clear();
        
        if (useSimulatedData) {
            startSimulation();
        } else if (mHumidSensor != null) {
            mSensorManager.registerListener(this, mHumidSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (useSimulatedData) {
            stopSimulation();
        } else {
            mSensorManager.unregisterListener(this);
        }
        entries.clear();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            addEntry(event.values[0]);
        }
    }
}
