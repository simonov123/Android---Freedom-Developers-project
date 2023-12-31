package it.uniba.dib.sms232412;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.uniba.dib.sms232412.utils.Utente;

public class AnagraficaFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_anagrafica, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textMail = view.findViewById(R.id.anagrafica_mail);
        TextView textName = view.findViewById(R.id.anagrafica_name);
        TextView textSurname = view.findViewById(R.id.anagrafica_surname);
        TextView textSesso = view.findViewById(R.id.anagrafica_sesso);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String myUid = user.getUid();
            DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(myUid);
            rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Utente myUser = snapshot.getValue(Utente.class);
                        if(myUser != null){
                            textMail.setText(myUser.getEmail());
                            textName.setText(myUser.getNome());
                            textSurname.setText(myUser.getCognome());
                            if(myUser.getSesso().equals("M")){
                                textSesso.setText(getString(R.string.anagrafica_maschio));
                            } else {
                                textSesso.setText(getString(R.string.anagrafica_femmina));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}