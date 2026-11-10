package com.rongo.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Setup screen shown when a Rongo widget is added (and when it is reconfigured). */
public class WidgetConfigActivity extends Activity {

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String look = "glass";
    private final List<String> noteIds = new ArrayList<>();
    private Button[] lookButtons;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setResult(RESULT_CANCELED);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        String cfg = RongoWidgetProvider.prefs(this).getString("w" + widgetId, null);
        if (cfg != null && cfg.contains("|")) look = cfg.substring(cfg.lastIndexOf('|') + 1);

        setContentView(R.layout.activity_widget_config);

        lookButtons = new Button[]{
                findViewById(R.id.s_glass), findViewById(R.id.s_night),
                findViewById(R.id.s_priority), findViewById(R.id.s_minimal)
        };
        for (int i = 0; i < lookButtons.length; i++) {
            final String name = RongoWidgetProvider.STYLES[i];
            lookButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    look = name;
                    highlightLook();
                }
            });
        }
        highlightLook();

        List<String> titles = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(RongoWidgetProvider.prefs(this)
                    .getString(RongoWidgetProvider.KEY_NOTES, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String when = o.optString("w", "");
                noteIds.add(o.optString("id"));
                titles.add(o.optString("t", "Untitled") + (when.isEmpty() ? "" : "   ·   " + when));
            }
        } catch (Exception e) {
            // nothing saved yet
        }

        ListView list = findViewById(R.id.note_list);
        TextView empty = findViewById(R.id.empty);
        if (titles.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
        }
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                choose(noteIds.get(position));
            }
        });
    }

    private void highlightLook() {
        for (int i = 0; i < lookButtons.length; i++) {
            lookButtons[i].setAlpha(RongoWidgetProvider.STYLES[i].equals(look) ? 1f : 0.5f);
        }
    }

    private void choose(String noteId) {
        RongoWidgetProvider.prefs(this).edit().putString("w" + widgetId, noteId + "|" + look).apply();
        RongoWidgetProvider.render(this, AppWidgetManager.getInstance(this), widgetId);
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, result);
        finish();
    }
}
