package com.example.rumo.model;


import com.google.gson.annotations.SerializedName;

public class Vaga {

    @SerializedName("job_id")
    private String id;

    @SerializedName("job_title")
    private String titulo;

    @SerializedName("employer_name")
    private String empresa;

    @SerializedName("job_city")
    private String cidade;

    @SerializedName("job_employment_type")
    private String tipo; // FULLTIME, PARTTIME, CONTRACTOR

    @SerializedName("job_apply_link")
    private String linkCandidatura;

    @SerializedName("job_description")
    private String descricao;

    @SerializedName("job_is_remote")
    private boolean remoto;

    // Getters
    public String getId()              { return id; }
    public String getTitulo()          { return titulo; }
    public String getEmpresa()         { return empresa; }
    public String getCidade()          { return cidade; }
    public String getTipo()            { return tipo; }
    public String getLinkCandidatura() { return linkCandidatura; }
    public String getDescricao()       { return descricao; }
    public boolean isRemoto()          { return remoto; }

    // Retorna "Remoto" ou a cidade, para exibir no card
    public String getLocalExibicao() {
        if (remoto) return "Remoto";
        if (cidade != null && !cidade.isEmpty()) return cidade;
        return "Local não informado";
    }
}
