package it.uniba.dib.sms232412.personaleSanitario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.uniba.dib.sms232412.R;

public class PersonaleSanitarioActivity extends AppCompatActivity
        implements PersonaleSanitarioCreaRichiestaFragment.CompleteListener,
        PersonaleSanitarioModificaRichiestaFragment.CompleteListener,
        PersonaleSanitarioModificaStatusPSFragment.CompleteListener {

    private String userRole;
    private String userNome;
    private String userCognome;
    private boolean isStartModeActive = true;
    private boolean isRequestPendant = false;
    private Button config_button;
    private FragmentManager fragManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personale_sanitario);

        fragManager = getSupportFragmentManager();

        ActionBar myBar = getSupportActionBar();
        if(myBar != null){
            myBar.setTitle(getString(R.string.ps_titolo));
            myBar.setDisplayShowHomeEnabled(true);
            myBar.setDisplayHomeAsUpEnabled(true);
            myBar.setHomeButtonEnabled(true);
            myBar.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_healthcare_logo));
            myBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.app_bar_color)));
        }

        //Ricavo i dati dell'utente dall'intent usato per chiamare questa activity
        Intent callingIntent = getIntent();
        String userUid = callingIntent.getStringExtra("UID");
        userRole = callingIntent.getStringExtra("ROLE");
        userNome = callingIntent.getStringExtra("NOME");
        userCognome = callingIntent.getStringExtra("COGNOME");

        //In base al ruolo gestisco diversamente il bottone per la registrazione come personale sanitario
        config_button = findViewById(R.id.ps_btn_config);
        if(userUid !=null && !userRole.equals("personaleSanitario") && !userRole.equals("adminPersonaleSanitario")){
            DatabaseReference dbRootToCheckPSRequest = FirebaseDatabase.getInstance().getReference("RichiesteRegistrazioneComePersonaleSanitario").child(userUid);
            dbRootToCheckPSRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        isRequestPendant = true;
                        config_button.setText(R.string.ps_button_modify_request_of_register_as_ps);
                        config_button.setOnClickListener(null);
                        config_button.setOnClickListener(v -> makeUndoConfig());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        makeStartConfig();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if(isStartModeActive){
                finish();
            } else {
                makeStartConfig();
            }
            return true;
        }
        return false;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_enter_from_bottom, R.anim.anim_exit_to_top);
    }

    // Metodo per creare la configurazione di partenza per l'activity
    @Override
    public void makeStartConfig(){
        FragmentTransaction ft = fragManager.beginTransaction();
        if(!isStartModeActive){
            ft.setCustomAnimations(R.anim.anim_enter_from_left, R.anim.anim_exit_to_right);
        }
        ft.replace(R.id.ps_main_frame, new PersonaleSanitarioListaFragment());
        ft.commit();
        isStartModeActive = true;
        config_button.setOnClickListener(null);
        if(userRole.equals("personaleSanitario") || userRole.equals("adminPersonaleSanitario")){
            config_button.setText(R.string.ps_button_modify_setting_as_ps);
            config_button.setOnClickListener(v -> makeUndoConfig());
        } else if(isRequestPendant){
            config_button.setText(R.string.ps_button_modify_request_of_register_as_ps);
            config_button.setOnClickListener(v -> makeUndoConfig());
        } else {
            config_button.setText(R.string.ps_button_register_as_ps);
            config_button.setOnClickListener(v -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.ps_are_you_doctor_title)
                        .setMessage(R.string.ps_are_you_doctor_msg)
                        .setPositiveButton(R.string.option_menu_yes, (dialog, which) -> makeUndoConfig())
                        .setNegativeButton(R.string.option_menu_no, (dialog, which) -> {

                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            });
        }
    }

    // Metodo per creare la configurazione specifica dell'activity
    // in base al fatto se l'utente ha già fatto o meno richiesta
    // e se fa già parte del personale sanitario
    private void makeUndoConfig(){
        isStartModeActive = false;
        config_button.setOnClickListener(null);
        config_button.setText(R.string.ps_button_annulla_config);
        FragmentTransaction ft = fragManager.beginTransaction();
        ft.setCustomAnimations(R.anim.anim_enter_from_right, R.anim.anim_exit_to_left);
        if(userRole.equals("personaleSanitario") || userRole.equals("adminPersonaleSanitario")){
            ft.replace(R.id.ps_main_frame, new PersonaleSanitarioModificaStatusPSFragment());
        } else if(isRequestPendant){
            ft.replace(R.id.ps_main_frame, new PersonaleSanitarioModificaRichiestaFragment());
        } else {
            ft.replace(R.id.ps_main_frame, new PersonaleSanitarioCreaRichiestaFragment());
        }
        ft.commit();
        config_button.setOnClickListener(v -> makeStartConfig());
    }

    @Override
    public void setRequestPendant(boolean isPendant) {
        isRequestPendant = isPendant;
    }

    public String getNomeCompletoUtente(){
        return(userNome + " " + userCognome);
    }
}