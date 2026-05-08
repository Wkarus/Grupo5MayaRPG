package com.example.mayarpg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ExercisesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cardExercise1).setOnClickListener(v -> openExerciseDetail(R.string.ex_1_fortalecimento_pernas));
        view.findViewById(R.id.btnIniciar1).setOnClickListener(v -> openExerciseDetail(R.string.ex_1_fortalecimento_pernas));

        view.findViewById(R.id.cardExercise2).setOnClickListener(v -> openExerciseDetail(R.string.ex_2_fortalecimento_quadril));
        view.findViewById(R.id.btnIniciar2).setOnClickListener(v -> openExerciseDetail(R.string.ex_2_fortalecimento_quadril));

        view.findViewById(R.id.cardExercise3).setOnClickListener(v -> openExerciseDetail(R.string.ex_3_fortalecimento_articular));
        view.findViewById(R.id.btnIniciar3).setOnClickListener(v -> openExerciseDetail(R.string.ex_3_fortalecimento_articular));

        View.OnClickListener consume = v -> { };
        view.findViewById(R.id.btnPular1).setOnClickListener(consume);
        view.findViewById(R.id.btnPular2).setOnClickListener(consume);
        view.findViewById(R.id.btnPular3).setOnClickListener(consume);
    }

    private void openExerciseDetail(int exerciseTitleRes) {
        ExerciseHistoryStore.log(requireContext(), getString(exerciseTitleRes));
        getParentFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, new ExerciseDetailFragment())
                .addToBackStack(null)
                .commit();
    }
}
