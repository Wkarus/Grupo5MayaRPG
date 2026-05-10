package com.example.mayarpg;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/**
 * Notificação local simples para avisar quando a API trouxer exercícios novos.
 */
public final class ExerciseNewNotification {

    private static final String PREFS = "mayarpg_exercise_notify";
    private static final String KEY_PREFIX = "last_count_";
    private static final String CHANNEL_ID = "exercise_updates";

    private ExerciseNewNotification() {
    }

    public static void notifyIfHasNew(Context context, int currentCount) {
        if (currentCount <= 0) {
            return;
        }
        String key = KEY_PREFIX + SessionBookingPreferences.userKeySuffix(context);
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int oldCount = p.getInt(key, 0);
        p.edit().putInt(key, currentCount).apply();

        // Primeira vez só salva baseline para não notificar em massa.
        if (oldCount == 0 || currentCount <= oldCount) {
            return;
        }

        int newItems = currentCount - oldCount;
        createChannelIfNeeded(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String body = context.getString(R.string.notif_exercicios_novos_body, newItems);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(context.getString(R.string.notif_exercicios_novos_title))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(1201, builder.build());
    }

    private static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_exercicios_channel),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(context.getString(R.string.notif_exercicios_channel_desc));
        manager.createNotificationChannel(channel);
    }
}
