package com.example.localizacep;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Endereco implements Serializable {
    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    private String localidade;
    private String uf;
    private String ddd;

    public Endereco() {
    }

    public Endereco(String cep, String logradouro, String complemento, String bairro, String localidade, String uf, String ddd) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.complemento = complemento;
        this.bairro = bairro;
        this.localidade = localidade;
        this.uf = uf;
        this.ddd = ddd;
    }

    public String getCep() {
        return cep != null ? cep : "";
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return (logradouro != null && !logradouro.trim().isEmpty()) ? logradouro : "Nã⁠o informado";
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getComplemento() {
        return complemento != null ? complemento : "";
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return (bairro != null && !bairro.trim().isEmpty()) ? bairro : "Nã⁠o informado";
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getLocalidade() {
        return localidade != null ? localidade : "";
    }

    public void setLocalidade(String localidade) {
        this.localidade = localidade;
    }

    public String getUf() {
        return uf != null ? uf : "";
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getDdd() {
        return (ddd != null && !ddd.trim().isEmpty()) ? ddd : "N/A";
    }

    public void setDdd(String ddd) {
        this.ddd = ddd;
    }

    public String getCidadeEstado() {
        if (getLocalidade().isEmpty() && getUf().isEmpty()) {
            return "Nã⁠o informado";
        }
        if (getUf().isEmpty()) {
            return getLocalidade();
        }
        return getLocalidade() + " / " + getUf();
    }

    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("cep", getCep());
            json.put("logradouro", getLogradouro());
            json.put("complemento", getComplemento());
            json.put("bairro", getBairro());
            json.put("localidade", getLocalidade());
            json.put("uf", getUf());
            json.put("ddd", getDdd());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static Endereco fromJsonObject(JSONObject json) {
        if (json == null) return null;
        Endereco endereco = new Endereco();
        endereco.setCep(json.optString("cep", ""));
        endereco.setLogradouro(json.optString("logradouro", ""));
        endereco.setComplemento(json.optString("complemento", ""));
        endereco.setBairro(json.optString("bairro", ""));
        endereco.setLocalidade(json.optString("localidade", ""));
        endereco.setUf(json.optString("uf", ""));
        endereco.setDdd(json.optString("ddd", ""));
        return endereco;
    }
}