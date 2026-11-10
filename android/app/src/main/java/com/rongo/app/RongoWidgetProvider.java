package com.rongo.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Home-screen note widget. Each widget shows the note chosen in WidgetConfigActivity.
 * Stored choice per widget: prefs "w<widgetId>" = "<noteId>|<look>".
 * The app keeps the note list in prefs "notes" (see RongoChannels.saveWidget).
 */
public class RongoWidgetProvider extends AppWidgetProvider {

    static final String PREFS = "rongo_widget";
    static final String KEY_NOTES = "notes";
    static final String[] STYLES = {"glass", "night", "priority", "minimal"};
    static final String[] TIMES = {"morning", "afternoon", "evening", "night"};

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) render(ctx, mgr, id);
    }

    @Override
    public void onDeleted(Context ctx, int[] ids) {
        SharedPreferences.Editor e = prefs(ctx).edit();
        for (int id : ids) e.remove("w" + id);
        e.apply();
    }

    static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Called by the app whenever its notes change. */
    static void refreshAll(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, RongoWidgetProvider.class));
        for (int id : ids) render(ctx, mgr, id);
    }

    static int timeIndex() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (h >= 5 && h < 11) return 0;
        if (h >= 11 && h < 17) return 1;
        if (h >= 17 && h < 21) return 2;
        return 3;
    }

    static JSONObject findNote(Context ctx, String id) {
        try {
            JSONArray arr = new JSONArray(prefs(ctx).getString(KEY_NOTES, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null && id.equals(o.optString("id"))) return o;
            }
        } catch (Exception e) {
            // no notes saved yet
        }
        return null;
    }

    static int res(Context ctx, String name, String fallback) {
        int id = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
        if (id == 0) id = ctx.getResources().getIdentifier(fallback, "drawable", ctx.getPackageName());
        return id;
    }

    static String accent(String colour) {
        switch (colour) {
            case "coral": return "#FF9A8A";
            case "sun": return "#FFD766";
            case "lime": return "#C9EC7E";
            case "mint": return "#7FE3C0";
            case "teal": return "#6BD9DC";
            case "sky": return "#7CC8FF";
            case "indigo": return "#A9AEFF";
            case "violet": return "#D5B0FF";
            case "pink": return "#FFB0DA";
            case "orange": return "#FFC486";
            default: return "#8E86C8";
        }
    }

    static String kindLabel(JSONObject n) {
        String k = n.optString("k", "note");
        String label = k.equals("list") ? "CHECKLIST" : k.equals("table") ? "TABLE" : "NOTE";
        switch (n.optInt("pr", 0)) {
            case 4: return label + " · URGENT";
            case 3: return label + " · HIGH";
            case 2: return label + " · MEDIUM";
            case 1: return label + " · LOW";
            default: return label;
        }
    }

    static void render(Context ctx, AppWidgetManager mgr, int widgetId) {
        String pkg = ctx.getPackageName();
        RemoteViews v = new RemoteViews(pkg, R.layout.widget_note);
        String cfg = prefs(ctx).getString("w" + widgetId, null);

        String noteId = null, look = "glass";
        if (cfg != null && cfg.contains("|")) {
            int bar = cfg.lastIndexOf('|');
            noteId = cfg.substring(0, bar);
            look = cfg.substring(bar + 1);
        }

        if (noteId == null) {
            // not set up yet: tapping the widget opens the setup screen
            v.setInt(R.id.w_root, "setBackgroundResource", res(ctx, "note_glass_snow", "note_glass_snow"));
            v.setTextViewText(R.id.w_kind, "RONGO");
            v.setTextViewText(R.id.w_when, "");
            v.setTextViewText(R.id.w_title, "Choose a note");
            v.setTextViewText(R.id.w_preview, "Tap here to pick the note to show.");
            v.setInt(R.id.w_accent, "setBackgroundColor", Color.parseColor("#8E86C8"));
            Intent setup = new Intent(ctx, WidgetConfigActivity.class);
            setup.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            setup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(ctx, widgetId, setup,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
            setTextColors(v, 0xFF1D1736);
            mgr.updateAppWidget(widgetId, v);
            return;
        }

        JSONObject n = findNote(ctx, noteId);
        int t = timeIndex();
        String colour = (n == null) ? "snow" : n.optString("c", "snow");
        int pr = (n == null) ? 0 : n.optInt("pr", 0);

        boolean dark;
        String bg;
        switch (look) {
            case "night":
                dark = true;
                bg = "note_night_" + colour;
                break;
            case "priority":
                dark = (t == 3);
                bg = "widget_" + TIMES[t] + "_p" + Math.max(1, Math.min(4, pr));
                break;
            case "minimal":
                dark = false;
                bg = "note_minimal";
                break;
            default:
                dark = false;
                bg = "note_glass_" + colour;
        }
        v.setInt(R.id.w_root, "setBackgroundResource", res(ctx, bg, "note_glass_snow"));
        v.setInt(R.id.w_accent, "setBackgroundColor", Color.parseColor(accent(colour)));

        int text = dark ? 0xFFF4F1FF : 0xFF1D1736;
        setTextColors(v, text);

        if (n == null) {
            v.setTextViewText(R.id.w_kind, "NOTE");
            v.setTextViewText(R.id.w_when, "");
            v.setTextViewText(R.id.w_title, "Note not found");
            v.setTextViewText(R.id.w_preview, "It may have been deleted. Long-press the widget to pick another.");
        } else {
            v.setTextViewText(R.id.w_kind, kindLabel(n));
            v.setTextViewText(R.id.w_when, n.optString("w", ""));
            v.setTextViewText(R.id.w_title, n.optString("t", "Untitled"));
            v.setTextViewText(R.id.w_preview, n.optString("p", ""));
        }

        // tapping the note opens it in Rongo
        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        open.putExtra("openNote", noteId);
        v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(ctx, widgetId, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        mgr.updateAppWidget(widgetId, v);
    }

    static void setTextColors(RemoteViews v, int color) {
        v.setTextColor(R.id.w_kind, color);
        v.setTextColor(R.id.w_when, color);
        v.setTextColor(R.id.w_title, color);
        v.setTextColor(R.id.w_preview, color);
    }
}
