package it.uniba.dib.sms232412.valigettaMedica;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.utils.Utente;

public class ValigettaActivity extends AppCompatActivity {

    private Utente utenteSelezionato;
    private int actualStep = 1;
    private String malattieUtente, sensore;

    // Attributi utili per la gestione del collegamento BT con la valigetta
    private ValigettaStep3ComunicaConValigettaFragment btFragment;
    final private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private List<String> nomiDispositiviTrovati = new ArrayList<>();
    private List<BluetoothDevice> dispositiviTrovati = new ArrayList<>();
    final private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String nameDevice = device.getName() + "\n" + device.getAddress();
                    if(!nomiDispositiviTrovati.contains(nameDevice)){
                        nomiDispositiviTrovati.add(nameDevice);
                        dispositiviTrovati.add(device);
                        btFragment.updateListaDeviceTrovati(nomiDispositiviTrovati);
                    }
                }
            }
        }
    };
    private boolean receiverRegistrato = false;
    public Double misuraRilevata; // MISURA OTTENUTA DALLA VALIGETTA MEDICA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valigetta);

        FragmentManager fragManager = getSupportFragmentManager();

        ActionBar myBar = getSupportActionBar();
        if (myBar != null) {
            myBar.setTitle(getString(R.string.valigia_title));
            myBar.setDisplayShowHomeEnabled(true);
            myBar.setDisplayHomeAsUpEnabled(true);
            myBar.setHomeButtonEnabled(true);
            myBar.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_medical_logo));
            myBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.app_bar_color)));
        }

        //Ricavo i dati dell'utente dall'intent usato per chiamare questa activity
        //String userUid = getIntent().getStringExtra("UID");
        //String userRole = getIntent().getStringExtra("ROLE");

        FragmentTransaction ft = fragManager.beginTransaction();
        ft.replace(R.id.valigia_main_frame, new ValigettaStep1TrovaUtenteFragment());
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            switch (actualStep) {
                case 1:
                    finish();
                    break;
                case 2:
                    changeToStep(1);
                    break;
                case 3:
                    changeToStep(2);
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_enter_from_bottom, R.anim.anim_exit_to_top);
    }

    public void changeToStep(int step) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // A seconda della direzione dello step procedo avanti o indietro
        if (actualStep < step) {
            ft.setCustomAnimations(R.anim.anim_enter_from_right, R.anim.anim_exit_to_left);
        } else {
            ft.setCustomAnimations(R.anim.anim_enter_from_left, R.anim.anim_exit_to_right);
        }
        switch (step) {
            case 1:
                ft.replace(R.id.valigia_main_frame, new ValigettaStep1TrovaUtenteFragment());
                break;
            case 2:
                ft.replace(R.id.valigia_main_frame, new ValigettaStep2ScegliStrumentoFragment());
                break;
            case 3:
                btFragment = new ValigettaStep3ComunicaConValigettaFragment();
                ft.replace(R.id.valigia_main_frame, btFragment);
                break;
        }
        actualStep = step;
        ft.commit();

        // Svuoto eventualmente tutti i risultati ottenuti nello step del collegamento bluetooth
        nomiDispositiviTrovati.clear();
        dispositiviTrovati.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (btAdapter != null && btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        if (receiverRegistrato) {
            receiverRegistrato = false;
            unregisterReceiver(mReceiver);
        }
    }

    public void setChosenUser(Utente user) {
        utenteSelezionato = user;
    }

    public Utente getChosenUser() {
        return utenteSelezionato;
    }

    public void setSensore(String s) {
        sensore = s;
    }

    public String getSensore() {
        return sensore;
    }

    public void setMalattieUtente(String s) {
        malattieUtente = s;
    }

    public String getMalattieUtente() {
        return malattieUtente;
    }


    // Metodi necessari per la connessione BT con la valigetta
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBTFunctionPT1();
            }
        }
    }

    protected void startBTFunctionPT1() {
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

    private void startBTFunctionPT2() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Attivo la visibilità per gli altri device
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, 2);
    }

    private void startBTFunctionPT3() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (btAdapter != null && btAdapter.isEnabled()) {
            if (btAdapter.isDiscovering()) {
                btAdapter.cancelDiscovery();
            }
            registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            receiverRegistrato = true;
            btAdapter.startDiscovery();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (btAdapter != null && btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        if (receiverRegistrato) {
            unregisterReceiver(mReceiver);
        }
    }

    protected BluetoothDevice getDevice(int i){
        try{
            return dispositiviTrovati.get(i);
        } catch(IndexOutOfBoundsException e){
            return null;
        }
    }
}