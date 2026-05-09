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
            // Primeira entrada na Home (depois do login): abre Inicio.
            bottomNavigation.setSelectedItemId(R.id.nav_home);
            openFragment(HomeFragment.newInstance(nomeUsuario));
        } else {
            // App restaurado (voltou dos recentes / sistema matou o processo):
            // deixa a barra e o FragmentManager como estavam; so garante que o fragment bate com a aba.
            bottomNavigation.post(() -> syncFragmentToSelectedTab(bottomNavigation, nomeUsuario));
        }
    }

    private void syncFragmentToSelectedTab(BottomNavigationView nav, String nomeUsuario) {
        int id = nav.getSelectedItemId();
        if (id == R.id.nav_home) {
            openFragment(HomeFragment.newInstance(nomeUsuario));
        } else if (id == R.id.nav_schedule) {
            openFragment(new ScheduleFragment());
        } else if (id == R.id.nav_exercises) {
            openFragment(new ExercisesFragment());
        } else if (id == R.id.nav_user) {
            openFragment(UserFragment.newInstance(nomeUsuario));
        }
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
