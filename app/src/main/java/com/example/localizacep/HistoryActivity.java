package com.example.localizacep;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_ENDERECO = "extra_selected_endereco";

    private ListView listViewHistory;
    private TextView textEmptyHistory;
    private HistoryAdapter adapter;
    private ArrayList<Endereco> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        listViewHistory = findViewById(R.id.list_view_history);
        textEmptyHistory = findViewById(R.id.text_empty_history);

        listViewHistory.setEmptyView(textEmptyHistory);

        loadHistory();

        listViewHistory.setOnItemClickListener((parent, view, position, id) -> {
            if (historyList != null && position < historyList.size()) {
                Endereco selected = historyList.get(position);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_ENDERECO, selected);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void loadHistory() {
        historyList = HistoryStorage.getHistory(this);
        adapter = new HistoryAdapter(this, historyList);
        listViewHistory.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_history) {
            HistoryStorage.clearHistory(this);
            if (historyList != null) {
                historyList.clear();
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            Toast.makeText(this, R.string.toast_history_cleared, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}