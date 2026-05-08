package com.example.mayarpg;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String nomeUsuario = getIntent().getStringExtra("nome_usuario");

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                openFragment(HomeFragment.newInstance(nomeUsuario));
                return true;
            } else if (id == R.id.nav_schedule) {
                openFragment(new ScheduleFragment());
                return true;
            } else if (id == R.id.nav_exercises) {
                openFragment(new ExercisesFragment());
                return true;
            } else if (id == R.id.nav_user) {
                openFragment(UserFragment.newInstance(nomeUsuario));
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            // Define a Home como pagina inicial e garante estado visual correto da navegacao.
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
