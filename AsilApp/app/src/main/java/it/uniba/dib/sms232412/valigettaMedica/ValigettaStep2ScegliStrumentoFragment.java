package it.uniba.dib.sms232412.valigettaMedica;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.utils.Utente;

public class ValigettaStep2ScegliStrumentoFragment extends Fragment {

    final private DatabaseReference dbRoot = FirebaseDatabase.getInstance().getReference("Utenti");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_valigetta_step2_scegli_strumento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Controllo l'activity ospitante e ricavo l'utente selezionato nello step 1
        if(getActivity() == null || !(getActivity() instanceof ValigettaActivity)) return;
        ValigettaActivity parentActivity = (ValigettaActivity) getActivity();
        Utente chosenUser = parentActivity.getChosenUser();

        // Imposto i dati dell'utente scelto da mostrare in questo step
        TextView txtChosenUser = view.findViewById(R.id.chosen_user);
        TextView txtChosenUserGender = view.findViewById(R.id.chosen_user_gender);
        TextView txtChosenUserMalattie = view.findViewById(R.id.chosen_user_malattie);

        txtChosenUser.setText(getResources().getString(R.string.valigia_utente_scelto, (chosenUser.getNome() + " " + chosenUser.getCognome())));
        if(chosenUser.getSesso().equals("M")){
            txtChosenUserGender.setText(getString(R.string.anagrafica_maschio));
        } else {
            txtChosenUserGender.setText(getString(R.string.anagrafica_femmina));
        }
        DatabaseReference dbRootMalattieUtente = dbRoot.child(chosenUser.getUid()).child("malattie");
        dbRootMalattieUtente.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder userMalattie = new StringBuilder();
                if(snapshot.exists()){
                    boolean isFirst = true;
                    for(DataSnapshot childSnapshot : snapshot.getChildren()){
                        /**
                         * In base alla chiave nel db, che è sempre in lingua italiana,
                         * prendo la stringa gestita dal multilingua di Android
                         */
                        String malattia; // stringa relativa alla malattia gestita dal multilingua
                        if(childSnapshot.getKey() == null) break;
                        switch(childSnapshot.getKey()){
                            case "FEBBRE ALTA":
                                malattia = getResources().getStringArray(R.array.tutte_le_malattie)[0];
                                break;
                            case "SEPSI PUERPERALE":
                                malattia = getResources().getStringArray(R.array.tutte_le_malattie)[1];
                                break;
                            case "IPERTENSIONE":
                                malattia = getResources().getStringArray(R.array.tutte_le_malattie)[2];
                                break;
                            default:
                                malattia = getResources().getStringArray(R.array.tutte_le_malattie)[3];
                        }
                        if(isFirst){
                            isFirst = false;
                            userMalattie.append(" ").append(malattia);
                        } else {
                            userMalattie.append(", ").append(malattia);
                        }
                    }
                    parentActivity.setMalattieUtente(getResources().getString(R.string.valigia_utente_scelto_malattie, userMalattie));
                } else {
                    parentActivity.setMalattieUtente(getResources().getString(R.string.valigia_utente_scelto_malattie, getString(R.string.valigia_utente_scelto_nessuna_malattia)));
                }
                if(parentActivity.getMalattieUtente() != null)
                    txtChosenUserMalattie.setText(parentActivity.getMalattieUtente());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Imposto i bottoni per scegliere il sensore da richiamare
        Button btn_sensoreTemperatura = view.findViewById(R.id.btn_temperatura);
        btn_sensoreTemperatura.setOnClickListener(v -> {
            parentActivity.setSensore("TEMPERATURA");
            parentActivity.changeToStep(3);
        });

        Button btn_sensorePressione = view.findViewById(R.id.btn_pressione);
        btn_sensorePressione.setOnClickListener(v -> {
            parentActivity.setSensore("PRESSIONE");
            parentActivity.changeToStep(3);
        });

        Button btn_sensoreBattiti = view.findViewById(R.id.btn_battito);
        btn_sensoreBattiti.setOnClickListener(v -> {
            parentActivity.setSensore("BATTITI");
            parentActivity.changeToStep(3);
        });
    }
}