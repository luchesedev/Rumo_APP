package com.example.rumo.api;

import com.example.rumo.model.VagaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface VagaService {

    // Endpoint da JSearch API no RapidAPI
    // Documentação: https://rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch
    @GET("search")
    Call<VagaResponse> buscarVagas(
            @Header("X-RapidAPI-Key")  String apiKey,
            @Header("X-RapidAPI-Host") String apiHost,
            @Query("query")            String query,
            @Query("num_pages")        int paginas,
            @Query("date_posted")      String dataPostagem,
            @Query("country")          String pais,
            @Query("location")         String localizacao // Novo parâmetro
    );
}