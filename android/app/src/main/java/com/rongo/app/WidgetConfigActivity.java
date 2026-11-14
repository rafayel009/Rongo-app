package com.rongo.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Widget settings. A new widget never shows this screen: it fills in automatically.
 * Long-press a widget (Android 12+) to pin it to one note, or return to automatic.
 */
public class WidgetConfigActivity extends Activity {

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private final List<String> noteIds = new ArrayList<>();

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

        // first time: no screen, the widget simply starts in automatic mode
        if (RongoWidgetProvider.prefs(this).getString("w" + widgetId, null) == null) {
            choose("auto");
            return;
        }

        setContentView(R.layout.activity_widget_config);

        List<String> titles = new ArrayList<>();
        titles.add("Automatic (most important note for this widget)");
        noteIds.add("auto");
        try {
            JSONArray arr = RongoWidgetProvider.notes(this);
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o == null) continue;
                    String when = o.optString("w", "");
                    noteIds.add(o.optString("id"));
                    titles.add(o.optString("t", "Untitled") + (when.isEmpty() ? "" : "   ·   " + when));
                }
            }
        } catch (Exception e) {
            // nothing saved yet
        }

        ListView list = findViewById(R.id.note_list);
        TextView empty = findViewById(R.id.empty);
        if (noteIds.size() <= 1) {
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

    private void choose(String noteIdOrAuto) {
        RongoWidgetProvider.prefs(this).edit().putString("w" + widgetId, noteIdOrAuto).apply();
        RongoWidgetProvider.render(this, AppWidgetManager.getInstance(this), widgetId);
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, result);
        finish();
    }
}
