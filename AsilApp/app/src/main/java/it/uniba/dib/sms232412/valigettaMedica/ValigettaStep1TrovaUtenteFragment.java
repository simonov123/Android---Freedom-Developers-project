package it.uniba.dib.sms232412.valigettaMedica;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import it.uniba.dib.sms232412.utils.Utente;

public class ValigettaStep1TrovaUtenteFragment extends Fragment {

    private ValigettaActivity parentActivity;
    private ListaUtentiAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_valigetta_step1_trova_utente, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() == null || !(getActivity() instanceof ValigettaActivity)) return;
        parentActivity = (ValigettaActivity) getActivity();

        ListView listViewUser = view.findViewById(R.id.user_list);
        TextInputEditText editSearch = view.findViewById(R.id.edit_search);

        DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti");
        rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<Utente> myList = new ArrayList<>();
                    for(DataSnapshot childSnapshot : snapshot.getChildren()){
                        myList.add(childSnapshot.getValue(Utente.class));
                    }
                    if(myList.size() > 0 && getContext() != null){
                        Collections.sort(myList);
                        adapter = new ListaUtentiAdapter(getContext(), R.layout.lista_utenti_single_element_layout, myList);
                        listViewUser.setAdapter(adapter);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        listViewUser.setOnItemClickListener((parent, view1, position, id) -> {
            parentActivity.setChosenUser((Utente) adapter.getItem(position));
            parentActivity.changeToStep(2);
        });

        // Gestione ricerca da barra di testo
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterText(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}