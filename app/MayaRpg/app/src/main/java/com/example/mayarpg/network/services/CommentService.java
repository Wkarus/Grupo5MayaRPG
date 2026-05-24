package com.example.mayarpg.network.services;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CommentService {

    class CreateBody {
        @SerializedName("texto")
        public String texto;

        @SerializedName("postId")
        public Integer postId;

        public CreateBody(String texto) {
            this.texto = texto;
            this.postId = null;
        }

        public CreateBody(String texto, int postId) {
            this.texto = texto;
            this.postId = postId;
        }
    }

    class CreateResponse {
        @SerializedName("message")
        public String message;
    }

    class PostComment {
        @SerializedName("id")
        public int id;

        @SerializedName("texto")
        public String texto;

        @SerializedName("resposta")
        public String resposta;

        @SerializedName("createdAt")
        public String createdAt;
    }

    @POST("comments")
    Call<CreateResponse> create(@Body CreateBody body);

    @GET("posts/{id}/comments")
    Call<List<PostComment>> getPostComments(@Path("id") int postId);
}
