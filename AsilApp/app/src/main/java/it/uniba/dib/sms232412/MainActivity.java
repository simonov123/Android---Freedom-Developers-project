package it.uniba.dib.sms232412;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if(user == null){
            Intent intent = new Intent(this, EntryActivity.class);
            startActivity(intent);
            finish();
        }

        //betadasostituire
        Button test = findViewById(R.id.testsas);
        test.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, EntryActivity.class);
            startActivity(intent);
            finish();
        });


    }
}