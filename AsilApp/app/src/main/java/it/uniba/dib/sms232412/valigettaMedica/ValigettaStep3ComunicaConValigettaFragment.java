package it.uniba.dib.sms232412.valigettaMedica;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.utils.Utente;

public class ValigettaStep3ComunicaConValigettaFragment extends Fragment {

    private ValigettaActivity parentActivity;
    private ArrayAdapter<String> adapter;
    private ConnectThread connectThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_valigetta_step3_comunica_con_valigetta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Controllo l'activity ospitante e ricavo l'utente selezionato nello step 1
        if(getActivity() == null || !(getActivity() instanceof ValigettaActivity)) return;
        parentActivity = (ValigettaActivity) getActivity();
        Utente chosenUser = parentActivity.getChosenUser();

        // Imposto i dati dell'utente scelto da mostrare in questo step
        TextView txtChosenUser = view.findViewById(R.id.chosen_user);
        TextView txtChosenUserGender = view.findViewById(R.id.chosen_user_gender);
        TextView txtChosenUserMalattie = view.findViewById(R.id.chosen_user_malattie);
        TextView txtChosenSensore = view.findViewById(R.id.chosen_sensore);

        txtChosenUser.setText(getResources().getString(R.string.valigia_utente_scelto, (chosenUser.getNome() + " " + chosenUser.getCognome())));
        if(chosenUser.getSesso().equals("M")){
            txtChosenUserGender.setText(getString(R.string.anagrafica_maschio));
        } else {
            txtChosenUserGender.setText(getString(R.string.anagrafica_femmina));
        }
        txtChosenUserMalattie.setText(parentActivity.getMalattieUtente());
        switch (parentActivity.getSensore()){
            case "TEMPERATURA":
                txtChosenSensore.setText(getResources().getString(R.string.valigia_sensore_selezionato, getString(R.string.valigia_sensore_temperatura)));
                break;
            case "PRESSIONE":
                txtChosenSensore.setText(getResources().getString(R.string.valigia_sensore_selezionato, getString(R.string.valigia_sensore_pressione)));
                break;
            case "BATTITI":
                txtChosenSensore.setText(getResources().getString(R.string.valigia_sensore_selezionato, getString(R.string.valigia_sensore_battiti)));
                break;
        }

        // Gestione tasto per avviare la comunicazione Bluetooth con la valigetta medica
        Button test = view.findViewById(R.id.prova_btn_client);
        test.setOnClickListener(v -> {
            // Verifico che il bluetooth sia abilitato sul device
            if(BluetoothAdapter.getDefaultAdapter() == null){
                Toast.makeText(parentActivity, getString(R.string.valigia_bt_non_abilitato), Toast.LENGTH_SHORT).show();
                return;
            }
            // Verifico che la versione di Android sia almeno pari a 12 (API 31)
            if(Build.VERSION.SDK_INT < 31){
                Toast.makeText(parentActivity, getString(R.string.valigia_bt_versione_device_non_valida), Toast.LENGTH_SHORT).show();
                return;
            }
            // Gestione del permesso per usare il bluetooth
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.BLUETOOTH_SCAN)){
                    new AlertDialog.Builder(parentActivity)
                            .setTitle(R.string.valigia_bt_rationale_title)
                            .setMessage(R.string.valigia_bt_rationale_msg)
                            .setPositiveButton(R.string.permission_ok, (dialog, which) ->
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN,
                                            Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE,
                                            Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},1))
                            .show();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},1);
                }
            } else {
                parentActivity.startBTFunctionPT1();
            }
        });

        // Imposto la lista dei device trovati
        ListView listaDeviceTrovati = view.findViewById(R.id.lista_device_trovati);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listaDeviceTrovati.setAdapter(adapter);

        // Per ogni device trovato imposto il collegamento BT con esso
        listaDeviceTrovati.setOnItemClickListener((parent, view1, position, id) -> {
            BluetoothDevice device = parentActivity.getDevice(position);
            if(device != null){
                connectThread = new ConnectThread(parentActivity, device);
                connectThread.start();
                Toast.makeText(parentActivity, getString(R.string.valigia_collegamento_riuscito), Toast.LENGTH_SHORT).show();
            }
        });

        // Imposto il bottone per registrare il valore misurato nella valigetta nel DB
        // N.B. il bottone sarà visibile solo dopo aver ricevuto un valore dalla valigetta
        Button sendButton = view.findViewById(R.id.btn_registra_valore_ricevuto);
        sendButton.setOnClickListener(v -> {
            DatabaseReference dbRoot = FirebaseDatabase.getInstance().getReference("Utenti")
                    .child(chosenUser.getUid()).child("misurazioni").child(parentActivity.getSensore());
            dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(parentActivity.misuraRilevata == null) return;
                    if(snapshot.exists()){
                        GenericTypeIndicator<List<Double>> typeIndicator = new GenericTypeIndicator<List<Double>>() {};
                        List<Double> listaMisurePresenti = snapshot.getValue(typeIndicator);
                        if(listaMisurePresenti != null){
                            // Aggiungo la nuova misura effettuata
                            listaMisurePresenti.add(parentActivity.misuraRilevata);
                            // Conservo solo le ultime 10 misure effettuate
                            if(listaMisurePresenti.size() > 10){
                                listaMisurePresenti.remove(0);
                            }
                            dbRoot.setValue(listaMisurePresenti).addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    Toast.makeText(getActivity(), R.string.valigia_valore_registrato_con_successo, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), R.string.valigia_valore_non_registrato, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        List<Double> listaMisureNuove = new ArrayList<>();
                        // Aggiungo la nuova misura effettuata
                        listaMisureNuove.add(parentActivity.misuraRilevata);
                        dbRoot.setValue(listaMisureNuove).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), R.string.valigia_valore_registrato_con_successo, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), R.string.valigia_valore_non_registrato, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    // Mando l'avviso di aggiornamento all'utente su cui è stata confermata la misurazione
                    FirebaseDatabase.getInstance().getReference("Utenti").child(chosenUser.getUid())
                            .child("update").setValue(true);
                    parentActivity.finish();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        // Imposto il bottone per ricevere il valore misurato nella Valigetta Medica
        TextView testoValoreRicevuto = view.findViewById(R.id.testo_valore_ricevuto);
        Button btnRiceviValore = view.findViewById(R.id.btn_ricevi_valore);
        btnRiceviValore.setOnClickListener(v -> {
            if(parentActivity.misuraRilevata == null){
                Toast.makeText(parentActivity, getString(R.string.valigia_misura_ancora_non_fatta), Toast.LENGTH_SHORT).show();
            } else {
                switch (parentActivity.getSensore()){
                    case "TEMPERATURA":
                        testoValoreRicevuto.setText(getResources().getString(R.string.valigia_misura_temperatura_result, parentActivity.misuraRilevata));
                        break;
                    case "PRESSIONE":
                        testoValoreRicevuto.setText(getResources().getString(R.string.valigia_misura_pressione_result, parentActivity.misuraRilevata));
                        break;
                    case "BATTITI":
                        testoValoreRicevuto.setText(getResources().getString(R.string.valigia_misura_battiti_result, parentActivity.misuraRilevata));
                        break;
                }
                // Sono certo di aver ricevuto il valore dalla valigetta, quindi rendo visibile
                // il bottone per registrare tale valore nel database (Firebase)
                sendButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void updateListaDeviceTrovati(List<String> device){
        adapter.clear();
        adapter.addAll(device);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(connectThread != null){
            connectThread.cancel();
        }
    }
}