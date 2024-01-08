package it.uniba.dib.sms232412.adminSection;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import it.uniba.dib.sms232412.R;

public class ListaRichiestePerDiventarePSAdapter extends BaseAdapter {
    final private List<RichiestaPerDiventarePS> listaRichieste;
    final private Context context;
    final private int layout_single_line;
    final private AdminActivity parent;
    final private FirebaseDatabase dbRef = FirebaseDatabase.getInstance();

    public ListaRichiestePerDiventarePSAdapter(@NonNull Context context, int layout_single_line, List<RichiestaPerDiventarePS> richiestePS, AdminActivity parent) {
        this.context = context;
        this.listaRichieste = richiestePS;
        this.layout_single_line = layout_single_line;
        this.parent = parent;
    }
    @Override
    public int getCount() {
        return listaRichieste.size();
    }

    @Override
    public Object getItem(int position) {
        return listaRichieste.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout_single_line, parent, false);
        }
        RichiestaPerDiventarePS myRequest = listaRichieste.get(position);

        TextView textCodiceFiscale = convertView.findViewById(R.id.codice_fiscale);
        TextView textEmail = convertView.findViewById(R.id.email);
        TextView textNomeCompleto = convertView.findViewById(R.id.nome_completo);
        TextView textProfessione = convertView.findViewById(R.id.professione);
        TextView textIstituzione = convertView.findViewById(R.id.istituzione);
        TextView textRegione = convertView.findViewById(R.id.regione);

        Resources res = context.getResources();
        textCodiceFiscale.setText(res.getString(R.string.admin_single_request_codiceFiscale, myRequest.getCodiceFiscale()));
        textEmail.setText(res.getString(R.string.admin_single_request_email, myRequest.getEmail()));
        textNomeCompleto.setText(res.getString(R.string.admin_single_request_nomeCompleto, myRequest.getNomeCompleto()));
        textProfessione.setText(res.getString(R.string.admin_single_request_professione, myRequest.getProfessione()));
        textIstituzione.setText(res.getString(R.string.admin_single_request_istituzione, myRequest.getIstituzione()));
        textRegione.setText(res.getString(R.string.admin_single_request_regione, myRequest.getRegione()));

        ImageButton btnConfirm = convertView.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            // Aggiungo l'utente come personale sanitario
            String psUid = myRequest.getUid();
            DatabaseReference dbRootPS = dbRef.getReference("PersonaleSanitario").child(psUid);
            dbRootPS.child("uid").setValue(psUid);
            dbRootPS.child("nomeCompleto").setValue(myRequest.getNomeCompleto());
            dbRootPS.child("email").setValue(myRequest.getEmail());
            dbRootPS.child("professione").setValue(myRequest.getProfessione());
            dbRootPS.child("istituzione").setValue(myRequest.getIstituzione());
            dbRootPS.child("regione").setValue(myRequest.getRegione());
            dbRootPS.child("visibility").setValue(myRequest.getVisibility());

            // Modifico il ruolo dell'utente e aggiorno il suo profilo
            DatabaseReference dbRootRoleUser = dbRef.getReference("Utenti").child(psUid).child("ruolo");
            dbRootRoleUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String previousRole = snapshot.getValue(String.class);
                        if(previousRole == null) return;
                        if(previousRole.equals("utente")){
                            dbRootRoleUser.setValue("personaleSanitario");
                        } else if (previousRole.equals("admin")) {
                            dbRootRoleUser.setValue("adminPersonaleSanitario");
                        }
                        DatabaseReference dbRootUpdateUser = dbRef.getReference("Utenti").child(psUid).child("update");
                        dbRootUpdateUser.setValue(true);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // Tolgo la richiesta in sospeso per diventare personale sanitario
            DatabaseReference dbRootRequest = dbRef.getReference("RichiesteRegistrazioneComePersonaleSanitario").child(psUid);
            dbRootRequest.removeValue();
            reloadParent();
        });

        ImageButton btnDelete = convertView.findViewById(R.id.btn_denied);
        btnDelete.setOnClickListener(v -> {
            // Tolgo la richiesta in sospeso per diventare personale sanitario
            DatabaseReference dbRootRequest = dbRef.getReference("RichiesteRegistrazioneComePersonaleSanitario").child(myRequest.getUid());
            dbRootRequest.removeValue();
            reloadParent();
        });
        return convertView;
    }

    private void reloadParent(){
        parent.reloadPage();
    }
}
