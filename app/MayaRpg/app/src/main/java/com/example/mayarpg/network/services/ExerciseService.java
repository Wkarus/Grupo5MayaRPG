package com.example.mayarpg.network.services;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

// Chamadas da API de exercicios (Retrofit)
public interface ExerciseService {

    // POST precisa de um body JSON; este vira {}
    class EmptyBody {
    }

    @GET("exercises")
    Call<List<ExerciseDto>> listExercises();

    @POST("exercises/{id}/checkin")
    Call<CheckinResponse> checkin(@Path("id") long id, @Body EmptyBody body);

    class CheckinResponse {
        @SerializedName("message")
        public String message;
    }

    class ExerciseDto {
        @SerializedName("id")
        public long id;
        @SerializedName("titulo")
        public String titulo;
        @SerializedName("descricao")
        public String descricao;
    }
}
