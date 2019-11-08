package com.example.heroalert;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class HeroAlert extends AppWidgetProvider {

    SharedPreferences prefs;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId, RemoteViews views) {
        Log.d("Widget", "updating widget 1st method");
        prefs = context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String json = prefs.getString("Json", "");
        JSONObject jsonOb = null;
        int level = 200;
        try {
            jsonOb = new JSONObject(json);
            Double meter = jsonOb.getDouble("meter");
            meter = meter - 0.5;
            if (meter < 0) {
                meter = 0.5;
            }
            level = (int) (meter * 255 / 3);
            if (level > 255) {
                level = 255;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Widget json", json);
        views.setInt(R.id.heroalert_icon, "setAlpha", level);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Intent configIntent = new Intent(context, MainActivity.class);
        prefs = context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hero_alert);

        views.setOnClickPendingIntent(R.id.heroalert_icon, configPendingIntent);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
        prefs = context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }


}

