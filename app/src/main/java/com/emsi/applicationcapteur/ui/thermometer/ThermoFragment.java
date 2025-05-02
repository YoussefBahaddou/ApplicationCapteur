package com.emsi.applicationcapteur.ui.thermometer;

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

public class ThermoFragment extends Fragment implements SensorEventListener {

    private LineChart chart;
    private TextView temperatureValueText;
    private SensorManager mSensorManager;
    private Sensor mTempSensor;
    static ArrayList<Entry> entries = new ArrayList<>();
    private boolean useSimulatedData = false;
    private Handler simulationHandler = new Handler();
    private Runnable simulationRunnable;
    private Random random = new Random();
    private float baseTemperature = 22.0f; // Base temperature around 22°C

    public ThermoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mTempSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if(mTempSensor == null){
            Toast.makeText(getContext(), "Using simulated temperature data", Toast.LENGTH_LONG).show();
            useSimulatedData = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_thermo, container, false);
        chart = root.findViewById(R.id.chart);
        temperatureValueText = root.findViewById(R.id.temperature_value);
        
        if (temperatureValueText == null) {
            // If the layout doesn't have the TextView, create one
            temperatureValueText = new TextView(getContext());
            temperatureValueText.setTextSize(24);
            temperatureValueText.setPadding(20, 20, 20, 20);
            temperatureValueText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            
            // Add the TextView to the layout
            if (root instanceof ViewGroup) {
                ((ViewGroup) root).addView(temperatureValueText, 0);
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
        leftAxis.setAxisMinimum(10f); // Reasonable minimum for room temperature
        leftAxis.setAxisMaximum(35f); // Reasonable maximum for room temperature
        
        chart.getAxisRight().setEnabled(false);
        
        // Set empty data initially
        LineData data = new LineData();
        chart.setData(data);
    }

    private void addEntry(float temperature) {
        // Update temperature display
        temperatureValueText.setText(String.format("%.1f°C", temperature));
        
        entries.add(new Entry(entries.size(), temperature));
        
        LineDataSet dataSet;
        if (chart.getData() != null && 
            chart.getData().getDataSetCount() > 0) {
            dataSet = (LineDataSet) chart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            chart.getData().notifyDataChanged();
        } else {
            dataSet = new LineDataSet(entries, "Temperature (°C)");
            dataSet.setDrawCircles(false);
            dataSet.setColor(getResources().getColor(android.R.color.holo_orange_dark));
            dataSet.setLineWidth(2f);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getResources().getColor(android.R.color.holo_orange_light));
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

    private void startSimulation() {
        simulationRunnable = new Runnable() {
            @Override
            public void run() {
                // Generate a random temperature fluctuation around the base temperature
                float randomTemp = baseTemperature + (random.nextFloat() * 2 - 1); // +/- 1 degree
                addEntry(randomTemp);
                
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

    @Override
    public void onResume() {
        super.onResume();
        entries.clear();
        
        if (useSimulatedData) {
            startSimulation();
        } else {
            mSensorManager.registerListener(this, mTempSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            addEntry(event.values[0]);
        }
    }
}
