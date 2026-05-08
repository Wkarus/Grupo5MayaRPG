package com.example.mayarpg.network.services;

import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("auth/firebase")
    Call<LoginResponse> loginWithFirebase(@Body FirebaseLoginRequest body);

    class FirebaseLoginRequest {
        @SerializedName("idToken")
        public final String idToken;

        public FirebaseLoginRequest(String idToken) {
            this.idToken = idToken;
        }
    }

    class LoginRequest {
        @SerializedName("email")
        public final String email;
        @SerializedName("senha")
        public final String senha;

        public LoginRequest(String email, String senha) {
            this.email = email;
            this.senha = senha;
        }
    }

    class LoginResponse {
        @SerializedName("token")
        public String token;
        @SerializedName("user")
        public UserPayload user;
    }

    class UserPayload {
        @SerializedName("id")
        public long id;
        @SerializedName("email")
        public String email;
        @SerializedName("role")
        public String role;
        @SerializedName("nome")
        public String nome;
    }
}
