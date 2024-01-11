package it.uniba.dib.sms232412.gestioneSpese;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.utils.DatePickerFragment;

public class GestoreSpesaCalcolaFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private LocalDate dateFrom, dateTo;
    private TextView dateFromLabel, dateToLabel;
    private boolean isStartDateToModify;
    private boolean allDate = false;
    private boolean allType = true;
    final private DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MMMM/yyyy");
    private String tipoSpesa;
    private TextView labelRisultato;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gestore_spesa_calcola, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //gestione per la data di inizio ricerca
        dateFrom = LocalDate.now();
        dateFromLabel = view.findViewById(R.id.data_start_selezionata);
        dateFromLabel.setText(dateFrom.format(formato));
        ImageButton btnChangeDateFrom = view.findViewById(R.id.btn_data_start);
        btnChangeDateFrom.setOnClickListener(v -> {
            if(getActivity() != null){
                isStartDateToModify = true;
                DialogFragment myDatePicker = new DatePickerFragment(this);
                myDatePicker.show(getActivity().getSupportFragmentManager(), "TAG_PICKER_START_DATE");
            }
        });

        //gestione per la data di fine ricerca
        dateTo = LocalDate.now();
        dateToLabel = view.findViewById(R.id.data_end_selezionata);
        dateToLabel.setText(dateTo.format(formato));
        ImageButton btnChangeDateTo = view.findViewById(R.id.btn_data_end);
        btnChangeDateTo.setOnClickListener(v -> {
            if(getActivity() != null){
                isStartDateToModify = false;
                DialogFragment myDatePicker = new DatePickerFragment(this);
                myDatePicker.show(getActivity().getSupportFragmentManager(), "TAG_PICKER_END_DATE");
            }
        });

        //gestione per l'opzione "tutte le date"
        TextView textFrom = view.findViewById(R.id.txt_calcola_data_from);
        TextView textTo = view.findViewById(R.id.txt_calcola_data_to);
        CheckBox myCheck = view.findViewById(R.id.check_all_date);
        myCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            allDate = isChecked;
            if(isChecked){
                btnChangeDateFrom.setEnabled(false);
                btnChangeDateTo.setEnabled(false);
                btnChangeDateFrom.getBackground().setAlpha(128);
                btnChangeDateFrom.setColorFilter(R.color.color_data_attivata);
                btnChangeDateTo.getBackground().setAlpha(128);
                btnChangeDateTo.setColorFilter(R.color.color_data_attivata);
                dateFromLabel.setTextColor(getResources().getColor(R.color.color_data_disattivata));
                dateToLabel.setTextColor(getResources().getColor(R.color.color_data_disattivata));
                textFrom.setTextColor(getResources().getColor(R.color.color_data_disattivata));
                textTo.setTextColor(getResources().getColor(R.color.color_data_disattivata));
            } else {
                btnChangeDateFrom.setEnabled(true);
                btnChangeDateTo.setEnabled(true);
                btnChangeDateFrom.getBackground().setAlpha(255);
                btnChangeDateFrom.setColorFilter(null);
                btnChangeDateTo.getBackground().setAlpha(255);
                btnChangeDateTo.setColorFilter(null);
                dateFromLabel.setTextColor(getResources().getColor(R.color.color_data_attivata));
                dateToLabel.setTextColor(getResources().getColor(R.color.color_data_attivata));
                textFrom.setTextColor(getResources().getColor(R.color.color_data_attivata));
                textTo.setTextColor(getResources().getColor(R.color.color_data_attivata));
            }
        });

        //gestione per la scelta del tipo di spesa da cercare
        tipoSpesa = getResources().getStringArray(R.array.spese_lista_tipologie_piu_tutto)[0];
        Spinner spinner = view.findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.spese_lista_tipologie_piu_tutto,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoSpesa = getResources().getStringArray(R.array.spese_lista_tipologie_piu_tutto)[position];
                allType = (position == 0);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //gestione calcolo della spesa
        labelRisultato = view.findViewById(R.id.risultato_calcolo_spesa);
        Button btn_calcola = view.findViewById(R.id.btn_calcola);
        btn_calcola.setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser() == null){
                return;
            }
            String my_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(my_uid).child("spese");
            rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Double tot = 0.0;
                    if(snapshot.exists()){
                        GenericTypeIndicator<List<Spesa>> typeIndicator = new GenericTypeIndicator<List<Spesa>>() {};
                        List<Spesa> listaPresente = snapshot.getValue(typeIndicator);

                        if(listaPresente != null){
                            for(Spesa spesa:listaPresente){
                                if(allType || spesa.isType(tipoSpesa)){
                                    if(allDate || spesa.isDateInRange(dateFrom, dateTo)){
                                        tot += spesa.getCosto();
                                    }
                                }
                            }
                        }
                    }
                    labelRisultato.setText(getResources().getString(R.string.spese_risultato_calcolo_spesa, tot));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        LocalDate tmpDate = LocalDate.of(year, month+1, dayOfMonth);
        if(isStartDateToModify){
            if(tmpDate.isAfter(dateTo)){
                dateFrom = LocalDate.from(dateTo);
            } else {
                dateFrom = LocalDate.from(tmpDate);
            }
            dateFromLabel.setText(dateFrom.format(formato));
        } else {
            if(tmpDate.isBefore(dateFrom)){
                dateTo = LocalDate.from(dateFrom);
            } else {
                dateTo = LocalDate.from(tmpDate);
            }
            dateToLabel.setText(dateTo.format(formato));
        }
    }
}