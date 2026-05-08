package com.example.mayarpg.network;

import android.content.Context;

import com.example.mayarpg.BuildConfig;
import com.example.mayarpg.network.services.AgendaService;
import com.example.mayarpg.network.services.AuthService;
import com.example.mayarpg.network.services.PostsService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fabrica singleton de servicos Retrofit usados no app.
 */
public class ApiClient {
    private static Retrofit retrofit;

    private ApiClient() {
    }

    private static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            SessionManager sessionManager = new SessionManager(context.getApplicationContext());

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new AuthInterceptor(sessionManager))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthService authService(Context context) {
        return getRetrofit(context).create(AuthService.class);
    }

    public static PostsService postsService(Context context) {
        return getRetrofit(context).create(PostsService.class);
    }

    public static AgendaService agendaService(Context context) {
        return getRetrofit(context).create(AgendaService.class);
    }
}
