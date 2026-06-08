package com.example.rumo.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Curriculo implements Serializable {
    //atributos


    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }
    private int codigo;

    private String dadosPessoais;
    private String objetivo;
    private String experiencia;
    private String habilidade;
    private String formacao;
    private String resumo;

    //Construtor

    public Curriculo() {

    }

    public String getDadosPessoais() {
        return dadosPessoais;
    }

    public void setDadosPessoais(String dadosPessoais) {
        this.dadosPessoais = dadosPessoais;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public String getHabilidade() {
        return habilidade;
    }

    public void setHabilidade(String habilidade) {
        this.habilidade = habilidade;
    }

    public String getFormacao() {
        return formacao;
    }

    public void setFormacao(String formacao) {
        this.formacao = formacao;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }


}