package it.uniba.dib.sms232412;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import it.uniba.dib.sms232412.utils.Utente;

public class AnagraficaFragment extends Fragment {

    final static private double LIMITE_MIN_TEMPERATURA = 35.0;
    final static private double LIMITE_MAX_TEMPERATURA = 37.5;
    final static private double LIMITE_MIN_PRESSIONE = 80.0;
    final static private double LIMITE_MAX_PRESSIONE = 120.0;
    final static private double LIMITE_MIN_BATTITI = 60.0;
    final static private double LIMITE_MAX_BATTITI = 100.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_anagrafica, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textRuolo = view.findViewById(R.id.anagrafica_ruolo);
        TextView textMail = view.findViewById(R.id.anagrafica_mail);
        TextView textName = view.findViewById(R.id.anagrafica_name);
        TextView textSurname = view.findViewById(R.id.anagrafica_surname);
        TextView textSesso = view.findViewById(R.id.anagrafica_sesso);
        TextView textRegion = view.findViewById(R.id.anagrafica_regione);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String myUid = user.getUid();
            DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(myUid);

            // Inserisco nella sezione anagrafica tutti i dati relativi all'utente loggato
            rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Utente myUser = snapshot.getValue(Utente.class);
                        if(getActivity() != null && myUser != null){
                            switch (myUser.getRuolo()){
                                case "utente":
                                    textRuolo.setText(getString(R.string.anagrafica_ruolo_utente));
                                    break;
                                case "personaleSanitario":
                                    textRuolo.setText(getString(R.string.anagrafica_ruolo_ps));
                                    break;
                                case "admin":
                                    textRuolo.setText(getString(R.string.anagrafica_ruolo_admin));
                                    break;
                                case "adminPersonaleSanitario":
                                    textRuolo.setText(getString(R.string.anagrafica_ruolo_ps_admin));
                            }
                            textMail.setText(myUser.getEmail());
                            textName.setText(myUser.getNome());
                            textSurname.setText(myUser.getCognome());
                            if(myUser.getSesso().equals("M")){
                                textSesso.setText(getString(R.string.anagrafica_maschio));
                            } else {
                                textSesso.setText(getString(R.string.anagrafica_femmina));
                            }
                            SharedPreferences shared = getActivity().getSharedPreferences("regione.txt", Context.MODE_PRIVATE);
                            textRegion.setText(shared.getString("REGIONE", getString(R.string.anagrafica_regione_non_specificata)));
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            // Inserisco nella sezione anagrafica anche tutte le misure effettuate dal personale sanitario
            TextView viewMisureTemperatura = view.findViewById(R.id.misure_temperatura);
            TextView viewMisurePressione = view.findViewById(R.id.misure_pressione);
            TextView viewMisureBattiti = view.findViewById(R.id.misure_battiti);

            DatabaseReference rootDBMisureTemperatura = rootDB.child("misurazioni").child("TEMPERATURA");
            DatabaseReference rootDBMisurePressione = rootDB.child("misurazioni").child("PRESSIONE");
            DatabaseReference rootDBMisureBattiti = rootDB.child("misurazioni").child("BATTITI");

            // Mostro i dati registrati per la temperatura corporea
            inserisciMisureEffettuate(viewMisureTemperatura, rootDBMisureTemperatura,
                    getString(R.string.anagrafica_temperatura_misure),
                    getString(R.string.anagrafica_temperatura_no_misure),
                    LIMITE_MIN_TEMPERATURA, LIMITE_MAX_TEMPERATURA);

            // Mostro i dati registrati per la pressione
            inserisciMisureEffettuate(viewMisurePressione, rootDBMisurePressione,
                    getString(R.string.anagrafica_pressione_misure),
                    getString(R.string.anagrafica_pressione_no_misure),
                    LIMITE_MIN_PRESSIONE, LIMITE_MAX_PRESSIONE);

            // Mostro i dati registrati per i battiti cardiaci
            inserisciMisureEffettuate(viewMisureBattiti, rootDBMisureBattiti,
                    getString(R.string.anagrafica_battiti_misure),
                    getString(R.string.anagrafica_battiti_no_misure),
                    LIMITE_MIN_BATTITI, LIMITE_MAX_BATTITI);
        }
    }

    // Metodo per mostrare le misure effettuate dal personale sanitario nella pagina anagrafica
    private void inserisciMisureEffettuate(TextView view, DatabaseReference rootDBMisura,
                                           String stringaConMisure, String stringaSenzaMisure,
                                           double limiteMin, double limiteMax) {
        rootDBMisura.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    GenericTypeIndicator<List<Double>> typeIndicator = new GenericTypeIndicator<List<Double>>() {};
                    List<Double> listaMisure = snapshot.getValue(typeIndicator);
                    if(listaMisure != null){
                        String s = stringaConMisure;
                        for (double d:listaMisure) {
                            if(d< limiteMin || d > limiteMax){
                                // Se ho un valore fuori dalla norma lo evidenzio
                                s = s.concat(" <font color='#FF0000'><b>" + d + "</b></font> /");
                            } else {
                                s = s.concat(" " + d + " /");
                            }
                        }
                        view.setText(Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY));
                    }
                } else {
                    view.setText(stringaSenzaMisure);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}