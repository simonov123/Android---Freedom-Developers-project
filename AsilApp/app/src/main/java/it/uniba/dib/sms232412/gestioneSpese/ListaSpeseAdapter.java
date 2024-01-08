package it.uniba.dib.sms232412.gestioneSpese;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import it.uniba.dib.sms232412.R;

public class ListaSpeseAdapter extends BaseAdapter {

    final private List<Spesa> listaSpese;
    final private Context context;
    final private int layout_single_line;
    final private GestoreSpesaFragment parentFragment;

    public ListaSpeseAdapter(@NonNull Context context, int layout_single_line, List<Spesa> spese, GestoreSpesaFragment parentFragment) {
        this.context = context;
        this.listaSpese = spese;
        this.layout_single_line = layout_single_line;
        this.parentFragment = parentFragment;
    }

    @Override
    public int getCount() {
        return listaSpese.size();
    }

    @Override
    public Object getItem(int position) {
        return listaSpese.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout_single_line, parent, false);
        }
        Spesa mySpesa = listaSpese.get(position);

        TextView textDataSpesa = convertView.findViewById(R.id.data_spesa);
        TextView textTipoSpesa = convertView.findViewById(R.id.tipo_spesa);
        TextView textCostoSpesa = convertView.findViewById(R.id.costo_spesa);

        Resources res = context.getResources();
        textDataSpesa.setText(res.getString(R.string.spese_elemento_lista_data, mySpesa.getData()));
        textTipoSpesa.setText(res.getString(R.string.spese_elemento_lista_tipo, mySpesa.getTipologia()));
        textCostoSpesa.setText(res.getString(R.string.spese_elemento_lista_costo, mySpesa.getCosto()));

        ImageButton btn_delete = convertView.findViewById((R.id.btn_delete));
        btn_delete.setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser() == null){
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle(R.string.spese_delete_spesa_titolo)
                    .setMessage(R.string.spese_delete_spesa_content)
                    .setPositiveButton(R.string.option_menu_yes, (dialog, which) -> {
                        // Azione da eseguire quando l'utente fa clic su "Sì"
                        String my_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(my_uid).child("spese");
                        rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    GenericTypeIndicator<List<Spesa>> typeIndicator = new GenericTypeIndicator<List<Spesa>>() {};
                                    List<Spesa> listaPresente = snapshot.getValue(typeIndicator);
                                    if(listaPresente != null && position<listaPresente.size()){
                                        listaPresente.remove(position);
                                        rootDB.setValue(listaPresente);
                                        if(parentFragment != null){
                                            parentFragment.reloadList();
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    })
                    .setNegativeButton(R.string.option_menu_no, (dialog, which) -> {
                        // Azione da eseguire quando l'utente fa clic su "No"
                        dialog.dismiss();
                    })
                    .show();
        });

        return convertView;
    }
}
