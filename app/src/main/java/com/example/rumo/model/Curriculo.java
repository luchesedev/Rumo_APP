package com.example.rumo.model;

import java.io.Serializable;

public class Curriculo implements Serializable {

    private int id;
    private String email;
    private String dadosPessoais;
    private String objetivo;
    private String experiencia;
    private String habilidade;
    private String formacao;
    private String resumo;
    private String curriculoGerado; // ← texto completo gerado pela IA

    public Curriculo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDadosPessoais() { return dadosPessoais; }
    public void setDadosPessoais(String dadosPessoais) { this.dadosPessoais = dadosPessoais; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public String getExperiencia() { return experiencia; }
    public void setExperiencia(String experiencia) { this.experiencia = experiencia; }

    public String getHabilidade() { return habilidade; }
    public void setHabilidade(String habilidade) { this.habilidade = habilidade; }

    public String getFormacao() { return formacao; }
    public void setFormacao(String formacao) { this.formacao = formacao; }

    public String getResumo() { return resumo; }
    public void setResumo(String resumo) { this.resumo = resumo; }

    public String getCurriculoGerado() { return curriculoGerado; }
    public void setCurriculoGerado(String curriculoGerado) { this.curriculoGerado = curriculoGerado; }
}