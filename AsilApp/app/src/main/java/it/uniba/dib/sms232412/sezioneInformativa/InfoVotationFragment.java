package it.uniba.dib.sms232412.sezioneInformativa;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.uniba.dib.sms232412.R;

public class InfoVotationFragment extends Fragment {

    final private FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
    final private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_votation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(user == null) return;
        String userUid = user.getUid();

        // Imposto la logica per il panel del voto dell'app
        RatingBar ratingApp = view.findViewById(R.id.rating_bar_asilapp);
        TextView textVotoUtenteApp = view.findViewById(R.id.voto_asilapp_personale);
        TextView textVotoMedioApp = view.findViewById(R.id.voto_asilapp_medio);

        DatabaseReference rootDBCalcoloMediaApp = fireDB.getReference("VotiApp").child("VotiUtenti");
        DatabaseReference rootDBVotoUtenteApp = rootDBCalcoloMediaApp.child(userUid);
        DatabaseReference rootDBVotoMedioApp = fireDB.getReference("VotiApp").child("Media");

        setRatingPanel(rootDBCalcoloMediaApp, rootDBVotoUtenteApp, rootDBVotoMedioApp, ratingApp, textVotoUtenteApp, textVotoMedioApp);

        // Recupero la regione letta dal fragment genitore
        if(!(getParentFragment() instanceof InfoFragment)) return;
        InfoFragment parentFrag = (InfoFragment) getParentFragment();
        int num_region = parentFrag.num_region;

        if(num_region == -1) return;
        // Da qui procedo solo se ho una regione italiana registrata

        String region = getResources().getStringArray(R.array.regioni_italiane)[num_region];
        TextView titleVoteRegionPanel = view.findViewById(R.id.title_voto_regione);
        titleVoteRegionPanel.setText(getString(R.string.info_voto_regione_title, region));
        RelativeLayout containerVoteRegion = view.findViewById(R.id.container_2);
        containerVoteRegion.setVisibility(View.VISIBLE);

        // Imposto la logica per il voto della regione di appartenenza
        RatingBar ratingRegion = view.findViewById(R.id.rating_bar_regione);
        TextView textVotoUtenteRegion = view.findViewById(R.id.voto_regione_personale);
        TextView textVotoMedioRegion = view.findViewById(R.id.voto_regione_medio);

        DatabaseReference rootDBCalcoloMediaRegione = fireDB.getReference("VotiRegione").child(region).child("VotiUtenti");
        DatabaseReference rootDBVotoUtenteRegione = rootDBCalcoloMediaRegione.child(userUid);
        DatabaseReference rootDBVotoMedioRegione = fireDB.getReference("VotiRegione").child(region).child("Media");

        setRatingPanel(rootDBCalcoloMediaRegione, rootDBVotoUtenteRegione, rootDBVotoMedioRegione, ratingRegion, textVotoUtenteRegion, textVotoMedioRegion);
    }


    // Metodo per impostare i panel di votazione
    // Generalizzato sia per il panel di votazione dell'app che per quello della regione
    private void setRatingPanel(DatabaseReference calcoloMediaRoot, DatabaseReference userRoot, DatabaseReference averageRoot, RatingBar myRatingBar, TextView txtVotoUser, TextView txtVotoAverage){
        // Recupero e imposto il voto pregresso dato dall'utente (se esiste)
        userRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null){
                    int votoUtente = ((Long)snapshot.getValue()).intValue();
                    myRatingBar.setRating(votoUtente);
                    txtVotoUser.setVisibility(View.VISIBLE);
                    txtVotoUser.setText(getString(R.string.info_voto_result, votoUtente));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Recupero e imposto il voto medio pregresso dato dagli utenti (se esiste)
        averageRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null){
                    int votoMedio = ((Long)snapshot.getValue()).intValue();
                    if(votoMedio != -1) {
                        txtVotoAverage.setText(getString(R.string.info_voto_medio, votoMedio));
                    } else {
                        txtVotoAverage.setText(getString(R.string.info_voto_medio_inesistente));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Assegno la logica alla rating bar per votare
        myRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            // Imposto il nuovo voto per l'utente
            userRoot.setValue((int)rating);
            if(txtVotoUser.getVisibility() != View.VISIBLE) txtVotoUser.setVisibility(View.VISIBLE);
            txtVotoUser.setText(getString(R.string.info_voto_result, (int)rating));

            // Modifico il voto medio
            calcoloMediaRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        int media = 0;
                        int numeroVoti = 0;
                        for(DataSnapshot childSnapshot : snapshot.getChildren()){
                            if(childSnapshot.getValue()!=null){
                                media += ((Long)childSnapshot.getValue()).intValue();
                                numeroVoti++;
                            }
                        }
                        media = media/numeroVoti;
                        averageRoot.setValue(media);
                        txtVotoAverage.setText(getString(R.string.info_voto_medio, media));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
    }
}