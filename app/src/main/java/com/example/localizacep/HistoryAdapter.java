package com.example.localizacep;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class HistoryAdapter extends ArrayAdapter<Endereco> {

    public HistoryAdapter(@NonNull Context context, @NonNull ArrayList<Endereco> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_history, parent, false);
        }

        Endereco item = getItem(position);

        if (item != null) {
            TextView textCep = convertView.findViewById(R.id.text_history_cep);
            TextView textLogradouro = convertView.findViewById(R.id.text_history_logradouro);
            TextView textDetails = convertView.findViewById(R.id.text_history_details);
            TextView textDdd = convertView.findViewById(R.id.text_history_ddd);

            String cepText = getContext().getString(R.string.label_cep) + " " + item.getCep();
            textCep.setText(cepText);
            textLogradouro.setText(item.getLogradouro());

            String details = item.getBairro() + " • " + item.getCidadeEstado();
            textDetails.setText(details);

            String dddText = getContext().getString(R.string.label_ddd) + " " + item.getDdd();
            textDdd.setText(dddText);
        }

        return convertView;
    }
}