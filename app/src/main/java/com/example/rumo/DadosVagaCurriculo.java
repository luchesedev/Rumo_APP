package com.example.rumo;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.io.font.constants.StandardFonts;

import java.io.IOException;
import java.io.OutputStream;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class DadosVagaCurriculo extends AppCompatActivity {

    private TextInputEditText inputCargo, inputDescricao, inputPalavrasChave;
    private Button btnContinuar;
    private String formatoEscolhido;
    private View loadingOverlay; // opcional: overlay de loading no XML

    // ⚠️ ATENÇÃO: Mova essa chave para um arquivo local.properties ou para um backend
    // Nunca a deixe exposta em produção — qualquer um pode extrair do APK compilado.
    private static final String API_KEY = "AIzaSyD4IGtNxy3eLCJI9zo0KqY8IMp-27VdcpE";
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

        // Recupera o formato vindo da EscolhaCurriculo
        formatoEscolhido = "ATS"; // valor padrão
        if (getIntent().getExtras() != null) {
            String extra = getIntent().getStringExtra("FORMATO_CURRICULO");
            if (extra != null && !extra.isEmpty()) {
                formatoEscolhido = extra;
            }
        }

        inputCargo = findViewById(R.id.info1Input);
        inputDescricao = findViewById(R.id.info2Input);
        inputPalavrasChave = findViewById(R.id.info3Input);
        btnContinuar = findViewById(R.id.continuar);

        btnContinuar.setOnClickListener(view -> {
            String cargo = inputCargo.getText().toString().trim();
            String descricao = inputDescricao.getText().toString().trim();
            String palavrasChave = inputPalavrasChave.getText().toString().trim();

            if (cargo.isEmpty()) {
                inputCargo.setError("Informe o cargo da vaga");
                inputCargo.requestFocus();
                return;
            }
            if (descricao.isEmpty()) {
                inputDescricao.setError("Informe a descrição da vaga");
                inputDescricao.requestFocus();
                return;
            }

            // Desabilita botão durante a geração para evitar cliques duplos
            btnContinuar.setEnabled(false);
            btnContinuar.setText("Gerando...");

            gerarCurriculoComIA(cargo, descricao, palavrasChave, formatoEscolhido);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chamada à API Gemini
    // ─────────────────────────────────────────────────────────────────────────

    private void gerarCurriculoComIA(String cargo, String descricao, String palavras, String formato) {

        // Dados fictícios do candidato — substitua por dados reais do seu usuário
        String dadosCandidato = "Nome: João Silva\n"
                + "Email: joao@email.com\n"
                + "Telefone: (11) 99999-9999\n"
                + "Formação: Graduação em Análise e Desenvolvimento de Sistemas\n"
                + "Experiência: 1 ano em desenvolvimento de software, testes e suporte técnico";

        String instrucaoFormato = formato.equalsIgnoreCase("ATS")
                ? "Use seções limpas (RESUMO, EXPERIÊNCIA, FORMAÇÃO, HABILIDADES). "
                + "Sem tabelas, sem colunas, sem símbolos especiais. Texto direto."
                : "Escreva um parágrafo de perfil cativante, destaque conquistas com números, "
                + "use linguagem mais pessoal e envolvente.";

        String prompt = "Você é um recrutador profissional sênior. "
                + "Crie um currículo completo com base nos dados abaixo.\n\n"
                + "== DADOS DO CANDIDATO ==\n" + dadosCandidato + "\n\n"
                + "== VAGA ==\n"
                + "Cargo: " + cargo + "\n"
                + "Descrição: " + descricao + "\n"
                + "Palavras-chave: " + (palavras.isEmpty() ? "não informadas" : palavras) + "\n\n"
                + "== FORMATO: " + formato.toUpperCase() + " ==\n"
                + instrucaoFormato + "\n\n"
                + "Retorne APENAS o texto do currículo. "
                + "Sem comentários, sem introduções, sem backticks ou formatação markdown.";

        try {
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

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

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
                    Log.e("GEMINI", "Falha na chamada", e);
                    runOnUiThread(() -> {
                        reativarBotao();
                        mostrarErro("Erro de conexão. Verifique sua internet e tente novamente.");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Garante que o body seja fechado mesmo em caso de erro
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            runOnUiThread(() -> {
                                reativarBotao();
                                mostrarErro("Erro na API do Gemini (código " + response.code() + ").");
                            });
                            return;
                        }

                        String rawJson = responseBody.string();
                        try {
                            JSONObject jsonResponse = new JSONObject(rawJson);
                            String curriculoTexto = jsonResponse
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            runOnUiThread(() -> salvarComoPdf(curriculoTexto, cargo, formato));

                        } catch (Exception e) {
                            Log.e("GEMINI", "Erro ao parsear resposta: " + rawJson, e);
                            runOnUiThread(() -> {
                                reativarBotao();
                                mostrarErro("Resposta inesperada da IA. Tente novamente.");
                            });
                        }
                    }
                }
            });

        } catch (Exception e) {
            Log.e("GEMINI", "Erro ao montar requisição", e);
            reativarBotao();
            mostrarErro("Erro interno ao preparar a requisição.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Geração e salvamento do PDF
    // ─────────────────────────────────────────────────────────────────────────

    private void salvarComoPdf(String conteudo, String cargo, String formato) {
        // Nome do arquivo: curriculo_Desenvolvedor_Android_ATS.pdf
        String nomeArquivo = "curriculo_"
                + cargo.replaceAll("[^a-zA-Z0-9À-ú]", "_")
                + "_" + formato + ".pdf";

        try {
            OutputStream outputStream = obterOutputStreamParaPdf(nomeArquivo);
            if (outputStream == null) {
                reativarBotao();
                mostrarErro("Não foi possível criar o arquivo PDF no dispositivo.");
                return;
            }

            gerarPdf(outputStream, conteudo, cargo, formato, nomeArquivo);

        } catch (Exception e) {
            Log.e("PDF", "Erro ao gerar PDF", e);
            reativarBotao();
            mostrarErro("Erro ao gerar o PDF: " + e.getMessage());
        }
    }

    /**
     * Retorna um OutputStream compatível com Android 9 (API 28) e Android 10+ (API 29+).
     * No Android 10+, usa MediaStore (sem necessidade de permissão WRITE_EXTERNAL_STORAGE).
     * No Android 9 e abaixo, salva em Downloads (requer a permissão no Manifest).
     */
    private OutputStream obterOutputStreamParaPdf(String nomeArquivo) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ — usa MediaStore, sem permissão extra necessária
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, nomeArquivo);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return null;

            // Guarda a URI para abrir o arquivo depois
            pdfUriParaAbrir = uri;
            return getContentResolver().openOutputStream(uri);
        } else {
            // Android 9 e abaixo — precisa de WRITE_EXTERNAL_STORAGE no Manifest
            java.io.File pastaDownloads = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!pastaDownloads.exists()) pastaDownloads.mkdirs();

            java.io.File arquivo = new java.io.File(pastaDownloads, nomeArquivo);
            pdfUriParaAbrir = Uri.fromFile(arquivo);
            return new java.io.FileOutputStream(arquivo);
        }
    }

    // Armazena a URI do PDF gerado para abrir depois
    private Uri pdfUriParaAbrir;

    /**
     * Gera o PDF usando iText7.
     * O texto retornado pela IA é dividido por linhas. Linhas em MAIÚSCULAS
     * são tratadas como títulos de seção (negrito + cor verde).
     */
    private void gerarPdf(OutputStream outputStream, String conteudo,
                          String cargo, String formato, String nomeArquivo) throws Exception {

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        document.setMargins(50, 50, 50, 50);

        PdfFont fontNegrito = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // ── Cabeçalho do PDF ────────────────────────────────────────────────
        Paragraph titulo = new Paragraph("CURRÍCULO — " + cargo.toUpperCase())
                .setFont(fontNegrito)
                .setFontSize(16)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(46, 125, 82)) // #2E7D52
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Formato: " + formato)
                .setFont(fontNormal)
                .setFontSize(10)
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(95, 94, 90)) // #5F5E5A
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitulo);

        // ── Conteúdo gerado pela IA ──────────────────────────────────────────
        String[] linhas = conteudo.split("\n");

        for (String linha : linhas) {
            String linhaTrim = linha.trim();

            if (linhaTrim.isEmpty()) {
                // Linha em branco vira espaço
                document.add(new Paragraph(" ").setFontSize(4));
                continue;
            }

            // Detecta título de seção: linha toda em maiúsculas com pelo menos 3 chars
            boolean ehTitulo = linhaTrim.equals(linhaTrim.toUpperCase())
                    && linhaTrim.length() >= 3
                    && !linhaTrim.matches(".*\\d.*"); // não é linha numérica

            if (ehTitulo) {
                // Separador antes do título (exceto no início)
                SolidLine line = new SolidLine(0.5f);
                line.setColor(new com.itextpdf.kernel.colors.DeviceRgb(46, 125, 82));
                document.add(new LineSeparator(line).setMarginTop(8).setMarginBottom(4));

                document.add(new Paragraph(linhaTrim)
                        .setFont(fontNegrito)
                        .setFontSize(12)
                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(27, 94, 54)) // #1B5E36
                        .setMarginBottom(4));
            } else {
                document.add(new Paragraph(linhaTrim)
                        .setFont(fontNormal)
                        .setFontSize(11)
                        .setFontColor(ColorConstants.BLACK)
                        .setMarginBottom(2));
            }
        }

        // ── Rodapé ───────────────────────────────────────────────────────────
        document.add(new Paragraph("\n"));
        SolidLine rodapeLine = new SolidLine(0.5f);
        rodapeLine.setColor(new com.itextpdf.kernel.colors.DeviceRgb(168, 213, 187)); // #A8D5BB
        document.add(new LineSeparator(rodapeLine));

        document.add(new Paragraph("Gerado pelo app Rumo")
                .setFont(fontNormal)
                .setFontSize(8)
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(168, 213, 187))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(4));

        document.close();

        // ── Notifica o usuário ────────────────────────────────────────────────
        runOnUiThread(() -> {
            reativarBotao();
            mostrarSucessoComAbertura(nomeArquivo);
        });
    }



    private void reativarBotao() {
        btnContinuar.setEnabled(true);
        btnContinuar.setText("Criar Currículo");
    }

    private void mostrarErro(String mensagem) {
        new AlertDialog.Builder(this)
                .setTitle("Algo deu errado")
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }

    private void mostrarSucessoComAbertura(String nomeArquivo) {
        new AlertDialog.Builder(this)
                .setTitle("✅ Currículo gerado!")
                .setMessage("O arquivo \"" + nomeArquivo + "\" foi salvo em Downloads.")
                .setPositiveButton("Abrir PDF", (dialog, which) -> abrirPdf())
                .setNegativeButton("Fechar", null)
                .show();
    }

    private void abrirPdf() {
        if (pdfUriParaAbrir == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUriParaAbrir, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir com..."));
        } catch (Exception e) {
            Toast.makeText(this,
                    "Nenhum leitor de PDF instalado. O arquivo está em Downloads.",
                    Toast.LENGTH_LONG).show();
        }
    }
}