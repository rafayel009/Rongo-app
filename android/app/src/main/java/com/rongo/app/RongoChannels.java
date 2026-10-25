package com.rongo.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;


/**
 * Creates one notification channel per priority. Each channel has its own
 * 5-second sound, its own vibration pattern, and (for High and Urgent) the
 * alarm volume stream so the alert is louder.
 * Channel IDs must match CHANS in www/index.html.
 */
@CapacitorPlugin(name = "RongoChannels")
public class RongoChannels extends Plugin {

    /* reminder patterns in ms: index is the priority (0 Low .. 3 Urgent). Levels 7, 8 and 9 are UI taps. */
    private static final long[][] PATTERNS = {
            {0, 300, 200, 300},
            {0, 500, 250, 500, 250, 500},
            {0, 600, 200, 600, 200, 600, 200, 600},
            {0, 900, 200, 900, 200, 900, 200, 900, 200, 900}
    };

    /** Vibrates the phone directly, so it works even when the web view blocks navigator.vibrate. */
    @PluginMethod
    public void vibrate(PluginCall call) {
        int level = call.getInt("level", 1);
        long[] pattern;
        if (level == 9) pattern = new long[]{0, 30};        // long-press lift
        else if (level == 8) pattern = new long[]{0, 12};   // light tap
        else if (level == 7) pattern = new long[]{0, 25, 60, 25}; // confirmation
        else pattern = PATTERNS[Math.max(0, Math.min(3, level))];  // reminders 0..3
        try {
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) {
                call.reject("This phone has no vibrator");
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                v.vibrate(pattern, -1);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Vibration failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void createChannels(PluginCall call) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                String pkg = getContext().getPackageName();
                channel(nm, pkg, "rongo3_low", "Reminders: low", 3, "rongo3_low",
                        new long[]{0, 300, 200, 300}, false);
                channel(nm, pkg, "rongo3_med", "Reminders: medium", 4, "rongo3_med",
                        new long[]{0, 500, 250, 500, 250, 500}, false);
                channel(nm, pkg, "rongo3_high", "Reminders: high", 4, "rongo3_high",
                        new long[]{0, 600, 200, 600, 200, 600, 200, 600}, true);
                channel(nm, pkg, "rongo3_urgent", "Reminders: urgent", 5, "rongo3_urgent",
                        new long[]{0, 900, 200, 900, 200, 900, 200, 900, 200, 900}, true);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Could not create reminder channels: " + e.getMessage());
        }
    }

    private void channel(NotificationManager nm, String pkg, String id, String name,
                         int importance, String sound, long[] pattern, boolean alarmStream) {
        NotificationChannel ch = new NotificationChannel(id, name, importance);
        ch.setDescription("Reminders from your notes");
        ch.enableVibration(true);
        ch.setVibrationPattern(pattern);
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(alarmStream ? AudioAttributes.USAGE_ALARM : AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        ch.setSound(Uri.parse("android.resource://" + pkg + "/raw/" + sound), attrs);
        nm.createNotificationChannel(ch);
    }
}
