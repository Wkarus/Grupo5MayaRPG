package com.example.mayarpg;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/**
 * Historico de confirmacoes de consulta (append ao confirmar na agenda).
 */
public final class AppointmentHistoryStore {

    private static final String PREFS = "mayarpg_appointment_history";

    private AppointmentHistoryStore() {
    }

    public static void append(Context context, String dateIso, String timeText) {
        String suf = SessionBookingPreferences.userKeySuffix(context);
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String line = dateIso + "|" + timeText.trim() + "|" + System.currentTimeMillis();
        String existing = p.getString(suf, "");
        p.edit().putString(suf, existing.isEmpty() ? line : existing + "\n" + line).apply();
    }

    /**
     * Linhas mais recentes primeiro, texto legivel.
     */
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
            String[] parts = line.split("\\|");
            if (parts.length >= 3) {
                try {
                    long ts = Long.parseLong(parts[2].trim());
                    entries.add(new Entry(parts[0].trim(), parts[1].trim(), ts));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        Collections.sort(entries, Comparator.comparingLong((Entry e) -> e.ts).reversed());
        List<String> out = new ArrayList<>();
        for (Entry e : entries) {
            out.add(SessionBookingPreferences.formatLine(e.dateIso, e.time));
        }
        return out;
    }

    private static final class Entry {
        final String dateIso;
        final String time;
        final long ts;

        Entry(String dateIso, String time, long ts) {
            this.dateIso = dateIso;
            this.time = time;
            this.ts = ts;
        }
    }
}
