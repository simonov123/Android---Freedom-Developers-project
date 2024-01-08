package it.uniba.dib.sms232412.personaleSanitario;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms232412.R;

public class ListaPersonaleSanitarioAdapter extends BaseAdapter {

    final private List<PersonaleSanitario> listaPersonale;
    final private List<PersonaleSanitario> listaPersonaleOriginale;
    final private Context context;
    final private int layout_single_line;
    final private FirebaseDatabase dbRef = FirebaseDatabase.getInstance();

    public ListaPersonaleSanitarioAdapter(@NonNull Context context, int layout_single_line, List<PersonaleSanitario> listaPersonale) {
        this.context = context;
        this.listaPersonale = listaPersonale;
        this.listaPersonaleOriginale = new ArrayList<>(listaPersonale);
        this.layout_single_line = layout_single_line;
    }


    @Override
    public int getCount() {
        return listaPersonale.size();
    }

    @Override
    public Object getItem(int position) {
        return listaPersonale.get(position);
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
        PersonaleSanitario myPersonaleSanitario = listaPersonale.get(position);

        TextView textEmail = convertView.findViewById(R.id.email);
        TextView textNomeCompleto = convertView.findViewById(R.id.nome_completo);
        TextView textProfessione = convertView.findViewById(R.id.professione);
        TextView textIstituzione = convertView.findViewById(R.id.istituzione);
        TextView textRegione = convertView.findViewById(R.id.regione);

        Resources res = context.getResources();
        textEmail.setText(res.getString(R.string.admin_single_request_email, myPersonaleSanitario.getEmail()));
        textNomeCompleto.setText(res.getString(R.string.admin_single_request_nomeCompleto, myPersonaleSanitario.getNomeCompleto()));
        textProfessione.setText(res.getString(R.string.admin_single_request_professione, myPersonaleSanitario.getProfessione()));
        textIstituzione.setText(res.getString(R.string.admin_single_request_istituzione, myPersonaleSanitario.getIstituzione()));
        textRegione.setText(res.getString(R.string.admin_single_request_regione, myPersonaleSanitario.getRegione()));

        // Gestione tasto send

        return convertView;
    }

    public void filterText(String txt, boolean allRegion, String region){
        listaPersonale.clear();
        for (PersonaleSanitario ps: listaPersonaleOriginale) {
            if((allRegion || ps.getRegione().equals(region)) &&
                    ps.getNomeCompleto().toLowerCase().contains(txt.toLowerCase())){
                listaPersonale.add(ps);
            }
        }
        notifyDataSetChanged();
    }
}
