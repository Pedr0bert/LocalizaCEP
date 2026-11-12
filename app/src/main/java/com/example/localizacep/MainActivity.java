package com.example.localizacep;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText editTextCep;
    private View buttonSearch;
    private View buttonHistory;
    private ProgressBar progressBar;
    private MaterialCardView cardResult;
    private TextView textResultCep;
    private TextView textResultLogradouro;
    private TextView textResultBairro;
    private TextView textResultCidadeUf;
    private TextView textResultDdd;

    private ExecutorService executorService;
    private ActivityResultLauncher<Intent> historyLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        executorService = Executors.newSingleThreadExecutor();

        editTextCep = findViewById(R.id.edit_text_cep);
        buttonSearch = findViewById(R.id.button_search);
        buttonHistory = findViewById(R.id.button_history);
        progressBar = findViewById(R.id.progress_bar);
        cardResult = findViewById(R.id.card_result);

        textResultCep = findViewById(R.id.text_result_cep);
        textResultLogradouro = findViewById(R.id.text_result_logradouro);
        textResultBairro = findViewById(R.id.text_result_bairro);
        textResultCidadeUf = findViewById(R.id.text_result_cidade_uf);
        textResultDdd = findViewById(R.id.text_result_ddd);

        historyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Endereco selected = IntentCompat.getSerializableExtra(result.getData(), HistoryActivity.EXTRA_SELECTED_ENDERECO, Endereco.class);
                        if (selected != null) {
                            displayEnderecoResult(selected);
                            editTextCep.setText(selected.getCep());
                        }
                    }
                }
        );

        buttonSearch.setOnClickListener(v -> buscarCep());
        buttonHistory.setOnClickListener(v -> abrirHistorico());

        // Auto-formatting for CEP mask (00000-000)
        editTextCep.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                String str = s.toString().replaceAll("[^0-9]", "");
                if (str.length() > 8) {
                    str = str.substring(0, 8);
                }

                String formatted = str;
                if (str.length() > 5) {
                    formatted = str.substring(0, 5) + "-" + str.substring(5);
                }

                isUpdating = true;
                s.replace(0, s.length(), formatted);
                isUpdating = false;
            }
        });
    }

    private void buscarCep() {
        String rawInput = editTextCep.getText() != null ? editTextCep.getText().toString() : "";
        String cleanCep = rawInput.replaceAll("[^0-9]", "");

        // Structure IF: Check if CEP length is valid
        if (cleanCep.length() != 8) {
            Toast.makeText(this, R.string.toast_invalid_cep, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.searching, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);

        executorService.execute(() -> {
            Endereco resultEndereco = null;
            String errorMessage = null;

            // Structure TRY/CATCH for network call and JSON parsing
            try {
                String apiUrl = "https://viacep.com.br/ws/" + cleanCep + "/json/";
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    reader.close();
                    inputStream.close();

                    JSONObject jsonObject = new JSONObject(builder.toString());

                    // Check if ViaCEP returned error for non-existent CEP
                    if (jsonObject.has("erro") && jsonObject.getBoolean("erro")) {
                        resultEndereco = null;
                    } else {
                        resultEndereco = Endereco.fromJsonObject(jsonObject);
                    }
                } else {
                    errorMessage = getString(R.string.toast_network_error);
                }
                connection.disconnect();
            } catch (JSONException e) {
                e.printStackTrace();
                errorMessage = getString(R.string.toast_search_error);
            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = getString(R.string.toast_network_error);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = getString(R.string.toast_search_error);
            }

            final Endereco finalResult = resultEndereco;
            final String finalError = errorMessage;

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);

                if (finalError != null) {
                    Toast.makeText(MainActivity.this, finalError, Toast.LENGTH_SHORT).show();
                } else if (finalResult == null) {
                    Toast.makeText(MainActivity.this, R.string.toast_cep_not_found, Toast.LENGTH_SHORT).show();
                } else {
                    displayEnderecoResult(finalResult);
                    HistoryStorage.addEndereco(MainActivity.this, finalResult);
                }
            });
        });
    }

    private void displayEnderecoResult(Endereco endereco) {
        if (endereco == null) return;

        textResultCep.setText(endereco.getCep());
        textResultLogradouro.setText(endereco.getLogradouro());
        textResultBairro.setText(endereco.getBairro());
        textResultCidadeUf.setText(endereco.getCidadeEstado());
        textResultDdd.setText(endereco.getDdd());

        cardResult.setVisibility(View.VISIBLE);
    }

    private void abrirHistorico() {
        Intent intent = new Intent(this, HistoryActivity.class);
        historyLauncher.launch(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Structure SWITCH / CASE or IF for Menu item selection
        if (id == R.id.action_history) {
            abrirHistorico();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}