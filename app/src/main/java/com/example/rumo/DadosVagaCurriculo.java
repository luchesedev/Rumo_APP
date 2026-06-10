package com.example.rumo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class DadosVagaCurriculo extends AppCompatActivity {

    private TextInputEditText inputCargo, inputDescricao, inputPalavrasChave;
    private Button btnContinuar;
    private String formatoEscolhido;

    // Coloque sua chave do Google AI Studio aqui
    private static final String API_KEY = "AQ.Ab8RN6IbmEDActSmqFg-P6du4DvALucJj4BYb26y8-bDeYjNww";
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dados_vaga_curriculo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Recuperar o formato que veio da tela anterior
        if (getIntent().getExtras() != null) {
            formatoEscolhido = getIntent().getStringExtra("FORMATO_CURRICULO");
        } else {
            formatoEscolhido = "ATS"; // Valor padrão caso falhe
        }

        // 2. Inicializar os componentes da tela pelos IDs do seu XML
        inputCargo = findViewById(R.id.info1Input);
        inputDescricao = findViewById(R.id.info2Input);
        inputPalavrasChave = findViewById(R.id.info3Input);
        btnContinuar = findViewById(R.id.continuar);

        // 3. Evento do botão Criar Currículo
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cargo = inputCargo.getText().toString().trim();
                String descricao = inputDescricao.getText().toString().trim();
                String palavrasChave = inputPalavrasChave.getText().toString().trim();

                // Validação simples para não enviar campos vazios
                if (cargo.isEmpty() || descricao.isEmpty()) {
                    Toast.makeText(DadosVagaCurriculo.this, "Por favor, preencha o cargo e a descrição.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Chamar o método que conecta com a IA
                gerarCurriculoComIA(cargo, descricao, palavrasChave, formatoEscolhido);
            }
        });
    }

    private void gerarCurriculoComIA(String cargo, String descricao, String palavras, String formato) {
        // Exibe um alerta de carregamento enquanto a IA pensa
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("A IA está gerando seu currículo " + formato + "...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 📝 IMPORTANTE: Como seu app ainda não tem a tela de dados pessoais do usuário,
        // criei dados fictícios de exemplo no prompt. Depois você substitui pelas variáveis do seu usuário real.
        String dadosFicticiosUsuario = "Nome: João Silva, Email: joao@email.com, Telefone: (11) 99999-9999. "
                + "Formação: Graduação em Análise e Desenvolvimento de Sistemas. "
                + "Experiência: 1 ano atuando com desenvolvimento de software de testes e suporte técnico.";

        // Engenharia do Prompt estruturada
        String prompt = "Você é um recrutador profissional sênior. Crie um currículo baseado nos seguintes dados:\n\n"
                + "DADOS DO CANDIDATO: " + dadosFicticiosUsuario + "\n"
                + "CARGO DESEJADO: " + cargo + "\n"
                + "DESCRIÇÃO DA VAGA: " + descricao + "\n"
                + "PALAVRAS-CHAVE IMPORTANTES: " + palavras + "\n\n"
                + "Formate o currículo estritamente no modelo: " + formato + ".\n"
                + "Se o modelo for ATS, priorize seções limpas, texto direto, sem firulas visuais ou tabelas.\n"
                + "Se for Humanizado, crie um resumo de perfil mais cativante e focado em competências.\n"
                + "Retorne apenas o texto do currículo limpo.";

        try {
            // Montando o JSON padrão da API do Gemini
            JSONObject jsonBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject textObject = new JSONObject();

            textObject.put("text", prompt);
            partsArray.put(textObject);
            contentObject.put("parts", partsArray);
            contentsArray.put(contentObject);
            jsonBody.put("contents", contentsArray);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(GEMINI_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(DadosVagaCurriculo.this, "Erro de conexão: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(progressDialog::dismiss);

                    if (!response.isSuccessful()) {
                        // Pega a mensagem de erro que a API mandou de volta
                        String erroHttp = response.body() != null ? response.body().string() : "Sem corpo de erro";
                        Log.e("IA_ERROR", "Erro HTTP " + response.code() + " - Detalhe da API: " + erroHttp);

                        runOnUiThread(() -> Toast.makeText(DadosVagaCurriculo.this, "Erro na API da IA: " + response.code(), Toast.LENGTH_LONG).show());
                        return; // Para a execução por aqui
                    }

                    // 2. Se deu 200 OK, tenta ler o JSON (Cai no CATCH)
                    try {
                        String responseBody = response.body().string();

                        // LOG VITAL: Veja a estrutura exata do JSON antes do seu código tentar quebrar ele
                        Log.d("IA_RESPONSE", "Resposta bruta da IA: " + responseBody);

                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String curriculoGerado = jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        runOnUiThread(() -> exibirResultadoFinal(curriculoGerado));

                    } catch (Exception e) {
                        Log.e("IA_ERROR", "Erro de Parsing: O JSON retornou diferente do esperado.", e);
                        runOnUiThread(() -> Toast.makeText(DadosVagaCurriculo.this, "Erro ao ler a resposta do currículo", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }

    // Abre um popup com o resultado final gerado pela IA
    private void exibirResultadoFinal(String texto) {
        new AlertDialog.Builder(this)
                .setTitle("Seu Currículo está Pronto!")
                .setMessage(texto)
                .setPositiveButton("Fechar", null)
                .show();
    }
}