package it.uniba.dib.sms232412.valigetta.medica;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MisuraPressioneFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorePressione;
    private MainActivity parentActivity;
    private TextView editMisuraRilevata;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_misura_pressione, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() == null || !(getActivity() instanceof MainActivity)) return;
        parentActivity = (MainActivity) getActivity();
        sensorManager = (SensorManager) parentActivity.getSystemService(Context.SENSOR_SERVICE);

        if(sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null){
            Toast.makeText(parentActivity, getString(R.string.pressione_sensore_non_trovato), Toast.LENGTH_SHORT).show();
            return;
        }
        sensorePressione = sensorManager.getSensorList(Sensor.TYPE_PRESSURE).get(0);
        editMisuraRilevata = view.findViewById(R.id.pressione_misura_rilevata);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double misura = event.values[0];
        parentActivity.misuraTemporanea = misura;
        editMisuraRilevata.setText(getString(R.string.pressione_misura_rilevata, misura));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorePressione, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}