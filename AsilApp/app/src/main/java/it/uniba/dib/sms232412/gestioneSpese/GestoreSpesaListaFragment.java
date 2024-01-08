package it.uniba.dib.sms232412.gestioneSpese;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import it.uniba.dib.sms232412.R;

public class GestoreSpesaListaFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gestore_spesa_lista, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView myListView = view.findViewById(R.id.lista_spese);
        TextView myTextView = view.findViewById(R.id.text_check_empty_list);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String my_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(my_uid).child("spese");
            rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        GenericTypeIndicator<List<Spesa>> typeIndicator = new GenericTypeIndicator<List<Spesa>>() {};
                        List<Spesa> myList = snapshot.getValue(typeIndicator);
                        if(getContext() != null && myList != null && myList.size()>0){
                            ListaSpeseAdapter adapter = new ListaSpeseAdapter(getContext(), R.layout.lista_spese_single_element_layout, myList, (GestoreSpesaFragment) getParentFragment());
                            myListView.setAdapter(adapter);
                            myTextView.setText(R.string.spese_lista_non_vuota_msg);
                        } else {
                            myTextView.setText(R.string.spese_lista_vuota_msg);
                        }
                    } else {
                        myTextView.setText(R.string.spese_lista_vuota_msg);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}