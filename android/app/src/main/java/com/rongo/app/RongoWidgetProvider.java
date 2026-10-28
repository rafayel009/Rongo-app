package com.rongo.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Home-screen widget: four priority tiles (Urgent, High, Medium, Low).
 * The app saves a snapshot of the data into SharedPreferences (see RongoChannels.saveWidget).
 * Tile colours follow the priority and the time of day, using drawables widget_<time>_p<level>.
 */
public class RongoWidgetProvider extends AppWidgetProvider {

    static final String PREFS = "rongo_widget";
    static final String[] NAMES = {"Urgent", "High", "Medium", "Low"};
    static final int[] LEVELS = {4, 3, 2, 1};
    static final String[] TIMES = {"morning", "afternoon", "evening", "night"};

    static final int[] TILE = {R.id.t0, R.id.t1, R.id.t2, R.id.t3};
    static final int[] LABEL = {R.id.l0, R.id.l1, R.id.l2, R.id.l3};
    static final int[] COUNT = {R.id.n0, R.id.n1, R.id.n2, R.id.n3};
    static final int[] ITEM = {R.id.i0, R.id.i1, R.id.i2, R.id.i3};
    static final int[] WHEN = {R.id.m0, R.id.m1, R.id.m2, R.id.m3};

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) render(ctx, mgr, id);
    }

    /** Called by the app whenever its data changes. */
    static void refreshAll(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, RongoWidgetProvider.class));
        for (int id : ids) render(ctx, mgr, id);
    }

    static int timeIndex() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (h >= 5 && h < 11) return 0;   // morning
        if (h >= 11 && h < 17) return 1;  // afternoon
        if (h >= 17 && h < 21) return 2;  // evening
        return 3;                         // night
    }

    static void render(Context ctx, AppWidgetManager mgr, int widgetId) {
        String pkg = ctx.getPackageName();
        int t = timeIndex();
        RemoteViews v = new RemoteViews(pkg, R.layout.widget_rongo);

        // Tapping the widget opens Rongo.
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch != null) {
            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(ctx, 0, launch,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            v.setOnClickPendingIntent(R.id.widget_root, pi);
        }

        JSONObject data = null;
        try {
            String raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("data", null);
            if (raw != null) data = new JSONObject(raw);
        } catch (Exception e) {
            data = null;
        }

        int textColor = (t == 3) ? 0xFFF4F1FF : 0xFF1D1736;
        for (int i = 0; i < 4; i++) {
            int bg = ctx.getResources().getIdentifier(
                    "widget_" + TIMES[t] + "_p" + LEVELS[i], "drawable", pkg);
            if (bg != 0) v.setInt(TILE[i], "setBackgroundResource", bg);

            v.setTextViewText(LABEL[i], NAMES[i]);
            v.setTextColor(LABEL[i], textColor);

            JSONObject lvl = (data == null) ? null : data.optJSONObject(String.valueOf(LEVELS[i]));
            if (data == null) {
                v.setTextViewText(COUNT[i], "–");
                v.setTextViewText(ITEM[i], "Open Rongo to sync");
                v.setTextViewText(WHEN[i], "");
            } else {
                int count = (lvl == null) ? 0 : lvl.optInt("n", 0);
                v.setTextViewText(COUNT[i], String.valueOf(count));
                String first = "Nothing right now", when = "";
                JSONArray items = (lvl == null) ? null : lvl.optJSONArray("items");
                if (items != null && items.length() > 0) {
                    JSONObject it = items.optJSONObject(0);
                    if (it != null) {
                        first = it.optString("t", "Untitled");
                        when = it.optString("w", "");
                    }
                }
                v.setTextViewText(ITEM[i], first);
                v.setTextViewText(WHEN[i], when);
            }
            v.setTextColor(COUNT[i], textColor);
            v.setTextColor(ITEM[i], textColor);
            v.setTextColor(WHEN[i], textColor);
        }
        mgr.updateAppWidget(widgetId, v);
    }
}
