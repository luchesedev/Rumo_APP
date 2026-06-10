package com.example.rumo.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VagaResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private List<Vaga> vagas;

    public String getStatus() { return status; }
    public List<Vaga> getVagas() { return vagas; }
}