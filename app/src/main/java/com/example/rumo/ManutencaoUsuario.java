package com.example.rumo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rumo.dao.CurriculoDAO;
import com.example.rumo.model.Curriculo;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

public class ManutencaoUsuario extends AppCompatActivity {

    private TextInputEditText editNome, editBairro, editTelefone, editEmail, editLink, editInstituicao, editPeriodo, editStatus, editHabilidade, editExperiencia, editResumo;
    private MultiAutoCompleteTextView editAreaCursos;
    private Button btnSalvar, btnSair;

    private CurriculoDAO dao;
    private Curriculo curriculoAtual;
    private final String SEPARADOR = "##";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manutencao_usuario);

        dao = new CurriculoDAO(this);
        inicializarComponentes();
        configurarAbas();
        configurarCursosMultiplos();

        curriculoAtual = (Curriculo) getIntent().getSerializableExtra("curriculo_selecionado");
        if (curriculoAtual == null) {
            List<Curriculo> lista = dao.obterTodos();
            if (lista != null && !lista.isEmpty()) curriculoAtual = lista.get(0);
        }

        if (curriculoAtual != null) preencherDadosNaTela();

        btnSalvar.setOnClickListener(v -> salvarCurriculo());
        btnSair.setOnClickListener(v -> deslogarUsuario());
    }

    private void inicializarComponentes() {
        editNome = findViewById(R.id.editNome);
        editBairro = findViewById(R.id.editBairro);
        editTelefone = findViewById(R.id.editTelefone);
        editEmail = findViewById(R.id.editEmail);
        editAreaCursos = findViewById(R.id.editAreaCursos);
        editInstituicao = findViewById(R.id.editInstituicao);
        editPeriodo = findViewById(R.id.editPeriodo);
        editStatus = findViewById(R.id.editStatus);
        editHabilidade = findViewById(R.id.editHabilidade);
        editExperiencia = findViewById(R.id.editExperiencia);
        editResumo = findViewById(R.id.editResumo);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnSair = findViewById(R.id.btnSair);
    }

    private void deslogarUsuario() {
        Intent intent = new Intent(this, LoginCadastro.class); // Mude p/ sua activity de login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void preencherDadosNaTela() {
        if (curriculoAtual.getDadosPessoais() != null) {
            String[] d = curriculoAtual.getDadosPessoais().split(SEPARADOR);
            if (d.length > 0) editNome.setText(d[0]);
            if (d.length > 1) editBairro.setText(d[1]);
            if (d.length > 2) editTelefone.setText(d[2]);
            if (d.length > 3) editEmail.setText(d[3]);
        }
        if (curriculoAtual.getFormacao() != null) {
            String[] f = curriculoAtual.getFormacao().split(SEPARADOR);
            if (f.length > 0) editInstituicao.setText(f[0]);
            if (f.length > 1) editPeriodo.setText(f[1]);
            if (f.length > 2) editStatus.setText(f[2]);
        }
        editAreaCursos.setText(curriculoAtual.getObjetivo());
        editHabilidade.setText(curriculoAtual.getHabilidade());
        editExperiencia.setText(curriculoAtual.getExperiencia());
        editResumo.setText(curriculoAtual.getResumo());
    }

    private void salvarCurriculo() {
        if (editNome.getText().toString().isEmpty() || editEmail.getText().toString().isEmpty()) {
            Toast.makeText(this, "Nome e E-mail obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        String dados = editNome.getText() + SEPARADOR + editBairro.getText() + SEPARADOR + editTelefone.getText() + SEPARADOR + editEmail.getText();
        String formacao = editInstituicao.getText() + SEPARADOR + editPeriodo.getText() + SEPARADOR + editStatus.getText();

        boolean isNovo = (curriculoAtual == null);
        if (isNovo) curriculoAtual = new Curriculo();

        curriculoAtual.setDadosPessoais(dados);
        curriculoAtual.setFormacao(formacao);
        curriculoAtual.setObjetivo(editAreaCursos.getText().toString());
        curriculoAtual.setHabilidade(editHabilidade.getText().toString());
        curriculoAtual.setExperiencia(editExperiencia.getText().toString());
        curriculoAtual.setResumo(editResumo.getText().toString());

        if (isNovo) dao.Insert(curriculoAtual); else dao.update(curriculoAtual);
        Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void configurarCursosMultiplos() {
        String[] c = {"Administração", "TI", "Logística", "Engenharia"};
        editAreaCursos.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, c));
        editAreaCursos.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    private void configurarAbas() {
        // Vincula as abas (cabeçalhos) aos seus respectivos conteúdos
        configurarAba(findViewById(R.id.btnAbaDadosPessoais), findViewById(R.id.conteudoDadosPessoais), findViewById(R.id.setaDadosPessoais));
        configurarAba(findViewById(R.id.btnAbaCursos), findViewById(R.id.conteudoCursos), findViewById(R.id.setaCursos));
        configurarAba(findViewById(R.id.btnAbaFormacao), findViewById(R.id.conteudoFormacao), findViewById(R.id.setaFormacao));
        configurarAba(findViewById(R.id.btnAbaHabilidade), findViewById(R.id.conteudoHabilidade), findViewById(R.id.setaHabilidade));
        configurarAba(findViewById(R.id.btnAbaExperiencia), findViewById(R.id.conteudoExperiencia), findViewById(R.id.setaExperiencia));
    }

    private void configurarAba(View b, View c, TextView s) {
        if (b != null) b.setOnClickListener(v -> {
            boolean vsv = c.getVisibility() == View.VISIBLE;
            c.setVisibility(vsv ? View.GONE : View.VISIBLE);
            s.setText(vsv ? "▼" : "▲");
        });
    }
}