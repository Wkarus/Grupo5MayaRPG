package com.example.mayarpg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mayarpg.network.ApiClient;
import com.example.mayarpg.network.SessionManager;
import com.example.mayarpg.network.services.AuthService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQ_NOTIFICATIONS = 1402;

    private FirebaseAuth firebaseAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        requestNotificationPermissionIfNeeded();

        EditText etUsuario = findViewById(R.id.etUsuario);
        EditText etSenha = findViewById(R.id.etSenha);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnVisitante = findViewById(R.id.btnVisitante);
        TextView tvCadastro = findViewById(R.id.tvCadastro);

        // Se o Firebase ainda tem sessao, so preenche o e-mail (senha o usuario digita e toca em Login).
        FirebaseUser jaLogado = firebaseAuth.getCurrentUser();
        if (jaLogado != null && jaLogado.getEmail() != null) {
            etUsuario.setText(jaLogado.getEmail());
        }

        // Login real com Firebase Authentication (Email/Senha).
        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim().toLowerCase();
            String senha = etSenha.getText().toString().trim();

            if (usuario.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_campos_obrigatorios), Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(usuario, senha)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, getString(R.string.login_falhou), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this, getString(R.string.login_falhou), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!user.isEmailVerified()) {
                            user.sendEmailVerification();
                            firebaseAuth.signOut();
                            Toast.makeText(this, getString(R.string.email_nao_verificado), Toast.LENGTH_LONG).show();
                            return;
                        }

                        loginBackendAndOpenHome(usuario, senha, user);
                    });
        });

        tvCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        btnVisitante.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuestActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 1) Tenta JWT via Firebase ID token ({@code POST /auth/firebase}) — cria/sync do utilizador na MySQL.<br>
     * 2) Se o servidor nao tiver Firebase Admin (503), cai para login email/senha na API.<br>
     * 3) Falhas de rede mostram mensagem com URL em modo debug.
     */
    private void loginBackendAndOpenHome(String email, String senha, FirebaseUser firebaseUser) {
        firebaseUser.getIdToken(false).addOnCompleteListener(taskToken -> {
            if (!taskToken.isSuccessful() || taskToken.getResult() == null) {
                Exception e = taskToken.getException();
                Log.w(TAG, "Firebase getIdToken falhou", e);
                loginBackendWithPassword(email, senha, firebaseUser);
                return;
            }
            String idToken = taskToken.getResult().getToken();
            if (idToken == null || idToken.isEmpty()) {
                loginBackendWithPassword(email, senha, firebaseUser);
                return;
            }

            ApiClient.authService(this)
                    .loginWithFirebase(new AuthService.FirebaseLoginRequest(idToken))
                    .enqueue(new Callback<AuthService.LoginResponse>() {
                        @Override
                        public void onResponse(Call<AuthService.LoginResponse> call,
                                              Response<AuthService.LoginResponse> response) {
                            if (response.code() == 503) {
                                // Servidor sem credenciais Firebase Admin: modo legado.
                                loginBackendWithPassword(email, senha, firebaseUser);
                                return;
                            }
                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().token != null) {
                                onBackendLoginSuccess(response.body(), firebaseUser);
                                return;
                            }
                            if (response.code() == 403) {
                                Toast.makeText(MainActivity.this,
                                        "Email ainda nao verificado no servidor.",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            Toast.makeText(MainActivity.this,
                                    "Login backend falhou.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<AuthService.LoginResponse> call, Throwable t) {
                            Log.e(TAG, "Falha de rede POST /auth/firebase", t);
                            showConnectError(t);
                        }
                    });
        });
    }

    private void loginBackendWithPassword(String email, String senha, FirebaseUser firebaseUser) {
        ApiClient.authService(this)
                .login(new AuthService.LoginRequest(email, senha))
                .enqueue(new Callback<AuthService.LoginResponse>() {
                    @Override
                    public void onResponse(Call<AuthService.LoginResponse> call,
                                          Response<AuthService.LoginResponse> response) {
                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().token == null) {
                            Toast.makeText(MainActivity.this,
                                    "Login backend falhou.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        onBackendLoginSuccess(response.body(), firebaseUser);
                    }

                    @Override
                    public void onFailure(Call<AuthService.LoginResponse> call, Throwable t) {
                        Log.e(TAG, "Falha de rede POST /auth/login", t);
                        showConnectError(t);
                    }
                });
    }

    private void onBackendLoginSuccess(AuthService.LoginResponse body, FirebaseUser firebaseUser) {
        sessionManager.saveToken(body.token);
        openHome(firebaseUser);
    }

    private void showConnectError(Throwable t) {
        if (BuildConfig.DEBUG) {
            String msg = "API " + BuildConfig.API_BASE_URL + "\n" + (t.getMessage() != null ? t.getMessage() : "rede");
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        }
        if (isLocalDevApiUrl(BuildConfig.API_BASE_URL)) {
            Toast.makeText(this, getString(R.string.erro_api_apk_desenvolvimento), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, getString(R.string.erro_api_offline), Toast.LENGTH_LONG).show();
    }

    private static boolean isLocalDevApiUrl(String url) {
        if (url == null) {
            return false;
        }
        String normalized = url.toLowerCase();
        return normalized.contains("10.0.2.2")
                || normalized.contains("localhost")
                || normalized.contains("127.0.0.1");
    }

    private void openHome(FirebaseUser user) {
        String nome = user.getDisplayName();
        if (nome == null || nome.trim().isEmpty()) {
            nome = user.getEmail() != null ? user.getEmail() : "Paciente";
        }

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("nome_usuario", nome);
        startActivity(intent);
        finish();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
    }
}
