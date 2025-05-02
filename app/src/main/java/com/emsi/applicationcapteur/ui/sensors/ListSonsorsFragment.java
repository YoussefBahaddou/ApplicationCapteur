package com.emsi.applicationcapteur.ui.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.DividerItemDecoration;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.emsi.applicationcapteur.R;
import com.emsi.applicationcapteur.beans.SensorItem;

/**
 * A fragment representing a list of Sensors available on the device.
 */
public class ListSonsorsFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListSonsorsFragment() {
    }

    /**
     * Create a new instance of the fragment with specified column count.
     */
    public static ListSonsorsFragment newInstance(int columnCount) {
        ListSonsorsFragment fragment = new ListSonsorsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    /**
     * Load all available sensors on the device.
     */
    List<SensorItem> loadSensor() {
        List<SensorItem> sensors = new ArrayList<>();

        SensorManager sensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sListe = sensorManager.getSensorList(Sensor.TYPE_ALL);
        int id = 0;
        for(Sensor s : sListe){
            id++;
            sensors.add(new SensorItem(id+"", s.getName(), sensorTypeToString(s.getType()), s.getVendor(), s.getVersion()+""));
        }
        return sensors;
    }

    /**
     * Convert sensor type integer to readable string.
     */
    public static String sensorTypeToString(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_TEMPERATURE:
                return "Ambient Temperature";
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                return "Game Rotation Vector";
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                return "Geomagnetic Rotation Vector";
            case Sensor.TYPE_GRAVITY:
                return "Gravity";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                return "Gyroscope Uncalibrated";
            case Sensor.TYPE_HEART_RATE:
                return "Heart Rate Monitor";
            case Sensor.TYPE_LIGHT:
                return "Light";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Linear Acceleration";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetic Field";
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                return "Magnetic Field Uncalibrated";
            case Sensor.TYPE_ORIENTATION:
                return "Orientation";
            case Sensor.TYPE_PRESSURE:
                return "Pressure";
            case Sensor.TYPE_PROXIMITY:
                return "Proximity";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Relative Humidity";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Rotation Vector";
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                return "Significant Motion";
            case Sensor.TYPE_STEP_COUNTER:
                return "Step Counter";
            case Sensor.TYPE_STEP_DETECTOR:
                return "Step Detector";
            default:
                return "Unknown";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listsonsors_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            
            // Add dividers between items for better visual separation
            recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
            
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            
            // Load the sensors and set the adapter
            List<SensorItem> sensors = loadSensor();
            recyclerView.setAdapter(new ListSonsorsFragmentRecyclerViewAdapter(sensors, mListener));
            
            // Set a title for the screen
            if (getActivity() != null) {
                getActivity().setTitle(getString(R.string.menu_sensors) + " (" + sensors.size() + ")");
            }
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            // If the activity doesn't implement the interface, we'll just proceed without interaction
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Interface for handling item clicks in the sensor list.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(SensorItem item);
    }
}
