package com.example.mayarpg;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Persiste a proxima sessao (data + horario) no Cloud Firestore.
 * <p>
 * Caminho no console: colecao {@value #COLLECTION} &gt; documento com ID = UID do utilizador
 * (Firebase Authentication). Campos: {@value #FIELD_DATE_ISO} (string YYYY-MM-DD),
 * {@value #FIELD_TIME} (string, ex. 9:00).
 * <p>
 * Regras sugeridas (exemplo): permitir read/write apenas se request.auth.uid == documentId.
 */
public final class SessionBookingFirestore {

    public static final String COLLECTION = "user_bookings";
    public static final String FIELD_DATE_ISO = "dateIso";
    public static final String FIELD_TIME = "time";

    private SessionBookingFirestore() {
    }

    /**
     * Grava no Firestore (e deve chamar-se depois de {@link SessionBookingPreferences#save}).
     * Visitante sem login: nao faz nada na nuvem.
     */
    public static void syncToCloud(@NonNull Context context, @NonNull String dateIso, @NonNull String timeText) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_DATE_ISO, dateIso);
        data.put(FIELD_TIME, timeText);

        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .document(user.getUid())
                .set(data);
    }

    /**
     * Le o Firestore e atualiza o cache local; depois chama {@code onDone} na thread principal dos callbacks do Firebase.
     */
    public static void fetchAndCacheLocal(@NonNull Context context, @NonNull Runnable onDone) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            onDone.run();
            return;
        }
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String d = doc.getString(FIELD_DATE_ISO);
                        String t = doc.getString(FIELD_TIME);
                        if (d != null && t != null && !d.trim().isEmpty() && !t.trim().isEmpty()) {
                            SessionBookingPreferences.save(context.getApplicationContext(), d.trim(), t.trim());
                        }
                    }
                    onDone.run();
                })
                .addOnFailureListener(e -> onDone.run());
    }
}
