package com.example.mayarpg;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Guarda a data e horario da proxima sessao escolhidos na agenda (por utilizador).
 */
public final class SessionBookingPreferences {

    private static final String PREFS = "mayarpg_session_booking";
    private static final String SUF_DATE = "_date_iso";
    private static final String SUF_TIME = "_time";

    private SessionBookingPreferences() {
    }

    public static String userKeySuffix(Context context) {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u != null && u.getUid() != null && !u.getUid().isEmpty()) {
            return u.getUid();
        }
        return "guest";
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void save(Context context, String dateIso, String timeText) {
        String suf = userKeySuffix(context);
        prefs(context).edit()
                .putString(suf + SUF_DATE, dateIso)
                .putString(suf + SUF_TIME, timeText)
                .apply();
    }

    @Nullable
    public static String getDateIso(Context context) {
        String suf = userKeySuffix(context);
        return prefs(context).getString(suf + SUF_DATE, null);
    }

    @Nullable
    public static String getTime(Context context) {
        String suf = userKeySuffix(context);
        return prefs(context).getString(suf + SUF_TIME, null);
    }

    public static boolean hasBooking(Context context) {
        String d = getDateIso(context);
        String t = getTime(context);
        return d != null && !d.isEmpty() && t != null && !t.isEmpty();
    }

    /**
     * Texto para a home, ex.: "Ter, 22/04 as 9:00".
     */
    @Nullable
    public static String formatHomeSummary(Context context) {
        if (!hasBooking(context)) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(getDateIso(context), DateTimeFormatter.ISO_LOCAL_DATE);
            String[] weekPt = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "S\u00e1b"};
            int dayOfWeek = date.getDayOfWeek().getValue();
            int weekIndex = dayOfWeek % 7;
            String weekDay = weekPt[weekIndex];
            String formattedDate = String.format(Locale.getDefault(),
                    "%s, %02d/%02d",
                    weekDay,
                    date.getDayOfMonth(),
                    date.getMonthValue()
            );
            return formattedDate + " as " + getTime(context);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Formata data ISO + hora para texto igual ao da home (ex.: Ter, 22/04 as 9:00).
     */
    public static String formatLine(String dateIso, String timeText) {
        try {
            LocalDate date = LocalDate.parse(dateIso, DateTimeFormatter.ISO_LOCAL_DATE);
            String[] weekPt = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "S\u00e1b"};
            int dayOfWeek = date.getDayOfWeek().getValue();
            int weekIndex = dayOfWeek % 7;
            String weekDay = weekPt[weekIndex];
            String formattedDate = String.format(Locale.getDefault(),
                    "%s, %02d/%02d",
                    weekDay,
                    date.getDayOfMonth(),
                    date.getMonthValue()
            );
            return formattedDate + " as " + timeText;
        } catch (Exception e) {
            return dateIso + " as " + timeText;
        }
    }
}
