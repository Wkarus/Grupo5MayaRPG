package com.example.mayarpg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mayarpg.network.ApiClient;
import com.example.mayarpg.network.services.ExerciseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Aba Exercicios: busca a lista no servidor e monta os cards na tela
public class ExercisesFragment extends Fragment {

    private ProgressBar progress;
    private ScrollView scroll;
    private LinearLayout container;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress = view.findViewById(R.id.progressExercises);
        scroll = view.findViewById(R.id.scrollExercises);
        container = view.findViewById(R.id.containerExerciseList);
        tvEmpty = view.findViewById(R.id.tvExercisesEmpty);
        loadFromApi();
    }

    // GET /exercises e monta cada linha do layout
    private void loadFromApi() {
        if (getContext() == null) {
            return;
        }
        progress.setVisibility(View.VISIBLE);
        scroll.setVisibility(View.GONE);

        ApiClient.exerciseService(requireContext()).listExercises().enqueue(new Callback<List<ExerciseService.ExerciseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ExerciseService.ExerciseDto>> call, @NonNull Response<List<ExerciseService.ExerciseDto>> response) {
                if (!isAdded()) {
                    return;
                }
                progress.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), R.string.exercicios_erro_carregar, Toast.LENGTH_LONG).show();
                    scroll.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    return;
                }
                List<ExerciseService.ExerciseDto> list = response.body();
                container.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(requireContext());
                if (list == null || list.isEmpty()) {
                    scroll.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    return;
                }
                // Notificação local simples baseada na diferença de quantidade de exercícios.
                ExerciseNewNotification.notifyIfHasNew(requireContext(), list.size());
                tvEmpty.setVisibility(View.GONE);
                for (ExerciseService.ExerciseDto ex : list) {
                    if (ex == null) {
                        continue;
                    }
                    View row = inflater.inflate(R.layout.item_exercise_row, container, false);
                    TextView title = row.findViewById(R.id.tvExerciseTitle);
                    TextView btn = row.findViewById(R.id.btnOpenExercise);
                    String titulo = ex.titulo != null ? ex.titulo : "";
                    title.setText(titulo);
                    String desc = ex.descricao != null ? ex.descricao : "";
                    long id = ex.id;
                    btn.setOnClickListener(v -> openDetail(id, titulo, desc));
                    container.addView(row);
                }
                scroll.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<List<ExerciseService.ExerciseDto>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                progress.setVisibility(View.GONE);
                scroll.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), R.string.exercicios_erro_carregar, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openDetail(long exerciseId, String title, String description) {
        getParentFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, ExerciseDetailFragment.newInstance(exerciseId, title, description))
                .addToBackStack(null)
                .commit();
    }
}
