package com.example.legokp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.legokp.R;
import com.example.legokp.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Проверка авторизации
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupTabs();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("LEGO Catalog");
        }

        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Sets"));
        tabLayout.addTab(tabLayout.newTab().setText("Minifigs"));
        tabLayout.addTab(tabLayout.newTab().setText("Favorites"));

        // Показать первый фрагмент
        loadFragment(new SetsFragment());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new SetsFragment();
                        break;
                    case 1:
                        fragment = new MinifigsFragment();
                        break;
                    case 2:
                        fragment = new FavoritesFragment();
                        break;
                }
                if (fragment != null) {
                    loadFragment(fragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            handleLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleLogout() {
        sessionManager.clearSession();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}