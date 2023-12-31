package it.uniba.dib.sms232412.utils;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.autentication.EntryActivity;

public class OptionMenuUtility {

    final private AppCompatActivity activity;
    public OptionMenuUtility(AppCompatActivity activity){
        this.activity = activity;
    }

    public boolean handleOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.option_menu_logout){
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(activity, R.string.option_menu_logout_confirm, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, EntryActivity.class);
            activity.startActivity(intent);
            activity.finish();
            return true;
        }
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

                                            // Elimino i dati dell'utente dal DB Firebase
                                            DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(user.getUid());
                                            rootDB.removeValue();

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
        }
        return false;
    }
}
