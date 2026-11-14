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

import java.util.Arrays;
import java.util.Calendar;

/**
 * Home-screen note widget. Fills in automatically:
 * - Each widget shows the note at its position in the list the app sends (1st widget = top note,
 *   2nd widget = next note, and so on). The list updates whenever notes change.
 * - Optional: a widget can be pinned to one specific note from its settings (prefs "w<id>" = note id).
 * The app saves the list in prefs "notes" and the look in prefs "look" (see RongoChannels.saveWidget).
 */
public class RongoWidgetProvider extends AppWidgetProvider {

    static final String PREFS = "rongo_widget";
    static final String KEY_NOTES = "notes";
    static final String KEY_LOOK = "look";
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

    /** Called by the app whenever its notes or look change. */
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

    /** Position of this widget among all Rongo widgets, 0 for the first one. */
    static int position(AppWidgetManager mgr, Context ctx, int widgetId) {
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, RongoWidgetProvider.class)).clone();
        Arrays.sort(ids);
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == widgetId) return i;
        }
        return 0;
    }

    static JSONArray notes(Context ctx) {
        try {
            return new JSONArray(prefs(ctx).getString(KEY_NOTES, "[]"));
        } catch (Exception e) {
            return null;
        }
    }

    static JSONObject byId(JSONArray arr, String id) {
        if (arr == null) return null;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null && id.equals(o.optString("id"))) return o;
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
        String look = prefs(ctx).getString(KEY_LOOK, "glass");
        String pinned = prefs(ctx).getString("w" + widgetId, null);
        JSONArray list = notes(ctx);
        int t = timeIndex();

        JSONObject n = null;
        if (list != null) {
            if (pinned != null && !pinned.equals("auto")) {
                n = byId(list, pinned);
            } else {
                int idx = position(mgr, ctx, widgetId);
                n = (idx < list.length()) ? list.optJSONObject(idx) : null;
            }
        }

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
        v.setTextColor(R.id.w_kind, text);
        v.setTextColor(R.id.w_when, text);
        v.setTextColor(R.id.w_title, text);
        v.setTextColor(R.id.w_preview, text);

        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (n == null) {
            if (list == null) {
                v.setTextViewText(R.id.w_kind, "RONGO");
                v.setTextViewText(R.id.w_when, "");
                v.setTextViewText(R.id.w_title, "Open Rongo");
                v.setTextViewText(R.id.w_preview, "Your notes appear here after the app has synced once.");
            } else {
                v.setTextViewText(R.id.w_kind, "RONGO");
                v.setTextViewText(R.id.w_when, "");
                v.setTextViewText(R.id.w_title, "No notes yet");
                v.setTextViewText(R.id.w_preview, "Create a note in Rongo and it appears here.");
            }
        } else {
            v.setTextViewText(R.id.w_kind, kindLabel(n));
            v.setTextViewText(R.id.w_when, n.optString("w", ""));
            v.setTextViewText(R.id.w_title, n.optString("t", "Untitled"));
            v.setTextViewText(R.id.w_preview, n.optString("p", ""));
            open.putExtra("openNote", n.optString("id"));
        }
        // a tap opens the note (or the app)
        v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(ctx, widgetId, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        mgr.updateAppWidget(widgetId, v);
    }
}
