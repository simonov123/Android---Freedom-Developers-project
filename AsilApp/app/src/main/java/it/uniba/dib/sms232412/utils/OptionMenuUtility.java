package it.uniba.dib.sms232412.utils;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.uniba.dib.sms232412.adminSection.AdminActivity;
import it.uniba.dib.sms232412.MainActivity;
import it.uniba.dib.sms232412.personaleSanitario.PersonaleSanitarioActivity;
import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.autentication.EntryActivity;
import it.uniba.dib.sms232412.valigettaMedica.ValigettaActivity;

//Questa classe di utility è per il menù di opzioni della Main Activity
public class OptionMenuUtility {

    final private MainActivity activity;
    public OptionMenuUtility(MainActivity activity){
        this.activity = activity;
    }

    public boolean handleOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        //OPZIONE PER EFFETTUARE IL LOGOUT
        if(id == R.id.option_menu_logout){
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(activity, R.string.option_menu_logout_confirm, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, EntryActivity.class);
            activity.startActivity(intent);
            activity.finish();
            return true;
        }

        //OPZIONE PER ELIMINARE L'ACCOUNT
        if(id == R.id.option_menu_delete_account){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.option_menu_delete_account_title)
                        .setMessage(R.string.option_menu_delete_account_msg)
                        .setPositiveButton(R.string.option_menu_yes, (dialog, which) -> {
                            // L'utente ha confermato, procedi con l'eliminazione dell'account
                            user.delete()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Eliminazione dell'account completata con successo
                                            // L'utente sarà automaticamente disconnesso
                                            Toast.makeText(activity, R.string.option_menu_delete_success, Toast.LENGTH_SHORT).show();

                                            // Elimino tutti i dati dell'utente dal DB Firebase
                                            String myUid = user.getUid();
                                            DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(myUid);
                                            rootDB.removeValue();
                                            DatabaseReference rootDBReqPS = FirebaseDatabase.getInstance().getReference("RichiesteRegistrazioneComePersonaleSanitario").child(myUid);
                                            rootDBReqPS.removeValue();
                                            DatabaseReference rootDBPS = FirebaseDatabase.getInstance().getReference("PersonaleSanitario").child(myUid);
                                            rootDBPS.removeValue();

                                            Intent intent = new Intent(activity, EntryActivity.class);
                                            activity.startActivity(intent);
                                            activity.finish();

                                        } else {
                                            // Si è verificato un errore durante l'eliminazione dell'account
                                            Toast.makeText(activity, R.string.option_menu_delete_failed, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton(R.string.option_menu_no, (dialog, which) -> {
                            // L'utente ha scelto di non eliminare l'account, chiudi il dialog
                            dialog.dismiss();
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return true;
        }

        //OPZIONE PER ACCEDERE ALLA SEZIONE DEL PERSONALE SANITARIO
        if(id == R.id.option_menu_personale_sanitario){
            Intent intent = new Intent(activity, PersonaleSanitarioActivity.class);
            intent.putExtra("UID", activity.getUserUid());
            intent.putExtra("ROLE", activity.getUserRole());
            intent.putExtra("NOME", activity.getUserName());
            intent.putExtra("COGNOME", activity.getUserSurname());
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.anim_enter_from_top, R.anim.anim_exit_to_bottom);
            return true;
        }

        //OPZIONE PER ACCEDERE ALLA VALIGETTA MEDICA
        if(id == R.id.option_menu_valigetta){
            Intent intent = new Intent(activity, ValigettaActivity.class);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.anim_enter_from_top, R.anim.anim_exit_to_bottom);
            return true;
        }

        //OPZIONE PER ACCEDERE ALLA SEZIONE AMMINISTRATORE (riservato agli admin)
        if(id == R.id.option_menu_admin_page){
            Intent intent = new Intent(activity, AdminActivity.class);
            activity.startActivity(intent);
            return true;
        }

        //OPZIONE PER AGGIORNARE LA HOME PAGE
        if(id == R.id.option_menu_update){
            activity.reload();
            return true;
        }

        return false;
    }
}
