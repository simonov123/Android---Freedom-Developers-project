package it.uniba.dib.sms232412.sezioneMalattie;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.uniba.dib.sms232412.MainActivity;
import it.uniba.dib.sms232412.R;

public class ListaCercaMalattieAdapter extends BaseAdapter {

    final private String[] listaMalattie;
    final private String[] listaDescrizioneMalattie;
    final private String[] listaURLVideoMalattie;
    final private MainActivity context;
    final private MalattieFragment parentFragment;
    final private int layout_single_line;
    final private DatabaseReference dbRootMalattieUtente;

    public ListaCercaMalattieAdapter(@NonNull MainActivity context, @NonNull MalattieFragment parentFragment, int layout_single_line, String[] listaMalattie,
                                     String[] listaDescrizioneMalattie, String[] listaURLVideoMalattie){
        this.context = context;
        this.parentFragment = parentFragment;
        this.layout_single_line = layout_single_line;
        this.listaMalattie = listaMalattie;
        this.listaDescrizioneMalattie = listaDescrizioneMalattie;
        this.listaURLVideoMalattie = listaURLVideoMalattie;
        dbRootMalattieUtente = FirebaseDatabase.getInstance().getReference("Utenti").child(context.getUserUid()).child("malattie");
    }
    @Override
    public int getCount() {
        return listaMalattie.length;
    }
    @Override
    public Object getItem(int position) {
        return listaMalattie[position];
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

        // Gestione del nome e della descrizione della malattia
        String nomeMalattia = listaMalattie[position];
        TextView textMalattia = convertView.findViewById(R.id.nome_malattia);
        TextView textDescription = convertView.findViewById(R.id.descrizione_malattia);
        textMalattia.setText(nomeMalattia);
        textDescription.setText(listaDescrizioneMalattie[position]);

        // Gestione del video correlato alla malattia
        String urlMyMalattia = listaURLVideoMalattie[position];
        Uri videoUri = Uri.parse(urlMyMalattia);
        VideoView videoMalattia = convertView.findViewById(R.id.video_malattia);
        videoMalattia.setVideoURI(videoUri);
        ImageButton playBtn = convertView.findViewById(R.id.btn_play);
        ImageView thumb = convertView.findViewById(R.id.thumbnail);
        playBtn.setOnClickListener(v -> {
            if(thumb.getVisibility() == View.VISIBLE) thumb.setVisibility(View.INVISIBLE);
            if(videoMalattia.isPlaying()){
                playBtn.setImageResource(R.drawable.ic_play_logo);
                videoMalattia.pause();
            } else {
                playBtn.setImageResource(R.drawable.ic_pause_logo);
                videoMalattia.start();
            }
        });
        videoMalattia.setOnCompletionListener(mp -> playBtn.setImageResource(R.drawable.ic_play_logo));
        videoMalattia.setOnPreparedListener(mp -> {
            playBtn.setImageResource(R.drawable.ic_play_logo);
            thumb.setVisibility(View.VISIBLE);
        });

        // Gestione del tasto aggiungi malattia
        ImageButton confirmBtn = convertView.findViewById(R.id.btn_confirm);
        confirmBtn.setOnClickListener(v -> {
            DatabaseReference dbRootMalattiaSpecifica;
            /**
             * Il percorso nel db su Firebase è registrato nella lingua italiana, a prescindere dalla lingua del device
             */
            if(getCount() == 3){
                switch(position){
                    case 0:
                        dbRootMalattiaSpecifica = dbRootMalattieUtente.child("FEBBRE ALTA");
                        break;
                    case 1:
                        dbRootMalattiaSpecifica = dbRootMalattieUtente.child("IPERTENSIONE");
                        break;
                    default:
                        dbRootMalattiaSpecifica = dbRootMalattieUtente.child("TACHICARDIA");
                        break;
                }
            } else {
                switch(position){
                    case 0:
                        dbRootMalattiaSpecifica = dbRootMalattieUtente.child("FEBBRE ALTA");
                        break;
                    case 1:
                        dbRootMalattiaSpecifica = dbRootMalattieUtente.child("SEPSI PUERPERALE");
                        break;
                    case 2:
                        dbRootMalattiaSpecifica = dbRootMalattieUtente.child("IPERTENSIONE");
                        break;
                    default:
                        dbRootMalattiaSpecifica = dbRootMalattieUtente.child("TACHICARDIA");
                        break;
                }
            }

            dbRootMalattiaSpecifica.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Toast.makeText(context, context.getString(R.string.malattie_gia_possedute), Toast.LENGTH_SHORT).show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(context.getString(R.string.malattie_confermare_malattia, nomeMalattia))
                                .setMessage(listaDescrizioneMalattie[position])
                                .setPositiveButton(R.string.option_menu_yes, (dialog, which) -> {
                                    dbRootMalattiaSpecifica.setValue(true);
                                    Toast.makeText(context, context.getString(R.string.malattie_aggiunta_con_successo), Toast.LENGTH_SHORT).show();
                                    parentFragment.updateMieMalattie();
                                })
                                .setNegativeButton(R.string.option_menu_no, (dialog, which) -> {});

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        return convertView;
    }
}
