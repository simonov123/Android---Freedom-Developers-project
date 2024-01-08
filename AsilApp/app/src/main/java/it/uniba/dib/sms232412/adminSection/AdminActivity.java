package it.uniba.dib.sms232412.adminSection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms232412.R;

public class AdminActivity extends AppCompatActivity {

    final private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //Gestisco l'action bar della Main Activity
        ActionBar myBar = getSupportActionBar();
        if (myBar != null) {
            myBar.setTitle(getString(R.string.admin_page_title));
            myBar.setDisplayShowHomeEnabled(true);
            myBar.setDisplayHomeAsUpEnabled(true);
            myBar.setHomeButtonEnabled(true);
            myBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.app_bar_color)));
        }

        //Gestisco la lista delle richieste in sospeso
        TextView titleAdminPage = findViewById(R.id.admin_list_title);
        ListView listPendantRequests = findViewById(R.id.admin_request_list);
        DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("RichiesteRegistrazioneComePersonaleSanitario");
        rootDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<RichiestaPerDiventarePS> myList = new ArrayList<>();
                    for(DataSnapshot childSnapshot : snapshot.getChildren()){
                        myList.add(childSnapshot.getValue(RichiestaPerDiventarePS.class));
                    }
                    if(myList.size() > 0){
                        ListaRichiestePerDiventarePSAdapter adapter = new ListaRichiestePerDiventarePSAdapter(context, R.layout.lista_richieste_per_diventare_ps_single_element_layout,
                                myList, (AdminActivity) context);
                        listPendantRequests.setAdapter(adapter);
                    }
                } else {
                    titleAdminPage.setText(getString(R.string.admin_list_request_empty));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return false;
    }

    public void reloadPage(){
        startActivity(getIntent());
        finish();
    }
}