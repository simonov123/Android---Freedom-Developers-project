package it.uniba.dib.sms232412.personaleSanitario;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.utils.Utente;

public class PersonaleSanitarioCreaRichiestaFragment extends Fragment {

    private TextInputEditText editCodiceFiscale, editProfessione, editIstituzione;
    private String region = "";
    private SwitchCompat mySwitch;
    private CompleteListener listenerComplete = null;
    public interface CompleteListener {
        void makeStartConfig();
        void setRequestPendant(boolean isPendant);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof CompleteListener){
            listenerComplete = (CompleteListener) context;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listenerComplete = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_personale_sanitario_crea_richiesta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Imposto le variabili per leggere i dati
        editCodiceFiscale = view.findViewById(R.id.edit_codiceFiscale);
        editCodiceFiscale.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        editCodiceFiscale.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(16)});
        editProfessione = view.findViewById(R.id.edit_professione);
        editIstituzione = view.findViewById(R.id.edit_istituzione);

        //Gestione dello spinner per registrare la regione
        Spinner regionSpinner = view.findViewById(R.id.region_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.regioni_italiane,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(adapter);
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                region = getResources().getStringArray(R.array.regioni_italiane)[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Se ho una regione italiana registrata la imposto di default
        if(getActivity() != null){
            SharedPreferences shared = getActivity().getSharedPreferences("regione.txt", Context.MODE_PRIVATE);
            int num_regione = shared.getInt("NUM_REGIONE", -1);
            if(num_regione != -1){
                regionSpinner.setSelection(num_regione);
            } else {
                regionSpinner.setSelection(0);
            }
        }

        //Gestione per lo switch sulla visibilità
        mySwitch = view.findViewById(R.id.switch_visibility);
        mySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.ps_attiva_visibility_title)
                        .setMessage(R.string.ps_attiva_visibility_msg)
                        .setPositiveButton(R.string.option_menu_yes, (dialog, which) -> {

                        })
                        .setNegativeButton(R.string.option_menu_no, (dialog, which) -> mySwitch.setChecked(false));

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //Gestione per l'invio della richiesta
        Button sendRequestButton = view.findViewById(R.id.btn_confirm_request);
        sendRequestButton.setOnClickListener(v -> {
            String codiceFiscale = String.valueOf(editCodiceFiscale.getText());
            if(codiceFiscale.length() != 16){
                Toast.makeText(getActivity(), getString(R.string.ps_invia_richiesta_error_codice_fiscale), Toast.LENGTH_SHORT).show();
                return;
            }
            String professione = String.valueOf(editProfessione.getText());
            if(professione.length() == 0){
                Toast.makeText(getActivity(), getString(R.string.ps_invia_richiesta_error_professione), Toast.LENGTH_SHORT).show();
                return;
            }
            String istituzione = String.valueOf(editIstituzione.getText());
            if(istituzione.length() == 0){
                Toast.makeText(getActivity(), getString(R.string.ps_invia_richiesta_error_istituzione), Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null){
                return;
            }
            // Dopo i controlli procedo con la creazione della richiesta per diventare personale sanitario
            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(userUid);
            rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Utente myUser = snapshot.getValue(Utente.class);
                        if(myUser != null && getActivity() != null){
                            DatabaseReference dbRootToCheckPSRequest = FirebaseDatabase.getInstance().getReference("RichiesteRegistrazioneComePersonaleSanitario").child(userUid);
                            dbRootToCheckPSRequest.child("uid").setValue(userUid);
                            dbRootToCheckPSRequest.child("nomeCompleto").setValue(myUser.getCognome() + " " + myUser.getNome());
                            dbRootToCheckPSRequest.child("email").setValue(myUser.getEmail());
                            dbRootToCheckPSRequest.child("codiceFiscale").setValue(codiceFiscale);
                            dbRootToCheckPSRequest.child("professione").setValue(professione);
                            dbRootToCheckPSRequest.child("istituzione").setValue(istituzione);
                            dbRootToCheckPSRequest.child("regione").setValue(region);
                            dbRootToCheckPSRequest.child("visibility").setValue(mySwitch.isChecked());
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.ps_invia_richiesta_completata_title)
                                    .setMessage(R.string.ps_invia_richiesta_completata_msg)
                                    .setPositiveButton(R.string.permission_ok, (dialog, which) -> {

                                    });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                            listenerComplete.setRequestPendant(true);
                            listenerComplete.makeStartConfig();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }
}