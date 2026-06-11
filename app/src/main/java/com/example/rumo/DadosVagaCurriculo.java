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
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.geom.PageSize;
import com.example.rumo.BuildConfig;
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
    private static final String API_KEY = "AQ.Ab8RN6JmaCz3-dweAWwR-8rjyPwFPSTohrwO-Z0PVQ47Yi7tMw";
    // No DadosVagaCurriculo.java, linha do GEMINI_URL:

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + BuildConfig.GEMINI_API_KEY;

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
                            int codigo = response.code();
                            String mensagem;

                            if (codigo == 403) {
                                mensagem = "Acesso negado pela API (403). Verifique se a chave está correta e o projeto está ativo.";
                            } else if (codigo == 429) {
                                mensagem = "Limite de requisições atingido. Aguarde 1 minuto e tente novamente.";
                            } else {
                                mensagem = "Erro na API do Gemini (código " + codigo + ").";
                            }

                            runOnUiThread(() -> {
                                reativarBotao();
                                mostrarErro(mensagem);
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
    private void salvarComoPdf(String conteudo, String cargo, String formato) {
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

    private OutputStream obterOutputStreamParaPdf(String nomeArquivo) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, nomeArquivo);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return null;

            pdfUriParaAbrir = uri;
            return getContentResolver().openOutputStream(uri);
        } else {
            java.io.File pastaDownloads = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!pastaDownloads.exists()) pastaDownloads.mkdirs();

            java.io.File arquivo = new java.io.File(pastaDownloads, nomeArquivo);
            pdfUriParaAbrir = Uri.fromFile(arquivo);
            return new java.io.FileOutputStream(arquivo);
        }
    }

    private Uri pdfUriParaAbrir;

    // ─────────────────────────────────────────────────────────────────────────
    // Geração do PDF com estrutura profissional baseada no template
    // ─────────────────────────────────────────────────────────────────────────

    private void gerarPdf(OutputStream outputStream, String conteudo,
                          String cargo, String formato, String nomeArquivo) throws Exception {

        // Cores da paleta (cinza escuro para títulos de seção, como no template)
        com.itextpdf.kernel.colors.DeviceRgb COR_TITULO_SECAO   = new com.itextpdf.kernel.colors.DeviceRgb(60, 60, 60);   // #3C3C3C — fundo do cabeçalho de seção
        com.itextpdf.kernel.colors.DeviceRgb COR_TEXTO_SECAO    = new com.itextpdf.kernel.colors.DeviceRgb(255, 255, 255); // branco — texto do cabeçalho
        com.itextpdf.kernel.colors.DeviceRgb COR_SUBTITULO_INFO = new com.itextpdf.kernel.colors.DeviceRgb(80, 80, 80);    // cinza médio — dados pessoais
        com.itextpdf.kernel.colors.DeviceRgb COR_TEXTO_NORMAL   = new com.itextpdf.kernel.colors.DeviceRgb(30, 30, 30);    // quase preto — corpo
        com.itextpdf.kernel.colors.DeviceRgb COR_VERDE_RUMO     = new com.itextpdf.kernel.colors.DeviceRgb(46, 125, 82);   // #2E7D52 — rodapé

        PdfWriter writer     = new PdfWriter(outputStream);
        PdfDocument pdfDoc   = new PdfDocument(writer);
        // Usa tamanho A4
        pdfDoc.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4);
        Document document = new Document(pdfDoc);
        document.setMargins(45, 50, 45, 50);

        PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        // ── Parseia o conteúdo da IA em blocos estruturados ──────────────────
        CurriculoEstruturado estrutura = parsearCurriculo(conteudo);

        // ── 1. BLOCO DO NOME (grande, negrito, como no template) ─────────────
        Paragraph nomeParagrafo = new Paragraph(estrutura.nome)
                .setFont(fontBold)
                .setFontSize(26)
                .setFontColor(COR_TEXTO_NORMAL)
                .setMarginBottom(2);
        document.add(nomeParagrafo);

        // ── 2. DADOS PESSOAIS em linha (estado civil, nascimento, endereço) ──
        if (!estrutura.dadosPessoais.isEmpty()) {
            Paragraph dadosPessoaisParagrafo = new Paragraph(estrutura.dadosPessoais)
                    .setFont(fontNormal)
                    .setFontSize(9)
                    .setFontColor(COR_SUBTITULO_INFO)
                    .setMarginBottom(12);
            document.add(dadosPessoaisParagrafo);
        }

        // Linha separadora fina após o cabeçalho
        adicionarSeparador(document, COR_VERDE_RUMO, 1f);

        // ── 3. SEÇÕES DO CURRÍCULO ────────────────────────────────────────────
        for (SecaoCurriculo secao : estrutura.secoes) {

            // Cabeçalho da seção — fundo cinza escuro, texto branco, negrito
            // (estrutura idêntica ao template de referência)
            com.itextpdf.layout.element.Table tabelaTitulo =
                    new com.itextpdf.layout.element.Table(
                            com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{1}))
                            .useAllAvailableWidth()
                            .setMarginTop(14)
                            .setMarginBottom(4);

            com.itextpdf.layout.element.Cell celulaT = new com.itextpdf.layout.element.Cell()
                    .setBackgroundColor(COR_TITULO_SECAO)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .setPadding(4)
                    .setPaddingLeft(6);

            celulaT.add(new Paragraph(secao.titulo)
                    .setFont(fontBold)
                    .setFontSize(10)
                    .setFontColor(COR_TEXTO_SECAO)
                    .setMarginBottom(0));

            tabelaTitulo.addCell(celulaT);
            document.add(tabelaTitulo);

            // Conteúdo da seção — processa linha a linha
            for (LinhaConteudo linha : secao.linhas) {
                switch (linha.tipo) {

                    case BULLET:
                        // Bullet point com "•" manual (compatível com todas fontes)
                        Paragraph bullet = new Paragraph()
                                .setFont(fontNormal)
                                .setFontSize(10)
                                .setFontColor(COR_TEXTO_NORMAL)
                                .setMarginBottom(2)
                                .setMarginLeft(12)
                                .setFirstLineIndent(-10);
                        bullet.add("•  " + linha.texto);
                        document.add(bullet);
                        break;

                    case SUBTITULO:
                        // Ex: "Consultor Sênior — Empresa XYZ (Jan 2020 – atual)"
                        // Negrito, levemente maior
                        document.add(new Paragraph(linha.texto)
                                .setFont(fontBold)
                                .setFontSize(10)
                                .setFontColor(COR_TEXTO_NORMAL)
                                .setMarginTop(6)
                                .setMarginBottom(1));
                        break;

                    case EMPRESA:
                        // Ex: "Empresa: Nome da Empresa"
                        document.add(new Paragraph(linha.texto)
                                .setFont(fontBold)
                                .setFontSize(9)
                                .setFontColor(COR_SUBTITULO_INFO)
                                .setMarginBottom(2));
                        break;

                    case NORMAL:
                    default:
                        document.add(new Paragraph(linha.texto)
                                .setFont(fontNormal)
                                .setFontSize(10)
                                .setFontColor(COR_TEXTO_NORMAL)
                                .setMarginBottom(2));
                        break;
                }
            }
        }

        // ── 4. RODAPÉ ─────────────────────────────────────────────────────────
        document.close();

       

        runOnUiThread(() -> {
            reativarBotao();
            mostrarSucessoComAbertura(nomeArquivo);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parser: converte o texto da IA em estrutura organizada
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Nomes de seções que o Gemini costuma gerar — insensível a maiúsculas.
     * Qualquer linha que CONTENHA uma dessas palavras e esteja em maiúsculas
     * ou seguida de ":" será tratada como cabeçalho de seção.
     */
    private static final String[] PALAVRAS_SECAO = {
            "RESUMO", "OBJETIVO", "PERFIL",
            "EXPERIÊNCIA", "EXPERIENCIA", "PROFISSIONAL",
            "FORMAÇÃO", "FORMACAO", "ACADÊMICA", "ACADEMICA",
            "HABILIDADES", "COMPETÊNCIAS", "COMPETENCIAS",
            "CERTIFICAÇÕES", "CERTIFICACOES",
            "IDIOMAS", "CURSOS", "PROJETOS", "CONQUISTAS"
    };

    private CurriculoEstruturado parsearCurriculo(String texto) {
        CurriculoEstruturado estrutura = new CurriculoEstruturado();
        String[] linhas = texto.split("\n");

        SecaoCurriculo secaoAtual = null;
        boolean nomeExtraido = false;

        for (String linha : linhas) {
            String t = linha.trim();
            if (t.isEmpty()) continue;

            // Limpa marcadores markdown que o Gemini às vezes inclui
            // ex: **Texto**, ## Texto, *Texto*
            t = t.replaceAll("\\*\\*(.+?)\\*\\*", "$1")  // **negrito**
                    .replaceAll("\\*(.+?)\\*", "$1")          // *itálico*
                    .replaceAll("^#{1,3}\\s*", "")            // ## títulos markdown
                    .trim();

            if (t.isEmpty()) continue;

            // ── Extrai o nome (primeira linha não-vazia como nome principal) ──
            if (!nomeExtraido) {
                estrutura.nome = t;
                nomeExtraido = true;
                continue;
            }

            // ── Extrai dados pessoais (2ª e 3ª linha, antes da 1ª seção) ────
            if (secaoAtual == null && !ehTituloDeSecao(t)) {
                if (estrutura.dadosPessoais.isEmpty()) {
                    estrutura.dadosPessoais = t;
                } else {
                    estrutura.dadosPessoais += "  |  " + t;
                }
                continue;
            }

            // ── Detecta início de nova seção ─────────────────────────────────
            if (ehTituloDeSecao(t)) {
                secaoAtual = new SecaoCurriculo();
                // Remove ":" do final se houver
                secaoAtual.titulo = t.replaceAll(":$", "").trim().toUpperCase();
                estrutura.secoes.add(secaoAtual);
                continue;
            }

            // ── Adiciona linha ao conteúdo da seção atual ────────────────────
            if (secaoAtual != null) {
                LinhaConteudo lc = new LinhaConteudo();

                if (t.startsWith("-") || t.startsWith("•") || t.startsWith("*")) {
                    // Bullet point — remove o marcador inicial
                    lc.tipo = TipoLinha.BULLET;
                    lc.texto = t.replaceAll("^[-•*]\\s*", "").trim();

                } else if (t.toLowerCase().startsWith("empresa:") ||
                        t.toLowerCase().startsWith("company:")) {
                    lc.tipo = TipoLinha.EMPRESA;
                    lc.texto = t;

                } else if (ehSubtitulo(t)) {
                    // Linha de cargo/período: tem traço (–) ou parêntese com data
                    lc.tipo = TipoLinha.SUBTITULO;
                    lc.texto = t;

                } else {
                    lc.tipo = TipoLinha.NORMAL;
                    lc.texto = t;
                }

                secaoAtual.linhas.add(lc);
            }
        }

        // Fallback: se não extraiu nome, usa o cargo
        if (estrutura.nome == null || estrutura.nome.isEmpty()) {
            estrutura.nome = "Currículo";
        }

        return estrutura;
    }

    /** Verifica se a linha é um título de seção (ex: "EXPERIÊNCIA PROFISSIONAL") */
    private boolean ehTituloDeSecao(String linha) {
        String upper = linha.toUpperCase();

        // Critério 1: linha toda em maiúsculas com pelo menos 4 chars
        boolean todoMaiusculo = linha.equals(linha.toUpperCase())
                && linha.length() >= 4
                && linha.matches("[A-ZÁÉÍÓÚÃÕÂÊÔÇÀ\\s\\-:]+");

        // Critério 2: contém palavra-chave de seção
        boolean contemPalavraChave = false;
        for (String palavra : PALAVRAS_SECAO) {
            if (upper.contains(palavra)) {
                contemPalavraChave = true;
                break;
            }
        }

        return todoMaiusculo || (contemPalavraChave && linha.length() < 60);
    }

    /** Verifica se é uma linha de subtítulo (cargo + período, com traço ou parêntese) */
    private boolean ehSubtitulo(String linha) {
        return (linha.contains("–") || linha.contains("-") || linha.contains("—"))
                && (linha.matches(".*\\d{4}.*") || linha.toLowerCase().contains("atual")
                || linha.toLowerCase().contains("present"));
    }

    /** Adiciona linha separadora horizontal */
    private void adicionarSeparador(Document doc,
                                    com.itextpdf.kernel.colors.DeviceRgb cor,
                                    float espessura) throws Exception {
        SolidLine sl = new SolidLine(espessura);
        sl.setColor(cor);
        doc.add(new LineSeparator(sl).setMarginBottom(6));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Classes de modelo interno (estrutura do currículo parseado)
    // ─────────────────────────────────────────────────────────────────────────

    private static class CurriculoEstruturado {
        String nome = "";
        String dadosPessoais = "";
        java.util.List<SecaoCurriculo> secoes = new java.util.ArrayList<>();
    }

    private static class SecaoCurriculo {
        String titulo = "";
        java.util.List<LinhaConteudo> linhas = new java.util.ArrayList<>();
    }

    private static class LinhaConteudo {
        TipoLinha tipo = TipoLinha.NORMAL;
        String texto = "";
    }

    private enum TipoLinha {
        NORMAL,    // parágrafo comum
        BULLET,    // item de lista com "•"
        SUBTITULO, // cargo + período (negrito)
        EMPRESA    // "Empresa: Nome"
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