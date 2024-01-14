package it.uniba.dib.sms232412.sezioneMalattie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import it.uniba.dib.sms232412.MainActivity;
import it.uniba.dib.sms232412.R;

public class ListaMieMalattieAdapter extends BaseAdapter {

    final private List<String> listaMalattiePersonali;
    final private List<String> listaSuggerimentiMalattie;
    final private MainActivity context;
    final private MalattieFragment parentFragment;
    final private int layout_single_line;
    final private DatabaseReference dbRootMalattieUtente;

    public ListaMieMalattieAdapter(@NonNull MainActivity context, @NonNull MalattieFragment parentFragment, int layout_single_line, List<String> listaMalattiePersonali, List<String> listaSuggerimentiMalattie){
        this.context = context;
        this.parentFragment = parentFragment;
        this.layout_single_line = layout_single_line;
        this.listaMalattiePersonali = listaMalattiePersonali;
        this.listaSuggerimentiMalattie = listaSuggerimentiMalattie;
        dbRootMalattieUtente = FirebaseDatabase.getInstance().getReference("Utenti").child(context.getUserUid()).child("malattie");
    }

    @Override
    public int getCount() {
        return listaMalattiePersonali.size();
    }
    @Override
    public Object getItem(int position) {
        return listaMalattiePersonali.get(position);
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

        // Imposto il testo per la malattia e i suggerimenti legati alla malattia
        String nomeMalattia = listaMalattiePersonali.get(position);
        TextView textMalattia = convertView.findViewById(R.id.nome_malattia);
        TextView textSuggerimento = convertView.findViewById(R.id.consigli_malattia);
        textMalattia.setText(nomeMalattia);
        textSuggerimento.setText(listaSuggerimentiMalattie.get(position));

        // Imposto il tasto per eliminare la malattia dalla lista delle malattie personali
        ImageButton deleteBtn = convertView.findViewById(R.id.btn_delete);
        deleteBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.malattie_rimuovi_malattia_titolo))
                    .setMessage(context.getString(R.string.malattie_rimuovi_malattia_msg, nomeMalattia))
                    .setPositiveButton(R.string.option_menu_yes, (dialog, which) -> {
                        DatabaseReference dbRootMalattiaSpecifica = dbRootMalattieUtente.child(nomeMalattia);
                        dbRootMalattiaSpecifica.removeValue();
                        Toast.makeText(context, context.getString(R.string.malattie_rimozione_con_successo), Toast.LENGTH_SHORT).show();
                        parentFragment.updateMieMalattie();
                    })
                    .setNegativeButton(R.string.option_menu_no, (dialog, which) -> {});

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        return convertView;
    }

    public void rimpiazzaLista(List<String> nuovaListaMalattie, List<String> nuovaListaSuggerimenti){
        listaMalattiePersonali.clear();
        listaMalattiePersonali.addAll(nuovaListaMalattie);
        listaSuggerimentiMalattie.clear();
        listaSuggerimentiMalattie.addAll(nuovaListaSuggerimenti);
        notifyDataSetChanged();
    }
}
