package it.uniba.dib.sms232412.valigettaMedica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import it.uniba.dib.sms232412.R;
import it.uniba.dib.sms232412.utils.Utente;

public class ValigettaActivity extends AppCompatActivity {

    private FragmentManager fragManager;
    private Utente utenteSelezionato;
    private int actualStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Base_Theme_AsilApp);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valigetta);

        fragManager = getSupportFragmentManager();

        ActionBar myBar = getSupportActionBar();
        if(myBar != null){
            myBar.setTitle(getString(R.string.valigia_title));
            myBar.setDisplayShowHomeEnabled(true);
            myBar.setDisplayHomeAsUpEnabled(true);
            myBar.setHomeButtonEnabled(true);
            myBar.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_medical_logo));
            myBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.app_bar_color)));
        }

        //Ricavo i dati dell'utente dall'intent usato per chiamare questa activity
        String userUid = getIntent().getStringExtra("UID");
        String userRole = getIntent().getStringExtra("ROLE");

        FragmentTransaction ft = fragManager.beginTransaction();
        ft.replace(R.id.valigia_main_frame, new ValigettaStep1TrovaUtenteFragment());
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            switch(actualStep){
                case 1:
                    finish();
                    break;
                case 2:

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

    public void changeToStep(int step){
        actualStep = step;

    }

    public void setChosenUser(Utente user){
        utenteSelezionato = user;
    }
}