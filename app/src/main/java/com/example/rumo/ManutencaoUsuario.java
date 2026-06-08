package com.example.rumo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ManutencaoUsuario extends AppCompatActivity {

    // Declaração dos componentes do ecrã
    private TextInputEditText editNome, editContato, editFaculdade, editHabilidades, editExperiencia;
    private Spinner spinnerTipoCurriculo, spinnerTema;
    private Button btnSalvar; // Alterado para Button normal para alinhar com o XML atual

    // Declaração do ajudante da base de dados e ID do utilizador
    private DatabaseHelper db;
    private int idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manutencao_usuario);

        // Inicializa a ligação à base de dados
        db = new DatabaseHelper(this);

        // Recupera o ID do utilizador que iniciou a sessão
        idUsuarioLogado = getIntent().getIntExtra("ID_USUARIO", -1);

        // Vincula as variáveis Java aos IDs do XML
        inicializarComponentes();

        // Configura as opções dos Spinners
        configurarSpinners();

        // Configura o clique do botão para guardar as alterações
        btnSalvar.setOnClickListener(v -> salvarAlteracoes());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verifica e preenche os dados automaticamente sempre que o ecrã fica ativo
        verificarEPreencherDados();
    }

    private void inicializarComponentes() {
        editNome = findViewById(R.id.editNome);
        editContato = findViewById(R.id.editContato);
        editFaculdade = findViewById(R.id.editFaculdade);
        editHabilidades = findViewById(R.id.editHabilidades);
        editExperiencia = findViewById(R.id.editExperiencia);
        spinnerTipoCurriculo = findViewById(R.id.spinnerTipoCurriculo);
        spinnerTema = findViewById(R.id.spinnerTema);
        btnSalvar = findViewById(R.id.btnSalvar);
    }

    private void configurarSpinners() {
        // Opções para o Tipo de Currículo
        String[] tiposCurriculo = {"Humanizado", "Template Tradicional", "Criativo", "Técnico"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposCurriculo);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoCurriculo.setAdapter(adapterTipo);

        // Opções para os Temas
        String[] temas = {"Minimalista", "Moderno", "Corporativo", "Elegante"};
        ArrayAdapter<String> adapterTema = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, temas);
        adapterTema.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTema.setAdapter(adapterTema);
    }

    private void verificarEPreencherDados() {
        if (idUsuarioLogado == -1) {
            Toast.makeText(this, "Erro de autenticação. Inicie sessão novamente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = db.buscarDadosUsuario(idUsuarioLogado);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                editNome.setText(cursor.getString(cursor.getColumnIndexOrThrow("nome")));
                editContato.setText(cursor.getString(cursor.getColumnIndexOrThrow("contato")));
                editFaculdade.setText(cursor.getString(cursor.getColumnIndexOrThrow("faculdade")));
                editHabilidades.setText(cursor.getString(cursor.getColumnIndexOrThrow("habilidades")));
                editExperiencia.setText(cursor.getString(cursor.getColumnIndexOrThrow("experiencia")));

                String tipoSalvo = cursor.getString(cursor.getColumnIndexOrThrow("tipo_curriculo"));
                String temaSalvo = cursor.getString(cursor.getColumnIndexOrThrow("tema"));

                setSpinnerSelection(spinnerTipoCurriculo, tipoSalvo);
                setSpinnerSelection(spinnerTema, temaSalvo);

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        } else {
            if (cursor != null) cursor.close();

            Toast.makeText(this, "Por favor, preencha o seu perfil primeiro.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(ManutencaoUsuario.this, DadosVagaCurriculo.class);
            intent.putExtra("ID_USUARIO", idUsuarioLogado);
            startActivity(intent);
        }
    }

    private void salvarAlteracoes() {
        String nome = editNome.getText().toString().trim();
        String contato = editContato.getText().toString().trim();
        String faculdade = editFaculdade.getText().toString().trim();
        String habilidades = editHabilidades.getText().toString().trim();
        String experiencia = editExperiencia.getText().toString().trim();

        String tipoCurriculo = spinnerTipoCurriculo.getSelectedItem().toString();
        String tema = spinnerTema.getSelectedItem().toString();

        if (nome.isEmpty() || contato.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha pelo menos o Nome e o Contacto.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean sucesso = db.atualizarDados(
                idUsuarioLogado, nome, contato, faculdade,
                habilidades, experiencia, tipoCurriculo, tema
        );

        if (sucesso) {
            Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            finish(); // Fecha a atividade e retorna ao ecrã anterior após guardar
        } else {
            Toast.makeText(this, "Erro ao atualizar o perfil. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSpinnerSelection(Spinner spinner, String valor) {
        if (valor == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(valor)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}