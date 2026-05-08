package com.example.mayarpg.network.services;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AgendaService {
    @GET("agenda/disponivel")
    Call<List<AgendaDisponivelDto>> getAgendaDisponivel();

    @POST("agendamentos")
    Call<Void> criarAgendamento(@Body CriarAgendamentoRequest body);

    class AgendaDisponivelDto {
        @SerializedName("data")
        public String data;
        @SerializedName("horario_inicio")
        public String horarioInicio;
        @SerializedName("horario_fim")
        public String horarioFim;
        @SerializedName("bloqueado")
        public boolean bloqueado;
    }

    class CriarAgendamentoRequest {
        @SerializedName("data")
        public final String data;
        @SerializedName("horario")
        public final String horario;
        @SerializedName("nome")
        public final String nome;
        @SerializedName("observacao")
        public final String observacao;

        public CriarAgendamentoRequest(String data, String horario, String nome, String observacao) {
            this.data = data;
            this.horario = horario;
            this.nome = nome;
            this.observacao = observacao;
        }
    }
}
