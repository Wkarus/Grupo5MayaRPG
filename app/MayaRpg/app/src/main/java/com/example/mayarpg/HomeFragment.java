package com.example.mayarpg;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mayarpg.network.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private static final String KEY_NOME = "key_nome";
    private static final String PREFS_HOME = "mayarpg_home";
    private static final String PREF_KEY_REPLY_PREFIX = "maya_reply_";

    public static HomeFragment newInstance(String nomeUsuario) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(KEY_NOME, nomeUsuario == null ? "Paciente" : nomeUsuario);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvBoasVindas = view.findViewById(R.id.tvBoasVindas);
        ImageButton btnLogoutTop = view.findViewById(R.id.btnLogoutTop);

        String nome = resolveNomeCliente();

        tvBoasVindas.setText(getString(R.string.bem_vindo, nome));

        TextView tvMensagemMaya = view.findViewById(R.id.tvMensagemMaya);
        tvMensagemMaya.setText(getString(R.string.maya_mensagem_cliente, nome));

        btnLogoutTop.setOnClickListener(v -> showLogoutDialog());

        setupMayaReply(view);
        SessionBookingFirestore.fetchAndCacheLocal(requireContext(), () -> {
            if (isAdded() && getView() != null) {
                refreshProximaSessaoUi(getView());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v != null) {
            SessionBookingFirestore.fetchAndCacheLocal(requireContext(), () -> {
                if (isAdded() && getView() != null) {
                    refreshProximaSessaoUi(getView());
                }
            });
        }
    }

    private void refreshProximaSessaoUi(View root) {
        TextView tv = root.findViewById(R.id.tvProximaSessaoResumo);
        if (tv == null || getContext() == null) {
            return;
        }
        if (SessionBookingPreferences.hasBooking(requireContext())) {
            String summary = SessionBookingPreferences.formatHomeSummary(requireContext());
            tv.setText(summary != null ? summary : getString(R.string.proxima_sessao_agendar_hint));
            tv.setBackgroundResource(R.drawable.bg_button_green);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary));
        } else {
            tv.setText(R.string.proxima_sessao_agendar_hint);
            tv.setBackgroundResource(R.drawable.bg_chip_blue);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary));
        }
    }

    /**
     * Nome mostrado na home: argumentos da Activity (login) ou Firebase Auth (displayName / email).
     */
    private String resolveNomeCliente() {
        String nome = "Paciente";
        if (getArguments() != null) {
            nome = getArguments().getString(KEY_NOME, "Paciente");
        }
        if (!"Paciente".equals(nome) && nome != null && !nome.trim().isEmpty()) {
            return nome.trim();
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String display = user.getDisplayName();
            if (display != null && !display.trim().isEmpty()) {
                return display.trim();
            }
            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                int at = email.indexOf('@');
                String local = at > 0 ? email.substring(0, at) : email;
                if (!local.isEmpty()) {
                    return local;
                }
            }
        }
        return nome != null && !nome.trim().isEmpty() ? nome.trim() : "Paciente";
    }

    private void setupMayaReply(View root) {
        TextView btnResponder = root.findViewById(R.id.btnResponderMaya);
        LinearLayout layoutComposer = root.findViewById(R.id.layoutReplyComposer);
        EditText etReply = root.findViewById(R.id.etReplyMaya);
        TextView btnEnviar = root.findViewById(R.id.btnEnviarResposta);
        LinearLayout layoutSaved = root.findViewById(R.id.layoutSavedReply);
        TextView tvSaved = root.findViewById(R.id.tvSavedReplyMaya);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_HOME, android.content.Context.MODE_PRIVATE);
        String replyKey = replyPreferenceKey();

        Runnable refreshSaved = () -> {
            String saved = prefs.getString(replyKey, "").trim();
            if (saved.isEmpty()) {
                layoutSaved.setVisibility(View.GONE);
                tvSaved.setText("");
            } else {
                layoutSaved.setVisibility(View.VISIBLE);
                tvSaved.setText(saved);
            }
        };

        refreshSaved.run();

        btnResponder.setOnClickListener(v -> {
            boolean opening = layoutComposer.getVisibility() != View.VISIBLE;
            layoutComposer.setVisibility(opening ? View.VISIBLE : View.GONE);
            if (opening) {
                String draft = prefs.getString(replyKey, "");
                etReply.setText(draft);
                etReply.requestFocus();
                etReply.setSelection(etReply.length());
            }
        });

        btnEnviar.setOnClickListener(v -> {
            String text = etReply.getText() != null ? etReply.getText().toString().trim() : "";
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(requireContext(), R.string.resposta_vazia_toast, Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString(replyKey, text).apply();
            layoutComposer.setVisibility(View.GONE);
            refreshSaved.run();
            Toast.makeText(requireContext(), R.string.resposta_salva_toast, Toast.LENGTH_SHORT).show();
        });
    }

    private String replyPreferenceKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid() != null && !user.getUid().isEmpty()) {
            return PREF_KEY_REPLY_PREFIX + user.getUid();
        }
        return PREF_KEY_REPLY_PREFIX + "guest";
    }

    // Dialog de confirmacao para evitar logout acidental.
    private void showLogoutDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarLogout);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarLogout);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnConfirmar.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Limpa sessao JWT local para evitar token antigo apos logout.
            new SessionManager(requireContext()).clear();

            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            dialog.dismiss();
        });

        dialog.show();
    }
}
