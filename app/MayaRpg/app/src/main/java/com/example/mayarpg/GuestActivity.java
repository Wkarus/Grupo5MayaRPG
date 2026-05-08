package com.example.mayarpg;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GuestActivity extends AppCompatActivity {

    // Deixe o numero vazio por enquanto. Voce me envia depois.
    private static final String WHATSAPP_NUMBER = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        Button btnContatoWhatsapp = findViewById(R.id.btnContatoWhatsapp);
        btnContatoWhatsapp.setOnClickListener(v -> openWhatsapp());
    }

    private void openWhatsapp() {
        if (WHATSAPP_NUMBER.isEmpty()) {
            Toast.makeText(this, getString(R.string.numero_whatsapp_nao_configurado), Toast.LENGTH_SHORT).show();
            return;
        }

        String mensagem = "Ola Maya! Tenho interesse no RPG de fisioterapia.";
        String url = "https://wa.me/" + WHATSAPP_NUMBER + "?text=" + Uri.encode(mensagem);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
