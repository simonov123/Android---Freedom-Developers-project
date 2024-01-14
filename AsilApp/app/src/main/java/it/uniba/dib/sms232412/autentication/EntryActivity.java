package it.uniba.dib.sms232412.autentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.uniba.dib.sms232412.MainActivity;
import it.uniba.dib.sms232412.R;

public class EntryActivity extends AppCompatActivity implements LoginFragment.ChangePanelListener {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        ActionBar myBar = getSupportActionBar();
        if(myBar != null){
            myBar.setDisplayHomeAsUpEnabled(true);
            myBar.setHomeButtonEnabled(true);
            myBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.app_bar_color)));
        }
        mAuth = FirebaseAuth.getInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.frame_entry, new LoginFragment());
        ft.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void ChangePanel() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_entry, new RegisterFragment());
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack();
            fm.executePendingTransactions();
            return true;
        }
        return false;
    }
}