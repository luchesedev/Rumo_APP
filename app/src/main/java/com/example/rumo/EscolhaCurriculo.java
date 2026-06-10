package com.example.rumo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EscolhaCurriculo extends AppCompatActivity {

    private Intent it;
    // Mudamos de Button para View para aceitar o LinearLayout do XML
    private View ats, humanizado;
    private Button voltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_escolha_curriculo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ats = findViewById(R.id.cardAts);
        ats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                it = new Intent(EscolhaCurriculo.this, DadosVagaCurriculo.class);
                // Passa a escolha "ATS" para a próxima tela
                it.putExtra("FORMATO_CURRICULO", "ATS");
                startActivity(it);
            }
        });

        humanizado = findViewById(R.id.cardHuman);
        humanizado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                it = new Intent(EscolhaCurriculo.this, DadosVagaCurriculo.class);
                // Passa a escolha "Humanizado" para a próxima tela
                it.putExtra("FORMATO_CURRICULO", "Humanizado");
                startActivity(it);
            }
        });

        voltar = findViewById(R.id.voltar);
        voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                it = new Intent(EscolhaCurriculo.this, MainActivity.class);
                startActivity(it);
                finish();
            }
        });
    }
}