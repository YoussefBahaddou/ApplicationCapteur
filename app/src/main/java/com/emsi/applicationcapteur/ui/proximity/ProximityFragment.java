package com.emsi.applicationcapteur.ui.proximity;

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

/**
 * A simple {@link Fragment} subclass for displaying proximity sensor data.
 */
public class ProximityFragment extends Fragment implements SensorEventListener {
    private LineChart chart;
    private TextView proximityValueText;
    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    static ArrayList<Entry> entries = new ArrayList<>();
    private boolean useSimulatedData = false;
    private Handler simulationHandler = new Handler();
    private Runnable simulationRunnable;
    private Random random = new Random();
    private float maxRange = 10.0f; // Default max range for simulation

    public ProximityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        
        if(mProximitySensor == null){
            Toast.makeText(getContext(), "Using simulated proximity data", Toast.LENGTH_LONG).show();
            useSimulatedData = true;
        } else {
            // Get the maximum range of the proximity sensor for proper scaling
            maxRange = mProximitySensor.getMaximumRange();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_proximity, container, false);
        chart = root.findViewById(R.id.chart);
        proximityValueText = root.findViewById(R.id.proximity_value);
        
        if (proximityValueText == null) {
            // If the layout doesn't have the TextView, create one
            proximityValueText = new TextView(getContext());
            proximityValueText.setTextSize(24);
            proximityValueText.setPadding(20, 20, 20, 20);
            proximityValueText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            
            // Add the TextView to the layout
            if (root instanceof ViewGroup) {
                ((ViewGroup) root).addView(proximityValueText, 0);
            }
        }
        
        // Configure chart
        setupChart();
        
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        leftAxis.setAxisMaximum(maxRange); // Use the sensor's max range
        
        chart.getAxisRight().setEnabled(false);
        
        // Set empty data initially
        LineData data = new LineData();
        chart.setData(data);
    }

    private void startSimulation() {
        simulationRunnable = new Runnable() {
            @Override
            public void run() {
                // Simulate proximity readings - randomly switch between near (0) and far (max range)
                // with occasional intermediate values
                float value;
                int randomState = random.nextInt(10);
                
                if (randomState < 3) {
                    // Near state (30% chance)
                    value = 0f;
                } else if (randomState < 8) {
                    // Far state (50% chance)
                    value = maxRange;
                } else {
                    // Intermediate state (20% chance)
                    value = random.nextFloat() * maxRange;
                }
                
                addEntry(value);
                
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

    private void addEntry(float proximityValue) {
        // Update text view with current proximity value
        String displayText;
        if (proximityValue < 1.0f) {
            displayText = "NEAR";
        } else {
            displayText = String.format("%.1f cm", proximityValue);
        }
        proximityValueText.setText(displayText);
        
        // Add entry to chart
        entries.add(new Entry(entries.size(), proximityValue));

        LineDataSet dataSet;
        if (chart.getData() != null && 
            chart.getData().getDataSetCount() > 0) {
            dataSet = (LineDataSet) chart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            chart.getData().notifyDataChanged();
        } else {
            dataSet = new LineDataSet(entries, "Proximity (cm)");
            dataSet.setDrawCircles(false);
            dataSet.setColor(getResources().getColor(android.R.color.holo_purple));
            dataSet.setLineWidth(2f);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.STEPPED); // Stepped mode for proximity
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getResources().getColor(android.R.color.holo_purple));
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
        } else if (mProximitySensor != null) {
            mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            addEntry(event.values[0]);
        }
    }
}
