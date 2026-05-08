package com.example.mayarpg;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Registo local de exercicios iniciados pelo utilizador (aba Exercicios).
 */
public final class ExerciseHistoryStore {

    private static final String PREFS = "mayarpg_exercise_history";

    private ExerciseHistoryStore() {
    }

    public static void log(Context context, String exerciseTitle) {
        if (exerciseTitle == null || exerciseTitle.trim().isEmpty()) {
            return;
        }
        String suf = SessionBookingPreferences.userKeySuffix(context);
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String line = exerciseTitle.trim().replace("|", " ").replace("\n", " ")
                + "|" + System.currentTimeMillis();
        String existing = p.getString(suf, "");
        p.edit().putString(suf, existing.isEmpty() ? line : existing + "\n" + line).apply();
    }

    public static List<String> getFormattedLinesDescending(Context context) {
        String suf = SessionBookingPreferences.userKeySuffix(context);
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = p.getString(suf, "");
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Entry> entries = new ArrayList<>();
        for (String line : raw.split("\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            int sep = line.lastIndexOf('|');
            if (sep <= 0) {
                continue;
            }
            String title = line.substring(0, sep).trim();
            try {
                long ts = Long.parseLong(line.substring(sep + 1).trim());
                entries.add(new Entry(title, ts));
            } catch (NumberFormatException ignored) {
            }
        }
        Collections.sort(entries, Comparator.comparingLong((Entry e) -> e.ts).reversed());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        List<String> out = new ArrayList<>();
        for (Entry e : entries) {
            out.add(e.title + " — " + sdf.format(new Date(e.ts)));
        }
        return out;
    }

    private static final class Entry {
        final String title;
        final long ts;

        Entry(String title, long ts) {
            this.title = title;
            this.ts = ts;
        }
    }
}
