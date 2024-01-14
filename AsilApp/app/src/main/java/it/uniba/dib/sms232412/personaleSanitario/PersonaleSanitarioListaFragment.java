package it.uniba.dib.sms232412.personaleSanitario;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.uniba.dib.sms232412.R;

public class PersonaleSanitarioListaFragment extends Fragment {

    private String region = "Nessuna";
    private ListaPersonaleSanitarioAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_personale_sanitario_lista, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() == null || !(getActivity() instanceof PersonaleSanitarioActivity)) return;
        PersonaleSanitarioActivity parentActivity = (PersonaleSanitarioActivity) getActivity();

        ListView listViewPS = view.findViewById(R.id.ps_list);
        CheckBox checkEveryRegion = view.findViewById(R.id.region_search_check);
        TextInputEditText editSearch = view.findViewById(R.id.edit_search);

        // Se ho una regione già registrata attivo il check per ogni regione
        SharedPreferences shared = getActivity().getSharedPreferences("regione.txt", Context.MODE_PRIVATE);
        region = shared.getString("REGIONE", "Nessuna");
        if(!region.equals("Nessuna")){
            checkEveryRegion.setVisibility(View.VISIBLE);
            checkEveryRegion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(adapter != null && editSearch.getText() != null){
                    adapter.filterText(editSearch.getText().toString(), isChecked, region);
                }
            });
        }

        // Gestione della lista da mostrare
        DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("PersonaleSanitario");
        rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<PersonaleSanitario> myList = new ArrayList<>();
                    for(DataSnapshot childSnapshot : snapshot.getChildren()){
                        PersonaleSanitario tmpPS = childSnapshot.getValue(PersonaleSanitario.class);
                        if(tmpPS != null && tmpPS.getVisibility()) myList.add(tmpPS);
                    }
                    if(myList.size() > 0 && getContext() != null){
                        Collections.sort(myList);
                        adapter = new ListaPersonaleSanitarioAdapter(getContext(), R.layout.lista_ps_single_element_layout, myList, parentActivity.getNomeCompletoUtente());
                        listViewPS.setAdapter(adapter);
                        if(!region.equals("Nessuna") && editSearch.getText() != null){
                            adapter.filterText(editSearch.getText().toString(), false, region);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Gestione ricerca da barra di testo
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterText(s.toString(), checkEveryRegion.isChecked(), region);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}