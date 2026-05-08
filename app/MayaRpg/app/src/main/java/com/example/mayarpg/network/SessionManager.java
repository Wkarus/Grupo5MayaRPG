package com.example.mayarpg.network;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gerencia o token JWT do backend em armazenamento local simples.
 * Para producao, considerar migrar para EncryptedSharedPreferences.
 */
public class SessionManager {
    private static final String PREF_NAME = "mayarpg_api_session";
    private static final String KEY_TOKEN = "jwt_token";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public void clear() {
        preferences.edit().remove(KEY_TOKEN).apply();
    }
}
