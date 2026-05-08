package com.example.mayarpg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mayarpg.network.ApiClient;
import com.example.mayarpg.network.services.ExerciseService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Detalhe de um exercicio: botao manda check-in para API e grava historico local
public class ExerciseDetailFragment extends Fragment {

    private static final String ARG_ID = "exerciseId";
    private static final String ARG_TITLE = "exerciseTitle";
    private static final String ARG_DESC = "exerciseDesc";

    public static ExerciseDetailFragment newInstance(long exerciseId, String title, String description) {
        ExerciseDetailFragment f = new ExerciseDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, exerciseId);
        args.putString(ARG_TITLE, title != null ? title : "");
        args.putString(ARG_DESC, description != null ? description : "");
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercise_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle b = getArguments();
        long exerciseId = b != null ? b.getLong(ARG_ID, -1) : -1;
        String title = b != null ? b.getString(ARG_TITLE, "") : "";
        String desc = b != null ? b.getString(ARG_DESC, "") : "";

        TextView tvTitle = view.findViewById(R.id.tvExerciseDetailTitle);
        TextView tvDesc = view.findViewById(R.id.tvExerciseDetailDesc);
        tvTitle.setText(title);
        tvDesc.setText(desc.isEmpty() ? getString(R.string.exercicio_tela_detalhe) : desc);

        ImageButton back = view.findViewById(R.id.btnBackExercise);
        back.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        TextView btnCheckin = view.findViewById(R.id.btnRegistrarExecucao);
        btnCheckin.setOnClickListener(v -> submitCheckin(exerciseId, title, btnCheckin));
    }

    // POST /exercises/{id}/checkin e depois salva no historico local
    private void submitCheckin(long exerciseId, String titleForHistory, TextView btn) {
        if (getContext() == null || exerciseId < 1) {
            Toast.makeText(requireContext(), R.string.exercicio_checkin_erro, Toast.LENGTH_SHORT).show();
            return;
        }
        btn.setEnabled(false);
        ApiClient.exerciseService(requireContext())
                .checkin(exerciseId, new ExerciseService.EmptyBody())
                .enqueue(new Callback<ExerciseService.CheckinResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ExerciseService.CheckinResponse> call, @NonNull Response<ExerciseService.CheckinResponse> response) {
                        if (!isAdded()) {
                            return;
                        }
                        btn.setEnabled(true);
                        if (response.isSuccessful()) {
                            ExerciseHistoryStore.log(requireContext(), titleForHistory);
                            Toast.makeText(requireContext(), R.string.exercicio_checkin_ok, Toast.LENGTH_SHORT).show();
                            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                                getParentFragmentManager().popBackStack();
                            }
                        } else {
                            Toast.makeText(requireContext(), R.string.exercicio_checkin_erro, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ExerciseService.CheckinResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) {
                            return;
                        }
                        btn.setEnabled(true);
                        Toast.makeText(requireContext(), R.string.exercicio_checkin_erro, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
