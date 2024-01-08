package it.uniba.dib.sms232412.personaleSanitario;

import android.content.Context;
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
import it.uniba.dib.sms232412.utils.GestoreRegione;
import it.uniba.dib.sms232412.adminSection.RichiestaPerDiventarePS;


public class PersonaleSanitarioModificaRichiestaFragment extends Fragment {

    private DatabaseReference dbRootToCheckPSRequest;
    private TextInputEditText editCodiceFiscale, editProfessione, editIstituzione;
    private String region = "";
    private SwitchCompat mySwitch;
    private PersonaleSanitarioModificaRichiestaFragment.CompleteListener listenerComplete = null;
    private boolean mustShowDialogForVisibility = true;
    public interface CompleteListener {
        void makeStartConfig();
        void setRequestPendant(boolean isPendant);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof PersonaleSanitarioModificaRichiestaFragment.CompleteListener){
            listenerComplete = (PersonaleSanitarioModificaRichiestaFragment.CompleteListener) context;
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
        return inflater.inflate(R.layout.fragment_personale_sanitario_modifica_richiesta, container, false);
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
        dbRootToCheckPSRequest = FirebaseDatabase.getInstance().getReference("RichiesteRegistrazioneComePersonaleSanitario").child(userUid);
        dbRootToCheckPSRequest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    RichiestaPerDiventarePS previousRequest = snapshot.getValue(RichiestaPerDiventarePS.class);
                    if(previousRequest != null) {
                        editCodiceFiscale.setText(previousRequest.getCodiceFiscale());
                        editProfessione.setText(previousRequest.getProfessione());
                        editIstituzione.setText(previousRequest.getIstituzione());
                        regionSpinner.setSelection(GestoreRegione.getNumberRegion(previousRequest.getRegione()));
                        mustShowDialogForVisibility = false;
                        mySwitch.setChecked(previousRequest.getVisibility());
                        mustShowDialogForVisibility = true;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Gestione per salvare i nuovi dati della richiesta
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

            // Dopo i controlli procedo con la modifica della richiesta per diventare personale sanitario
            dbRootToCheckPSRequest.child("codiceFiscale").setValue(codiceFiscale);
            dbRootToCheckPSRequest.child("professione").setValue(professione);
            dbRootToCheckPSRequest.child("istituzione").setValue(istituzione);
            dbRootToCheckPSRequest.child("regione").setValue(region);
            dbRootToCheckPSRequest.child("visibility").setValue(mySwitch.isChecked());
            Toast.makeText(getActivity(), getString(R.string.ps_richiesta_modificata_confirm), Toast.LENGTH_SHORT).show();
            listenerComplete.makeStartConfig();
        });

        // Gestione per eliminare la richiesta
        Button btnDelete = view.findViewById(R.id.btn_delete_request);
        btnDelete.setOnClickListener(v -> {
            if(getActivity() == null) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.ps_elimina_richiesta)
                    .setMessage(R.string.ps_elimina_richiesta_msg)
                    .setPositiveButton(R.string.option_menu_yes, (dialog, which) -> {
                        dbRootToCheckPSRequest.removeValue();
                        listenerComplete.setRequestPendant(false);
                        Toast.makeText(getActivity(), getString(R.string.ps_richiesta_eliminata_confirm), Toast.LENGTH_SHORT).show();
                        listenerComplete.makeStartConfig();
                    })
                    .setNegativeButton(R.string.option_menu_no, (dialog, which) -> {});
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }
}