package com.example.mayarpg.network;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.example.mayarpg.BuildConfig;

/**
 * Escolhe a URL da API: emulador usa 10.0.2.2; celular fisico usa URL publica (Cloudflare / Wi-Fi).
 */
public final class ApiConfig {

    private ApiConfig() {
    }

    public static String resolveBaseUrl(Context context) {
        if (isEmulator()) {
            return BuildConfig.API_BASE_URL_EMULATOR;
        }
        return BuildConfig.API_BASE_URL_DEVICE;
    }

    public static boolean isEmulator() {
        String fingerprint = Build.FINGERPRINT != null ? Build.FINGERPRINT : "";
        String model = Build.MODEL != null ? Build.MODEL : "";
        String product = Build.PRODUCT != null ? Build.PRODUCT : "";
        String hardware = Build.HARDWARE != null ? Build.HARDWARE : "";

        return fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || model.contains("google_sdk")
                || model.contains("Emulator")
                || model.contains("Android SDK built for x86")
                || model.contains("sdk_gphone")
                || product.contains("sdk_gphone")
                || product.contains("sdk_google")
                || product.contains("vbox")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu");
    }

    public static boolean isLocalDevUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String normalized = url.toLowerCase();
        return normalized.contains("10.0.2.2")
                || normalized.contains("localhost")
                || normalized.contains("127.0.0.1")
                || normalized.contains("192.168.");
    }
}
