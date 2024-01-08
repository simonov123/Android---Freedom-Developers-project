package it.uniba.dib.sms232412.gestioneSpese;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.utils.DatePickerFragment;
import it.uniba.dib.sms232412.utils.DecimalDigitsInputFilter;

public class GestoreSpesaAddFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private LocalDate dataSpesa;
    private TextView dataSpesaLabel;
    final private DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MMMM/yyyy");
    private String tipoSpesa;
    private Double costoSpesa;
    private TextInputEditText editCostoSpesa;
    final private ChiusuraPannelloAggiunta parent;

    public interface ChiusuraPannelloAggiunta {
        void chiudiPannelloAggiunta();
    }

    public GestoreSpesaAddFragment(ChiusuraPannelloAggiunta parent){
        this.parent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gestore_spesa_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //gestione data della spesa da aggiungere
        dataSpesa = LocalDate.now();
        dataSpesaLabel = view.findViewById(R.id.data_selezionata);
        dataSpesaLabel.setText(getResources().getString(R.string.spese_data_selezionata, dataSpesa.format(formato)));
        ImageButton btnChangeDate = view.findViewById(R.id.btn_data);
        btnChangeDate.setOnClickListener(v -> {
            if(getActivity() != null){
                DialogFragment myDatePicker = new DatePickerFragment(this);
                myDatePicker.show(getActivity().getSupportFragmentManager(), "date picker");
            }
        });

        //gestione tipo di prodotto della spesa da aggiungere
        tipoSpesa = getResources().getStringArray(R.array.spese_lista_tipologie)[0];
        Spinner spinner = view.findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.spese_lista_tipologie,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoSpesa = getResources().getStringArray(R.array.spese_lista_tipologie)[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //gestione costo della spesa da aggiungere
        editCostoSpesa = view.findViewById(R.id.edit_costo);
        editCostoSpesa.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});

        //gestione aggiunta della spesa nel database
        Button btn_add = view.findViewById(R.id.btn_add_spesa);
        btn_add.setOnClickListener(v -> {
            if(editCostoSpesa.getText() != null &&
                    ((editCostoSpesa.getText().toString().length() == 0) || editCostoSpesa.getText().toString().equals("."))){
                Toast.makeText(getContext(), R.string.spese_alert_inserire_costo, Toast.LENGTH_SHORT).show();
                return;
            }
            costoSpesa = Double.valueOf(editCostoSpesa.getText().toString());
            addSpesa(new Spesa(tipoSpesa, costoSpesa, dataSpesa.format(formato)));
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        dataSpesa = LocalDate.of(year, month+1, dayOfMonth);
        dataSpesaLabel.setText(getResources().getString(R.string.spese_data_selezionata, dataSpesa.format(formato)));
    }

    private void addSpesa(Spesa spesa){
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            return;
        }
        String my_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(my_uid).child("spese");
        rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    GenericTypeIndicator<List<Spesa>> typeIndicator = new GenericTypeIndicator<List<Spesa>>() {};
                    List<Spesa> listaPresente = snapshot.getValue(typeIndicator);
                    if(listaPresente != null){
                        listaPresente.add(spesa);
                        Collections.sort(listaPresente);
                        rootDB.setValue(listaPresente).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), R.string.spese_aggiunta_successo, Toast.LENGTH_SHORT).show();
                                parent.chiudiPannelloAggiunta();
                            } else {
                                Toast.makeText(getContext(), R.string.spese_aggiunta_fallimento, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    List<Spesa> listaNuova = new ArrayList<>();
                    listaNuova.add(spesa);
                    rootDB.setValue(listaNuova).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), R.string.spese_aggiunta_successo, Toast.LENGTH_SHORT).show();
                            parent.chiudiPannelloAggiunta();
                        } else {
                            Toast.makeText(getContext(), R.string.spese_aggiunta_fallimento, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}