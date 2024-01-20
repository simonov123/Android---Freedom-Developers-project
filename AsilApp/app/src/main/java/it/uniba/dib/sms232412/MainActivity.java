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
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import it.uniba.dib.sms232412.utils.Utente;

public class MainActivity extends AppCompatActivity implements LocationListener {

    final static private int REQUEST_CODE_FOR_LOCATION_STARTUP = 101;
    final static private int REQUEST_CODE_FOR_LOCATION_EXPLORE = 102;
    final static private int REQUEST_CODE_FOR_LOCATION_ACTION_BAR = 103;
    private LocationManager locationManager;
    private double latitudine = 0.0;
    private double longitudine = 0.0;
    private String userUid, userRole, userName, userSurname, userSesso;
    private OptionMenuUtility menuUtility;
    private DatabaseReference dbRootForUpdate;
    private ValueEventListener valueEventListenerForUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * All'inizio rendo non interagibile l'activity sin quando non recupero tutti i dati
         * necessari dal database alla creazione del menù di opzioni (in onCreateOptionsMenu()).
         * Il recupero dei dati dal Database richiede qualche secondo, e un'interazione istantanea
         * con l'activity prima di tale recupero può portare ad un crash dell'app.
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        /**
         * Se non vi è alcun utente loggato, effettuo il logout
         */
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, EntryActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        userUid = user.getUid();
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        /**
         * Gestione dell'action bar relativa alla Main Activity
         */
        ActionBar myBar = getSupportActionBar();
        if (myBar != null) {
            myBar.setTitle(" HOME");
            myBar.setDisplayShowHomeEnabled(true);
            myBar.setIcon(AppCompatResources.getDrawable(this, R.drawable.asilapp_icon));
            myBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.app_bar_color)));
        }

        /**
         * Creazione dell'utility per gestire le scelte del menù di opzioni
         */
        menuUtility = new OptionMenuUtility(this);

        /**
         * Collegamento fra pagerView e Tab per la creazione dello slider orizzontale
         * utilizzabile sia selezionando il tab che scrollando orizzontalmente le pagine
         */
        TabLayout mainTab = findViewById(R.id.main_tab);
        ViewPager2 mainPager = findViewById(R.id.main_pager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        mainPager.setAdapter(pagerAdapter);
        mainPager.setCurrentItem(0, false);

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

        /**
         * Impostazione delle variabili necessarie per l'eventuale update del profilo
         */
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
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        /**
         * Gestione del permesso all'avvio per la posizione del device (NON OBBLIGATORIA PER USARE L'APP)
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getRegion(); //ricavo la regione
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionRationale(REQUEST_CODE_FOR_LOCATION_STARTUP);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_FOR_LOCATION_STARTUP);
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
        DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(userUid);
        rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Utente utente = snapshot.getValue(Utente.class);
                if (utente == null) {
                    return;
                }
                userRole = utente.getRuolo();
                userName = utente.getNome();
                userSurname = utente.getCognome();
                userSesso = utente.getSesso();
                if (userRole.equals("admin") || userRole.equals("adminPersonaleSanitario")) {
                    inflater.inflate(R.menu.menu_action_bar_admin, menu);
                } else if (userRole.equals("personaleSanitario")) {
                    inflater.inflate(R.menu.menu_action_bar_medical, menu);
                } else {
                    inflater.inflate(R.menu.menu_action_bar, menu);
                }
                /**
                 * Riabilito l'interazione con l'activity
                 */
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.option_menu_map){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showPermissionRationale(REQUEST_CODE_FOR_LOCATION_ACTION_BAR);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_FOR_LOCATION_ACTION_BAR);
                }
            } else {
                activateGps(); //se non ho il gps attivato, procedo con l'attivazione
                callMaps();
            }
            return true;
        }
        return menuUtility.handleOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public String getUserRole() {
        return userRole;
    }
    public String getUserUid() {
        return userUid;
    }
    public String getUserName(){
        return userName;
    }
    public String getUserSurname(){
        return userSurname;
    }
    public String getUserSesso(){
        return userSesso;
    }

    /**
     * Metodo che serve per ricaricare l'activity a seguito di un aggiornamento
     */
    public void reload() {
        Toast.makeText(this, R.string.option_menu_update_completed, Toast.LENGTH_SHORT).show();
        startActivity(getIntent());
        finish();
    }

    private void showPermissionRationale(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_FOR_LOCATION_EXPLORE:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_title_rationale_coarse_location)
                        .setMessage(R.string.permission_text_rationale_coarse_location)
                        .setPositiveButton(R.string.permission_ok, (dialog, which) ->
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE_FOR_LOCATION_EXPLORE))
                        .show();
                break;
            case REQUEST_CODE_FOR_LOCATION_STARTUP:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_title_rationale_coarse_location)
                        .setMessage(R.string.permission_text_rationale_coarse_location)
                        .setPositiveButton(R.string.permission_ok, (dialog, which) ->
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE_FOR_LOCATION_STARTUP))
                        .show();
                break;
            case REQUEST_CODE_FOR_LOCATION_ACTION_BAR:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_title_rationale_coarse_location)
                        .setMessage(R.string.permission_text_rationale_coarse_location)
                        .setPositiveButton(R.string.permission_ok, (dialog, which) ->
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE_FOR_LOCATION_ACTION_BAR))
                        .show();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_FOR_LOCATION_STARTUP:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    activateGps(); //se non ho il gps attivato, vado all'attivazione
                    getRegion(); //scansione della regione
                } else {
                    //rimuovo la regione registrata tra i dati salvati se all'avvio non ho il permesso
                    SharedPreferences shared = getSharedPreferences("regione.txt", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.remove("REGIONE");
                    editor.remove("NUM_REGIONE");
                    editor.apply();
                }
                break;
            case REQUEST_CODE_FOR_LOCATION_EXPLORE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    activateGps(); //se non ho il gps attivato, vado all'attivazione
                    getRegion(); //scansione della regione

                    //la ricerca della regione impiega qualche secondo, quindi mostro un toast informativo
                    Toast.makeText(this, getString(R.string.info_btn_search_for_region_ok_alert), Toast.LENGTH_SHORT).show();
                } else {
                    //mostro un banner per informare l'utente che serve dare il permesso
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.info_btn_search_for_region_denied_title)
                            .setMessage(R.string.info_btn_search_for_region_denied_msg)
                            .setPositiveButton(R.string.permission_ok, (dialog, which) -> {})
                            .show();
                }
                break;
            case REQUEST_CODE_FOR_LOCATION_ACTION_BAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    activateGps(); //se non ho il gps attivato, vado all'attivazione
                    getRegion();
                    callMaps();
                }
                break;
        }
    }

    /**
     * Metodo per la lettura della regione una volta avuto il permesso per la geolocalizzazione
     */
    private void getRegion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            // Attivo la ricerca in tempo reale della regione di appartenenza
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, MainActivity.this);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Metodo invocato quando il locationManager legge una posizione
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            latitudine = location.getLatitude();
            longitudine = location.getLongitude();
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
        // Se ancora attiva, disattivo la ricerca in tempo reale della regione di appartenenza
        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
    }

    /**
     * Metodo invocato quando l'utente accede manualmente alla funzionalità che richiede la posizione.
     * Tale metodo non viene invocato all'avvio dell'app e viene considerato come richiesta diversa
     * per via del fatto che in caso di mancato consenso da parte dell'utente viene mostrato un banner
     * di avviso che non si vuole mostrare ad ogni avvio dell'app.
     * L'invocazione avviene solo nel caso in cui non ho ancora il permesso per accedere alla posizione
     * o se ho già il permesso ma il gps non è stato attivato manualmente
     */
    public void askExplicitPermissionForLocationInInfo(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionRationale(REQUEST_CODE_FOR_LOCATION_EXPLORE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_FOR_LOCATION_EXPLORE);
            }
        } else {
            activateGps(); //se ho già il permesso ma non ho il gps attivato, procedo con l'attivazione
        }
    }

    /**
     * Metodo per attivare manualmente il gps
     */
    private void activateGps(){
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent activateGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(activateGpsIntent);
        }
    }

    /**
     * Metodo per richiamare il servizio di mapping (tramite l'icona di sistema apposita)
     */
    private void callMaps(){
        // L'utente prima decide cosa cercare
        String[] scelte_possibili = getResources().getStringArray(R.array.maps_luoghi_interesse);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.maps_domanda))
                .setItems(scelte_possibili, (dialog, which) -> {
                    // L'utente ha selezionato un'opzione
                    String scelta = scelte_possibili[which];

                    // Viene chiamato il servizio di mapping per cercare le località richieste
                    // Nelle vicinanze se si sono dati i permessi per la posizione
                    String uriString = "geo:" + latitudine + "," + longitudine + "?q=" + scelta;
                    Uri uri = Uri.parse(uriString);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    Intent myChooser = Intent.createChooser(intent, getString(R.string.maps_chooser_scegli_app));
                    startActivity(myChooser);
                });
        builder.show();
    }
}