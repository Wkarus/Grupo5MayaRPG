package com.example.mayarpg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

/**
 * Area do utilizador: consulta marcada, historico, mensagens da Maya e exercicios feitos.
 */
public class UserFragment extends Fragment {

    private static final String KEY_NOME = "key_nome";

    public static UserFragment newInstance(String nomeUsuario) {
        UserFragment f = new UserFragment();
        Bundle args = new Bundle();
        args.putString(KEY_NOME, nomeUsuario == null ? "Paciente" : nomeUsuario);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String nome = getArguments() != null ? getArguments().getString(KEY_NOME, "Paciente") : "Paciente";
        TextView tvHeader = view.findViewById(R.id.tvUsuarioNomeHeader);
        tvHeader.setText(getString(R.string.bem_vindo, nome.trim()));

        refreshLists(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v != null) {
            refreshLists(v);
        }
    }

    private void refreshLists(View root) {
        if (getContext() == null) {
            return;
        }

        TextView tvConsulta = root.findViewById(R.id.tvConsultaMarcada);
        if (SessionBookingPreferences.hasBooking(requireContext())) {
            String s = SessionBookingPreferences.formatHomeSummary(requireContext());
            tvConsulta.setText(s != null ? s : getString(R.string.lista_sem_registros_perfil));
        } else {
            tvConsulta.setText(R.string.perfil_sem_consulta_marcada);
        }

        TextView tvHistConsultas = root.findViewById(R.id.tvHistoricoConsultas);
        List<String> consultas = AppointmentHistoryStore.getFormattedLinesDescending(requireContext());
        tvHistConsultas.setText(consultas.isEmpty()
                ? getString(R.string.lista_sem_registros_perfil)
                : formatBulletList(consultas));

        TextView tvMaya = root.findViewById(R.id.tvMensagensMaya);
        tvMaya.setText(buildMayaHistoricoText());

        TextView tvEx = root.findViewById(R.id.tvExerciciosFeitos);
        List<String> ex = ExerciseHistoryStore.getFormattedLinesDescending(requireContext());
        tvEx.setText(ex.isEmpty() ? getString(R.string.perfil_sem_exercicios) : formatBulletList(ex));
    }

    private static String formatBulletList(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append('•').append(' ').append(lines.get(i));
        }
        return sb.toString();
    }

    private String buildMayaHistoricoText() {
        String[] datas = getResources().getStringArray(R.array.maya_historico_data);
        String[] textos = getResources().getStringArray(R.array.maya_historico_texto);
        StringBuilder sb = new StringBuilder();

        int n = Math.min(datas.length, textos.length);
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append("\n\n—\n\n");
            }
            sb.append(getString(R.string.perfil_maya_item_header, datas[i]));
            sb.append("\n");
            sb.append(textos[i]);
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("mayarpg_home", android.content.Context.MODE_PRIVATE);
        String replyKey = "maya_reply_" + SessionBookingPreferences.userKeySuffix(requireContext());
        String reply = prefs.getString(replyKey, "").trim();
        if (!reply.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n\n—\n\n");
            }
            sb.append(getString(R.string.perfil_sua_resposta_header));
            sb.append("\n");
            sb.append(reply);
        }

        if (sb.length() == 0) {
            return getString(R.string.lista_sem_registros_perfil);
        }
        return sb.toString();
    }
}
