package it.uniba.dib.sms232412;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.uniba.dib.sms232412.autentication.EntryActivity;
import it.uniba.dib.sms232412.utils.OptionMenuUtility;
import it.uniba.dib.sms232412.utils.PagerAdapter;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private OptionMenuUtility menuUtility;
    private TabLayout mainTab;
    private ViewPager2 mainPager;
    private PagerAdapter pagerAdapter;

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

        menuUtility = new OptionMenuUtility(this);
        //collegamento fra pagerview e tab
        mainTab = findViewById(R.id.main_tab);
        mainPager = findViewById(R.id.main_pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        mainPager.setAdapter(pagerAdapter);
        mainPager.setCurrentItem(1,false);

        new TabLayoutMediator(mainTab, mainPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.pager_tab1));
                    break;
                case 1:
                    tab.setText(getString(R.string.pager_tab2));
                    break;
                case 2:
                    tab.setText(getString(R.string.pager_tab3));
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return menuUtility.handleOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}