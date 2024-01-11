package it.uniba.dib.sms232412.valigettaMedica;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.personaleSanitario.PersonaleSanitario;
import it.uniba.dib.sms232412.utils.Utente;

public class ListaUtentiAdapter extends BaseAdapter {

    final private List<Utente> listaUtenti;
    final private List<Utente> listaUtentiOriginale;
    final private Context context;
    final private int layout_single_line;
    final private FirebaseDatabase dbRef = FirebaseDatabase.getInstance();

    public ListaUtentiAdapter(@NonNull Context context, int layout_single_line, List<Utente> listaUtenti) {
        this.context = context;
        this.listaUtenti = listaUtenti;
        this.listaUtentiOriginale = new ArrayList<>(listaUtenti);
        this.layout_single_line = layout_single_line;
    }

    @Override
    public int getCount() {
        return listaUtenti.size();
    }

    @Override
    public Object getItem(int position) {
        return listaUtenti.get(position);
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
        Utente myUtente = listaUtenti.get(position);

        TextView textNome = convertView.findViewById(R.id.nome);
        TextView textCognome = convertView.findViewById(R.id.cognome);
        TextView textEmail = convertView.findViewById(R.id.email);

        textNome.setText(myUtente.getNome());
        textCognome.setText(myUtente.getCognome());
        textEmail.setText(myUtente.getEmail());

        return convertView;
    }

    public void filterText(String txt){
        listaUtenti.clear();
        for (Utente utente: listaUtentiOriginale) {
            if(utente.getNome().toLowerCase().contains(txt.toLowerCase())
                    || utente.getCognome().toLowerCase().contains(txt.toLowerCase())){
                listaUtenti.add(utente);
            }
        }
        notifyDataSetChanged();
    }
}
