package com.example.mayarpg;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();

        EditText etNome = findViewById(R.id.etNome);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etSenha = findViewById(R.id.etSenhaCadastro);
        EditText etConfirmarSenha = findViewById(R.id.etConfirmarSenha);
        Button btnConcluirCadastro = findViewById(R.id.btnConcluirCadastro);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnConcluirCadastro.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();
            String confirmarSenha = etConfirmarSenha.getText().toString().trim();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_campos_obrigatorios), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!senha.equals(confirmarSenha)) {
                Toast.makeText(this, getString(R.string.erro_senhas_diferentes), Toast.LENGTH_SHORT).show();
                return;
            }

            if (senha.length() < 6) {
                Toast.makeText(this, getString(R.string.erro_senha_curta), Toast.LENGTH_SHORT).show();
                return;
            }

            createFirebaseAccount(nome, email, senha);
        });

        tvLogin.setOnClickListener(v -> finish());
    }

    // Cria conta no Firebase e envia automaticamente e-mail de verificacao.
    private void createFirebaseAccount(String nome, String email, String senha) {
        firebaseAuth.createUserWithEmailAndPassword(email.trim().toLowerCase(), senha)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                        Exception exception = task.getException();
                        Toast.makeText(this, resolveCadastroErrorMessage(exception), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nome)
                            .build();

                    task.getResult().getUser().updateProfile(request);
                    task.getResult().getUser().sendEmailVerification();
                    firebaseAuth.signOut();

                    Toast.makeText(this, getString(R.string.cadastro_sucesso_email), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    private String resolveCadastroErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return getString(R.string.erro_senha_curta);
        }
        if (exception instanceof FirebaseAuthUserCollisionException) {
            return getString(R.string.erro_email_em_uso);
        }
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return getString(R.string.erro_email_invalido);
        }
        return getString(R.string.cadastro_falhou);
    }
}
