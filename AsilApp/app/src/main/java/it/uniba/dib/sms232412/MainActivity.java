package it.uniba.dib.sms232412;

import androidx.annotation.NonNull;

import android.Manifest;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import it.uniba.dib.sms232412.autentication.EntryActivity;
import it.uniba.dib.sms232412.utils.GestoreRegione;
import it.uniba.dib.sms232412.utils.OptionMenuUtility;
import it.uniba.dib.sms232412.utils.PagerAdapter;

public class MainActivity extends AppCompatActivity implements LocationListener {

    final static private int REQUEST_CODE_FOR_LOCATION = 101;
    private PagerAdapter pagerAdapter;
    private LocationManager locationManager;
    private FirebaseUser user;
    private String userUid;
    private String userRole;
    private OptionMenuUtility menuUtility;
    private DatabaseReference dbRootForUpdate;
    private ValueEventListener valueEventListenerForUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Se non vi è alcun utente loggato, effettuo il logout
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, EntryActivity.class);
            startActivity(intent);
            finish();
        }
        userUid = user.getUid();

        //Gestisco l'action bar della Main Activity
        ActionBar myBar = getSupportActionBar();
        if (myBar != null) {
            myBar.setTitle("Home");
            myBar.setDisplayShowHomeEnabled(true);
            myBar.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_medical_logo));
            myBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.app_bar_color)));
        }

        //Creo l'utility per gestire le scelte del menù di opzioni
        menuUtility = new OptionMenuUtility(this);

        //Effettuo il collegamento fra pagerview e tab per lo slider orizzontale
        TabLayout mainTab = findViewById(R.id.main_tab);
        ViewPager2 mainPager = findViewById(R.id.main_pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        mainPager.setAdapter(pagerAdapter);
        mainPager.setCurrentItem(1, false);

        new TabLayoutMediator(mainTab, mainPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.pager_tab1));
                    tab.setIcon(getDrawable(R.drawable.ic_user_logo));
                    break;
                case 1:
                    tab.setText(getString(R.string.pager_tab2));
                    tab.setIcon(getDrawable(R.drawable.ic_illness_logo));
                    break;
                case 2:
                    tab.setText(getString(R.string.pager_tab3));
                    tab.setIcon(getDrawable(R.drawable.ic_explore_logo));
                    break;
                case 3:
                    tab.setText(getString(R.string.pager_tab4));
                    tab.setIcon(getDrawable(R.drawable.ic_spesa_logo));
                    break;
            }
        }).attach();

        //Imposto le variabili utili per l'eventuale update del profilo
        dbRootForUpdate = FirebaseDatabase.getInstance().getReference("Utenti").child(userUid).child("update");
        valueEventListenerForUpdate = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(boolean.class))) {
                    FloatingActionButton btnAlertUpdate = findViewById(R.id.alert_update_button);
                    btnAlertUpdate.setVisibility(View.VISIBLE);
                    btnAlertUpdate.setOnClickListener(v -> {
                        dbRootForUpdate.setValue(false);
                        reload();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //Gestione del permesso per la posizione del device (NON OBBLIGATORIA)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //ricavo la regione
            getRegion();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionRationale(REQUEST_CODE_FOR_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_FOR_LOCATION);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Aggiungo il listener per ascoltare gli aggiornamenti del profilo
        dbRootForUpdate.addValueEventListener(valueEventListenerForUpdate);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Rimuovo il listener per gli aggiornamenti del profilo
        dbRootForUpdate.removeEventListener(valueEventListenerForUpdate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(userUid).child("ruolo");
        rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ruolo = snapshot.getValue(String.class);
                if (ruolo == null) {
                    return;
                }
                userRole = String.copyValueOf(ruolo.toCharArray());
                if (userRole.equals("admin") || userRole.equals("adminPersonaleSanitario")) {
                    inflater.inflate(R.menu.menu_action_bar_admin, menu);
                } else if (userRole.equals("personaleSanitario")) {
                    inflater.inflate(R.menu.menu_action_bar_medical, menu);
                } else {
                    inflater.inflate(R.menu.menu_action_bar, menu);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return menuUtility.handleOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public String getUserRole() {
        return userRole;
    }

    public String getUserUid() {
        return userUid;
    }

    private void reload() {
        Toast.makeText(this, R.string.option_menu_update_completed, Toast.LENGTH_SHORT).show();
        startActivity(getIntent());
        finish();
    }

    private void showPermissionRationale(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_FOR_LOCATION:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_title_rationale_coarse_location)
                        .setMessage(R.string.permission_text_rationale_coarse_location)
                        .setPositiveButton(R.string.permission_ok, (dialog, which) ->
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE_FOR_LOCATION))
                        .show();
                break;
            default:

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_FOR_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //lettura regione a real time
                    getRegion();
                } else {
                    //rimuovo la regione registrata tra i dati salvati
                    SharedPreferences shared = getSharedPreferences("regione.txt", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.remove("REGIONE");
                    editor.remove("NUM_REGIONE");
                    editor.apply();
                }

        }
    }

    private void getRegion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, MainActivity.this);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if(addresses != null){
                String region = addresses.get(0).getAdminArea();
                SharedPreferences shared = getSharedPreferences("regione.txt", Context.MODE_PRIVATE);
                String previousRegion = shared.getString("REGIONE", "Nessuna");
                if(!region.equals(previousRegion)){
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("REGIONE", region);
                    editor.putInt("NUM_REGIONE", GestoreRegione.getNumberRegion(region));
                    editor.apply();
                    dbRootForUpdate.setValue(true);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        finally {
            /**
             * Disabilitando il seguente comando ho la localizzazione in tempo reale
             * ma ho un maggiore consumo della batteria
             * Abilitandolo, invece, ho un aggiornamento della regione solo all'avvio dell'app
             * però ottengo un risparmio della batteria
             */
            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
    }
}