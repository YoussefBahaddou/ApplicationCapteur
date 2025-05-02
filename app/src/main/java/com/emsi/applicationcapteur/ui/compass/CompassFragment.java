package com.emsi.applicationcapteur.ui.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.emsi.applicationcapteur.R;

public class CompassFragment extends Fragment implements SensorEventListener {

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    private Sensor mCompassSensor;

    TextView tvHeading;

    public CompassFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_compass, container, false);

        image = root.findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading
        tvHeading = root.findViewById(R.id.tvHeading);

        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mCompassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (mCompassSensor == null) {
            Toast.makeText(getContext(), R.string.message_neg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCompassSensor != null) {
            mSensorManager.registerListener(this, mCompassSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}
