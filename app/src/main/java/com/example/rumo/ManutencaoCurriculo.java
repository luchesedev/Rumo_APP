package com.example.rumo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rumo.dao.CurriculoDAO;
import com.example.rumo.model.Curriculo;
import com.google.android.material.button.MaterialButton;

public class ManutencaoCurriculo extends AppCompatActivity {

    private EditText etDadosPessoais, etObjetivo, etExperiencia,
            etHabilidade, etFormacao, etResumo;
    private MaterialButton btnSalvar, btnExcluir;
    private ImageView ivVoltar;

    private CurriculoDAO dao;
    private Curriculo curriculoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manutencao_curriculo);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(android.R.id.content), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        vincularViews();

        dao = new CurriculoDAO(this);

        // Recebe o id do currículo enviado pelo CurriculoAdapter
        int curriculoId = getIntent().getIntExtra("curriculo_id", -1);

        if (curriculoId == -1) {
            // Não deveria acontecer, mas protege contra navegação errada
            Toast.makeText(this, "Currículo não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Busca o currículo no banco e preenche os campos
        curriculoAtual = dao.buscarPorId(curriculoId);

        if (curriculoAtual == null) {
            Toast.makeText(this, "Currículo não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        preencherCampos(curriculoAtual);
        configurarBotoes();
    }

    private void vincularViews() {
        etDadosPessoais = findViewById(R.id.etDadosPessoais);
        etObjetivo      = findViewById(R.id.etObjetivo);
        etExperiencia   = findViewById(R.id.etExperiencia);
        etHabilidade    = findViewById(R.id.etHabilidade);
        etFormacao      = findViewById(R.id.etFormacao);
        etResumo        = findViewById(R.id.etResumo);
        btnSalvar       = findViewById(R.id.btnSalvar);
        btnExcluir      = findViewById(R.id.btnExcluir);
        ivVoltar        = findViewById(R.id.ivVoltar);
    }

    private void preencherCampos(Curriculo c) {
        etDadosPessoais.setText(c.getDadosPessoais());
        etObjetivo.setText(c.getObjetivo());
        etExperiencia.setText(c.getExperiencia());
        etHabilidade.setText(c.getHabilidade());
        etFormacao.setText(c.getFormacao());
        etResumo.setText(c.getResumo());
    }

    private void configurarBotoes() {

        // Voltar
        ivVoltar.setOnClickListener(v -> finish());

        // Salvar: atualiza os campos no objeto e persiste no banco
        btnSalvar.setOnClickListener(v -> {
            curriculoAtual.setDadosPessoais(etDadosPessoais.getText().toString().trim());
            curriculoAtual.setObjetivo(etObjetivo.getText().toString().trim());
            curriculoAtual.setExperiencia(etExperiencia.getText().toString().trim());
            curriculoAtual.setHabilidade(etHabilidade.getText().toString().trim());
            curriculoAtual.setFormacao(etFormacao.getText().toString().trim());
            curriculoAtual.setResumo(etResumo.getText().toString().trim());

            // Reconstrói o curriculoGerado com os dados editados
            // para que uma futura regeneração do PDF use os dados atualizados
            curriculoAtual.setCurriculoGerado(reconstruirTexto(curriculoAtual));

            dao.update(curriculoAtual);

            Toast.makeText(this, "Currículo salvo!", Toast.LENGTH_SHORT).show();
            finish(); // volta para a tela anterior e aciona o onResume que recarrega a lista
        });

        // Excluir: pede confirmação antes de deletar
        btnExcluir.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Excluir currículo")
                        .setMessage("Tem certeza que deseja excluir este currículo? Esta ação não pode ser desfeita.")
                        .setPositiveButton("Excluir", (dialog, which) -> {
                            dao.delete(curriculoAtual);
                            Toast.makeText(this, "Currículo excluído.", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show()
        );
    }

    // Reconstrói o texto completo do currículo a partir dos campos editados.
    // Este texto é salvo em curriculoGerado e usado para regenerar o PDF.
    private String reconstruirTexto(Curriculo c) {
        return c.getDadosPessoais() + "\n\n"
                + "OBJETIVO\n" + c.getObjetivo() + "\n\n"
                + "RESUMO\n" + c.getResumo() + "\n\n"
                + "EXPERIÊNCIA PROFISSIONAL\n" + c.getExperiencia() + "\n\n"
                + "FORMAÇÃO ACADÊMICA\n" + c.getFormacao() + "\n\n"
                + "HABILIDADES\n" + c.getHabilidade();
    }
}