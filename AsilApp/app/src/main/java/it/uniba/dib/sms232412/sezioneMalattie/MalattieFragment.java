package it.uniba.dib.sms232412.sezioneMalattie;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms232412.MainActivity;
import it.uniba.dib.sms232412.R;

public class MalattieFragment extends Fragment {
    private TextView titleMieMalattie;
    private ListView listaMieMalattie;
    private DatabaseReference dbRootMalattieUtente;
    private ListaMieMalattieAdapter personalAdapter;
    private MainActivity parentActivity;
    final private MalattieFragment thisFragment = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_malattie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getActivity() == null || !(getActivity() instanceof MainActivity)) return;
        parentActivity = (MainActivity) getActivity();
        dbRootMalattieUtente = FirebaseDatabase.getInstance().getReference("Utenti").child(parentActivity.getUserUid()).child("malattie");

        // Controllo le malattie dell'utente
        titleMieMalattie = view.findViewById(R.id.mie_malattie_titolo);
        listaMieMalattie = view.findViewById(R.id.lista_mie_malattie);
        updateMieMalattie();

        // Creo la lista per ricercare le malattie
        ListView listCercaMalattie = view.findViewById(R.id.lista_cerca_malattie);
        String[] listaMalattie, listaDescrizioni, urlVideo;

        // In base al sesso dell'utente uso una lista di malattie differente
        if(parentActivity.getUserSesso().equals("M")){
            listaMalattie = getResources().getStringArray(R.array.malattie_maschili_lista);
            listaDescrizioni = getResources().getStringArray(R.array.malattie_maschili_lista_descrizioni);
            urlVideo = new String[]{
                    // Febbre
                    "https://firebasestorage.googleapis.com/v0/b/freedomdev-asilapp-2324.appspot.com/o/febbre.mp4?alt=media&token=d575b420-ce69-4edc-839b-793915995aa5",
                    // Ipertensione
                    "https://firebasestorage.googleapis.com/v0/b/freedomdev-asilapp-2324.appspot.com/o/ipertensione.mp4?alt=media&token=5ac3d5c8-449f-40d0-bde6-8e062f009d9a",
                    // Tachicardia
                    "https://firebasestorage.googleapis.com/v0/b/freedomdev-asilapp-2324.appspot.com/o/tachicardia.mp4?alt=media&token=02665279-d62d-4e5e-9853-8bd5562d31b1"
            };
        } else {
            listaMalattie = getResources().getStringArray(R.array.malattie_femminili_lista);
            listaDescrizioni = getResources().getStringArray(R.array.malattie_femminili_lista_descrizioni);
            urlVideo = new String[]{
                    // Febbre
                    "https://firebasestorage.googleapis.com/v0/b/freedomdev-asilapp-2324.appspot.com/o/febbre.mp4?alt=media&token=d575b420-ce69-4edc-839b-793915995aa5",
                    // Sepsi Puerperale
                    "https://firebasestorage.googleapis.com/v0/b/freedomdev-asilapp-2324.appspot.com/o/sepsi_pauperale.mp4?alt=media&token=cd7d8a96-87b3-46fa-927f-6a794a6557b8",
                    // Ipertensione
                    "https://firebasestorage.googleapis.com/v0/b/freedomdev-asilapp-2324.appspot.com/o/ipertensione.mp4?alt=media&token=5ac3d5c8-449f-40d0-bde6-8e062f009d9a",
                    // Tachicardia
                    "https://firebasestorage.googleapis.com/v0/b/freedomdev-asilapp-2324.appspot.com/o/tachicardia.mp4?alt=media&token=02665279-d62d-4e5e-9853-8bd5562d31b1"
            };
        }

        ListaCercaMalattieAdapter adapter = new ListaCercaMalattieAdapter(parentActivity, thisFragment, R.layout.lista_malattie_ricerca_single_element_layout,
                listaMalattie, listaDescrizioni, urlVideo);
        listCercaMalattie.setAdapter(adapter);
    }

    public void updateMieMalattie(){
        dbRootMalattieUtente.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> myListaMalattie = new ArrayList<>();
                //List<String> myListaSuggerimenti = new ArrayList<>();
                if(snapshot.exists()){
                    // Rendo visibile la lista delle malattie personali
                    titleMieMalattie.setVisibility(View.VISIBLE);
                    listaMieMalattie.setVisibility(View.VISIBLE);

                    // Riempio la lista delle malattie registrate con i rispettivi suggerimenti
                    for(DataSnapshot childSnapshot : snapshot.getChildren()){
                        myListaMalattie.add(childSnapshot.getKey());
                    }

                    // Inizializzo/Aggiorno l'adapter per la lista delle malattie personali
                    if(personalAdapter == null){
                        personalAdapter = new ListaMieMalattieAdapter(parentActivity, thisFragment, R.layout.lista_malattie_personali_single_element_layout,
                                myListaMalattie);
                        listaMieMalattie.setAdapter(personalAdapter);
                    } else {
                        personalAdapter.rimpiazzaLista(myListaMalattie);
                    }
                } else {
                    // Rendo invisibile la lista delle malattie personali
                    titleMieMalattie.setVisibility(View.GONE);
                    listaMieMalattie.setVisibility(View.GONE);
                    if(personalAdapter != null){
                        personalAdapter.rimpiazzaLista(myListaMalattie);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}