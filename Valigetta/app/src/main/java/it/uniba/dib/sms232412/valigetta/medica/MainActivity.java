package it.uniba.dib.sms232412.valigetta.medica;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private AcceptThread myServerThread;
    private String sensoreRichiesto;
    private TextView viewSensoreRichiesto;

    /**
     * Questa è la misura definitiva della misurazione
     * Una volta assegnata, viene passata ad AsilApp e la comunicazione BT viene interrotta
     */
    public Double misuraEffettuata = null;

    /**
     * Questa è la misura temporanea della misurazione
     * Questa può variare fintanto che si usa il sensore richiesto da AsilApp
     */
    protected Double misuraTemporanea = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewSensoreRichiesto = findViewById(R.id.sensore_rilevato);

        // Bottone per resettare la valigetta
        Button resetButton = findViewById(R.id.btn_reset);
        resetButton.setOnClickListener(v -> resetValigetta());

        // Bottone per avviare la comunicazione BT con AsilApp
        Button btn_BT = findViewById(R.id.btn_bt_valigia);
        btn_BT.setOnClickListener(v -> {
            // Verifico che il bluetooth sia abilitato sul device
            if(btAdapter == null){
                Toast.makeText(this, getString(R.string.valigia_bt_non_abilitato), Toast.LENGTH_SHORT).show();
                return;
            }
            // Gestione del permesso per usare il bluetooth
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.BLUETOOTH_SCAN)){
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.valigia_bt_rationale_title)
                            .setMessage(R.string.valigia_bt_rationale_msg)
                            .setPositiveButton(R.string.ok, (dialog, which) ->
                                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN,
                                            android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_ADVERTISE,
                                            android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN},1))
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_ADVERTISE,
                            android.Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},1);
                }
            } else {
                startBTFunctionPT1();
            }
        });

        // Bottone per inviare la misura fatta con il sensore richiesto
        Button btn_Send = findViewById(R.id.prova_misura_fatta);
        btn_Send.setOnClickListener(v -> {
            if(misuraTemporanea != null){
                misuraEffettuata = misuraTemporanea;
                Toast.makeText(this, getString(R.string.misura_inviata), Toast.LENGTH_SHORT).show();
                resetValigetta();
            } else {
                Toast.makeText(this, getString(R.string.misura_non_ancora_fatta), Toast.LENGTH_SHORT).show();
            }
        });

        // Bottone per aprire il sensore richiesto
        Button btnOpen = findViewById(R.id.btn_apri_sensore);
        btnOpen.setOnClickListener(v -> {
            if(sensoreRichiesto != null){
                viewSensoreRichiesto.setText(getResources().getString(R.string.valigia_sensore_rilevato, sensoreRichiesto));
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                switch(sensoreRichiesto){
                    case "TEMPERATURA":
                        ft.replace(R.id.frame_sensore, new MisuraTemperaturaFragment());
                        break;
                    case "PRESSIONE":
                        ft.replace(R.id.frame_sensore, new MisuraPressioneFragment());
                        break;
                    case "BATTITI":
                        ft.replace(R.id.frame_sensore, new MisuraBattitiFragment());
                        break;
                }
                ft.commit();
                btn_Send.setVisibility(View.VISIBLE);
                btnOpen.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, getString(R.string.valigia_sensore_non_rilevato), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBTFunctionPT1();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            startBTFunctionPT2();
            return;
        }
        if (requestCode == 2 && (resultCode == RESULT_OK || resultCode == 300)) {
            startBTFunctionPT3();
        }
    }

    private void startBTFunctionPT1(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Verifico che il bluetooth sia attivato sul device
        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        } else {
            startBTFunctionPT2();
        }
    }

    private void startBTFunctionPT2(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Attivo la visibilità per gli altri device
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, 2);
    }

    private void startBTFunctionPT3(){
        myServerThread = new AcceptThread(this);
        myServerThread.start();
        Toast.makeText(this, getString(R.string.valigia_visibilita_avviata), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myServerThread != null){
            myServerThread.cancel();
        }
    }

    public void setSensoreRichiesto(String sensore){
        this.sensoreRichiesto = sensore;
    }

    private void resetValigetta(){
        Intent resetIntent = new Intent(this, MainActivity.class);
        startActivity(resetIntent);
        finish();
    }
}