package it.uniba.dib.sms232412.personaleSanitario;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

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
import it.uniba.dib.sms232412.utils.GestoreRegione;

public class PersonaleSanitarioModificaStatusPSFragment extends Fragment {

    private DatabaseReference dbRootToPS;
    private TextInputEditText editProfessione, editIstituzione;
    private String region = "";
    private SwitchCompat mySwitch;
    private PersonaleSanitarioModificaStatusPSFragment.CompleteListener listenerComplete = null;
    private boolean mustShowDialogForVisibility = true;

    public interface CompleteListener {
        void makeStartConfig();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof PersonaleSanitarioModificaStatusPSFragment.CompleteListener){
            listenerComplete = (PersonaleSanitarioModificaStatusPSFragment.CompleteListener) context;
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
        return inflater.inflate(R.layout.fragment_personale_sanitario_modifica_status_p_s, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Imposto le variabili per leggere i dati
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

        //Gestione per lo switch sulla visibilità
        mySwitch = view.findViewById(R.id.switch_visibility);
        mySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(mustShowDialogForVisibility && isChecked && getActivity() != null){
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

        // Inserisco i dati precedentemente salvati
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return;
        String userUid = user.getUid();
        dbRootToPS = FirebaseDatabase.getInstance().getReference("PersonaleSanitario").child(userUid);
        dbRootToPS.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    PersonaleSanitario personaleSanitario = snapshot.getValue(PersonaleSanitario.class);
                    if(personaleSanitario != null) {
                        editProfessione.setText(personaleSanitario.getProfessione());
                        editIstituzione.setText(personaleSanitario.getIstituzione());
                        regionSpinner.setSelection(GestoreRegione.getNumberRegion(personaleSanitario.getRegione()));
                        mustShowDialogForVisibility = false;
                        mySwitch.setChecked(personaleSanitario.getVisibility());
                        mustShowDialogForVisibility = true;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Gestione per salvare i nuovi dati dello status da personale sanitario
        Button sendRequestButton = view.findViewById(R.id.btn_confirm_request);
        sendRequestButton.setOnClickListener(v -> {
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

            // Dopo i controlli procedo con la modifica dello status da personale sanitario
            dbRootToPS.child("professione").setValue(professione);
            dbRootToPS.child("istituzione").setValue(istituzione);
            dbRootToPS.child("regione").setValue(region);
            dbRootToPS.child("visibility").setValue(mySwitch.isChecked());
            Toast.makeText(getActivity(), getString(R.string.ps_richiesta_modificata_confirm), Toast.LENGTH_SHORT).show();
            listenerComplete.makeStartConfig();
        });
    }
}