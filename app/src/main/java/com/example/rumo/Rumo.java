package com.example.rumo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rumo.dao.CurriculoDAO;
import com.example.rumo.model.Curriculo;

import java.util.ArrayList;
import java.util.List;

public class Rumo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rumo);

        configurarCarrossel();
        configurarListaCurriculos();
    }

    private void configurarCarrossel() {
        RecyclerView rvCarrossel = findViewById(R.id.rvCarousel);

        // Dados fictícios do carrossel
        List<CarrosselAdapter.CarrosselItem> itens = new ArrayList<>();
        itens.add(new CarrosselAdapter.CarrosselItem(
                "Nova vaga",
                "Desenvolvedor Android",
                "Empresa XYZ · Remoto",
                Color.parseColor("#1A1A2E")   // azul escuro
        ));
        itens.add(new CarrosselAdapter.CarrosselItem(
                "Em alta",
                "Designer UI/UX",
                "Studio Criativo · São Paulo",
                Color.parseColor("#16213E")   // azul marinho
        ));
        itens.add(new CarrosselAdapter.CarrosselItem(
                "Urgente",
                "Analista de Dados",
                "DataCorp · Híbrido",
                Color.parseColor("#0F3460")   // azul profundo
        ));

        // Configura o RecyclerView horizontal
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvCarrossel.setLayoutManager(layoutManager);
        rvCarrossel.setAdapter(new CarrosselAdapter(this, itens));
    }

    private void configurarListaCurriculos() {
        RecyclerView rvCurriculos = findViewById(R.id.rvCurriculos);

        // Dados fictícios da lista de currículos
        List<CurriculoAdapter.CurriculoItem> itens = new ArrayList<>();
        itens.add(new CurriculoAdapter.CurriculoItem(
                "Currículo Principal",
                "07/06/2026",
                "Completo",
                true
        ));
        itens.add(new CurriculoAdapter.CurriculoItem(
                "Currículo Tech",
                "01/06/2026",
                "Completo",
                true
        ));
        itens.add(new CurriculoAdapter.CurriculoItem(
                "Currículo Freelancer",
                "20/05/2026",
                "Incompleto",
                false
        ));

        // Configura o RecyclerView vertical
        rvCurriculos.setLayoutManager(new LinearLayoutManager(this));
        rvCurriculos.setAdapter(new CurriculoAdapter(this, itens));
    }
}