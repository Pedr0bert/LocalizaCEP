package com.example.localizacep;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HistoryStorage {

    private static final String PREF_NAME = "localiza_cep_history";
    private static final String KEY_HISTORY = "history_list";
    private static final int MAX_HISTORY_ITEMS = 50;

    public static ArrayList<Endereco> getHistory(Context context) {
        ArrayList<Endereco> historyList = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyJsonStr = prefs.getString(KEY_HISTORY, null);

        if (historyJsonStr != null && !historyJsonStr.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(historyJsonStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Endereco endereco = Endereco.fromJsonObject(obj);
                    if (endereco != null) {
                        historyList.add(endereco);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return historyList;
    }

    public static void addEndereco(Context context, Endereco endereco) {
        if (context == null || endereco == null || endereco.getCep().isEmpty()) {
            return;
        }

        ArrayList<Endereco> currentList = getHistory(context);

        // Remove duplicate entry if CEP already exists
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getCep().equalsIgnoreCase(endereco.getCep())) {
                currentList.remove(i);
                break;
            }
        }

        // Add to front (latest search first)
        currentList.add(0, endereco);

        // Cap size
        while (currentList.size() > MAX_HISTORY_ITEMS) {
            currentList.remove(currentList.size() - 1);
        }

        saveHistoryList(context, currentList);
    }

    public static void clearHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_HISTORY).apply();
    }

    private static void saveHistoryList(Context context, ArrayList<Endereco> list) {
        JSONArray jsonArray = new JSONArray();
        for (Endereco item : list) {
            jsonArray.put(item.toJsonObject());
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply();
    }
}