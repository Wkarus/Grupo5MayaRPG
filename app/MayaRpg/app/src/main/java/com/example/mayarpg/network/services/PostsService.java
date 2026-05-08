package com.example.mayarpg.network.services;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PostsService {
    @GET("posts")
    Call<List<PostDto>> getPosts();

    class PostDto {
        @SerializedName("id")
        public long id;
        @SerializedName("titulo")
        public String titulo;
        @SerializedName("conteudo")
        public String conteudo;
        @SerializedName("categoria")
        public String categoria;
        @SerializedName("status")
        public String status;
    }
}
