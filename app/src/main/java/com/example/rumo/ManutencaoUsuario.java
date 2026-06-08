package com.example.rumo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rumo.dao.CurriculoDAO;
import com.example.rumo.model.Curriculo;
import com.google.android.material.textfield.TextInputEditText;

public class ManutencaoUsuario extends AppCompatActivity {

    // Componentes da tela adaptados para o novo XML
    private TextInputEditText editDadosPessoais, editObjetivo, editFormacao, editHabilidade, editExperiencia, editResumo;
    private Button btnSalvar;

    // Conexão com o banco e o objeto que será salvo/editado
    private CurriculoDAO dao;
    private Curriculo curriculoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manutencao_usuario);

        // Inicializa o DAO
        dao = new CurriculoDAO(this);

        inicializarComponentes();

        // Verifica se a tela foi aberta para edição passando um currículo existente
        curriculoAtual = (Curriculo) getIntent().getSerializableExtra("curriculo_selecionado");

        if (curriculoAtual != null) {
            preencherDadosNaTela();
        }

        btnSalvar.setOnClickListener(v -> salvarCurriculo());
    }

    private void inicializarComponentes() {
        editDadosPessoais = findViewById(R.id.editDadosPessoais);
        editObjetivo = findViewById(R.id.editObjetivo);
        editFormacao = findViewById(R.id.editFormacao);
        editHabilidade = findViewById(R.id.editHabilidade);
        editExperiencia = findViewById(R.id.editExperiencia);
        editResumo = findViewById(R.id.editResumo);
        btnSalvar = findViewById(R.id.btnSalvar);
    }

    private void preencherDadosNaTela() {
        // Mapeando diretamente os dados do modelo para os novos componentes correspondentes do XML
        editDadosPessoais.setText(curriculoAtual.getDadosPessoais());
        editObjetivo.setText(curriculoAtual.getObjetivo());
        editFormacao.setText(curriculoAtual.getFormacao());
        editHabilidade.setText(curriculoAtual.getHabilidade());
        editExperiencia.setText(curriculoAtual.getExperiencia());
        editResumo.setText(curriculoAtual.getResumo());
    }

    private void salvarCurriculo() {
        String dadosPessoais = editDadosPessoais.getText().toString().trim();

        // Validação usando o novo campo obrigatório
        if (dadosPessoais.isEmpty()) {
            Toast.makeText(this, "O campo Dados Pessoais é obrigatório.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Se for um currículo novo, instancia o objeto
        boolean isNovo = false;
        if (curriculoAtual == null) {
            curriculoAtual = new Curriculo();
            isNovo = true;
        }

        // Preenchendo o modelo com os dados capturados da tela
        curriculoAtual.setDadosPessoais(dadosPessoais);
        curriculoAtual.setObjetivo(editObjetivo.getText().toString().trim());
        curriculoAtual.setFormacao(editFormacao.getText().toString().trim());
        curriculoAtual.setHabilidade(editHabilidade.getText().toString().trim());
        curriculoAtual.setExperiencia(editExperiencia.getText().toString().trim());
        curriculoAtual.setResumo(editResumo.getText().toString().trim());

        // Executa a operação no Banco de Dados
        try {
            if (isNovo) {
                long resultado = dao.Insert(curriculoAtual);
                if (resultado != -1) {
                    Toast.makeText(this, "Currículo cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Erro ao cadastrar currículo.", Toast.LENGTH_SHORT).show();
                }
            } else {
                dao.update(curriculoAtual);
                Toast.makeText(this, "Currículo atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}