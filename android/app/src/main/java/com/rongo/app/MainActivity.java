package com.rongo.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(RongoChannels.class);
        super.onCreate(savedInstanceState);
        rememberOpen(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        rememberOpen(intent);
    }

    /** A widget tap sends the note id here; the web app picks it up and opens the note. */
    private void rememberOpen(Intent intent) {
        if (intent != null && intent.hasExtra("openNote")) {
            String id = intent.getStringExtra("openNote");
            getSharedPreferences(RongoWidgetProvider.PREFS, Context.MODE_PRIVATE)
                    .edit().putString("pendingOpen", id).apply();
            intent.removeExtra("openNote");
        }
    }
}
