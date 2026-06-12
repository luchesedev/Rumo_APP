package com.example.rumo;

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

import com.example.rumo.BuildConfig;
import com.example.rumo.dao.CurriculoDAO;
import com.example.rumo.model.Curriculo;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DadosVagaCurriculo extends AppCompatActivity {

    private TextInputEditText inputCargo, inputDescricao, inputPalavrasChave;
    private Button btnContinuar;
    private String formatoEscolhido;
    private Uri pdfUriParaAbrir;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="+BuildConfig.GEMINI_API_KEY;

    // ─────────────────────────────────────────────────────────────────────────
    // onCreate
    // ─────────────────────────────────────────────────────────────────────────

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

        formatoEscolhido = "ATS";
        if (getIntent().getExtras() != null) {
            String extra = getIntent().getStringExtra("FORMATO_CURRICULO");
            if (extra != null && !extra.isEmpty()) {
                formatoEscolhido = extra;
            }
        }

        inputCargo       = findViewById(R.id.info1Input);
        inputDescricao   = findViewById(R.id.info2Input);
        inputPalavrasChave = findViewById(R.id.info3Input);
        btnContinuar     = findViewById(R.id.continuar);

        btnContinuar.setOnClickListener(view -> {
            String cargo       = inputCargo.getText().toString().trim();
            String descricao   = inputDescricao.getText().toString().trim();
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

            btnContinuar.setEnabled(false);
            btnContinuar.setText("Gerando...");

            gerarCurriculoComIA(cargo, descricao, palavrasChave, formatoEscolhido);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chamada à API Gemini
    // ─────────────────────────────────────────────────────────────────────────
    private void gerarCurriculoComIA(String cargo, String descricao,
                                     String palavras, String formato) {

        // ── 1. PEGA O E-MAIL DO FIREBASE (Igual à tela de manutenção) ────────────
        String emailUsuario = "";
        com.google.firebase.auth.FirebaseUser usuarioFirebase = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioFirebase != null) {
            emailUsuario = usuarioFirebase.getEmail();
        } else {
            // Fallback caso o Firebase falhe, tenta pegar do SharedPreferences
            emailUsuario = obterEmailUsuarioLogado();
        }

        // ── 2. BUSCA NO BANCO DE DADOS ───────────────────────────────────────────
        CurriculoDAO daoConsulta = new CurriculoDAO(this);
        Curriculo curriculoBase = daoConsulta.buscarPorEmail(emailUsuario);

        String dadosCandidato = "";

        if (curriculoBase != null) {
            // --- SEPARANDO OS DADOS PESSOAIS (Separador ##) ---
            String nome = "Não informado";
            String bairro = "Não informado";
            String telefone = "Não informado";
            String emailBanco = emailUsuario;

            if (curriculoBase.getDadosPessoais() != null && !curriculoBase.getDadosPessoais().isEmpty()) {
                // O uso do Pattern.quote garante que o "##" seja lido como texto e não expressão regular
                String[] dp = curriculoBase.getDadosPessoais().split(java.util.regex.Pattern.quote("##"));
                if (dp.length > 0 && !dp[0].trim().isEmpty()) nome = dp[0];
                if (dp.length > 1 && !dp[1].trim().isEmpty()) bairro = dp[1];
                if (dp.length > 2 && !dp[2].trim().isEmpty()) telefone = dp[2];
                if (dp.length > 3 && !dp[3].trim().isEmpty()) emailBanco = dp[3];
            }

            // --- SEPARANDO A FORMAÇÃO (Separador ##) ---
            String formacaoFormatada = "Não informada";
            if (curriculoBase.getFormacao() != null && !curriculoBase.getFormacao().isEmpty()) {
                String[] form = curriculoBase.getFormacao().split(java.util.regex.Pattern.quote("##"));
                String inst = form.length > 0 ? form[0] : "";
                String per = form.length > 1 ? form[1] : "";
                String stat = form.length > 2 ? form[2] : "";

                // Junta tudo de forma bonita para a IA ler
                formacaoFormatada = inst + " (Período: " + per + " - Status: " + stat + ")";
            }

            // --- MONTANDO O TEXTO PARA A IA ---
            dadosCandidato = "Nome: " + nome + "\n"
                    + "Email: " + emailBanco + "\n"
                    + "Telefone: " + telefone + "\n"
                    + "Endereço/Bairro: " + bairro + "\n"
                    + "Formação Acadêmica: " + formacaoFormatada + "\n"
                    + "Experiência Profissional: " + (curriculoBase.getExperiencia() != null ? curriculoBase.getExperiencia() : "Nenhuma") + "\n"
                    + "Habilidades: " + (curriculoBase.getHabilidade() != null ? curriculoBase.getHabilidade() : "Nenhuma") + "\n"
                    + "Resumo/Perfil Profissional: " + (curriculoBase.getResumo() != null ? curriculoBase.getResumo() : "Não informado");

        } else {
            // Fallback: Se o usuário ainda não tiver dados salvos no banco
            dadosCandidato = "Email: " + emailUsuario + "\n(Candidato sem dados completos no banco. Por favor, crie um currículo focado na vaga e coloque espaços como [Seu Nome Aqui], [Seu Telefone Aqui] para ele preencher depois).";
        }

        Log.d("PROMPT_IA", "Dados que serão enviados para o Gemini:\n" + dadosCandidato);

        // ── A partir daqui o código segue exatamente igual ───────────────────────
        String instrucaoFormato = formato.equalsIgnoreCase("ATS")
                ? "Use seções limpas (RESUMO, EXPERIÊNCIA, FORMAÇÃO, HABILIDADES). "
                + "Sem tabelas, sem colunas, sem símbolos especiais. Texto direto. Certifique-se de colocar Nome, E-mail e Telefone no cabeçalho."
                : "Escreva um parágrafo de perfil cativante, destaque conquistas com números, "
                + "use linguagem mais pessoal e envolvente. Certifique-se de colocar Nome, E-mail e Telefone no cabeçalho.";

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
            JSONObject jsonBody    = new JSONObject();
            JSONArray contentsArray  = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray partsArray   = new JSONArray();
            JSONObject textObject  = new JSONObject();

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

                // ... (Mantenha todo o restante do Callback exatamente como já está no seu código)

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
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            int codigo = response.code();
                            String mensagem;
                            if (codigo == 403) {
                                mensagem = "Acesso negado (403). Verifique a chave da API.";
                            } else if (codigo == 429) {
                                mensagem = "Limite de requisições atingido. Aguarde e tente novamente.";
                            } else {
                                mensagem = "Erro na API do Gemini (código " + codigo + ").";
                            }
                            runOnUiThread(() -> { reativarBotao(); mostrarErro(mensagem); });
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

                            // ── 1. Parseia o texto nos campos estruturados ──────────
                            CurriculoEstruturado estrutura = parsearCurriculo(curriculoTexto);

                            // ── 2. Monta o objeto Curriculo para persistência ────────
                            Curriculo curriculoParaSalvar = new Curriculo();
                            curriculoParaSalvar.setEmail(obterEmailUsuarioLogado());
                            curriculoParaSalvar.setDadosPessoais(estrutura.dadosPessoais);
                            curriculoParaSalvar.setObjetivo(cargo);
                            curriculoParaSalvar.setResumo(
                                    extrairSecao(estrutura, "RESUMO", "PERFIL", "OBJETIVO"));
                            curriculoParaSalvar.setExperiencia(
                                    extrairSecao(estrutura, "EXPERIÊNCIA", "EXPERIENCIA", "PROFISSIONAL"));
                            curriculoParaSalvar.setFormacao(
                                    extrairSecao(estrutura, "FORMAÇÃO", "FORMACAO", "ACADÊMICA"));
                            curriculoParaSalvar.setHabilidade(
                                    extrairSecao(estrutura, "HABILIDADES", "COMPETÊNCIAS", "COMPETENCIAS"));
                            // Texto completo: fonte de verdade para edição e regeneração do PDF
                            curriculoParaSalvar.setCurriculoGerado(curriculoTexto);

                            // ── 3. Salva como um NOVO currículo no banco (Sem sobrescrever) ───────
                            CurriculoDAO dao = new CurriculoDAO(DadosVagaCurriculo.this);
                            long novoId = dao.Insert(curriculoParaSalvar); // Usa o Insert direto para criar uma nova linha
                            curriculoParaSalvar.setId((int) novoId);
                            Log.d("BANCO", "Novo currículo gerado e salvo com id=" + novoId);

// ── 4. Gera o PDF a partir do texto persistido ──────────
                            runOnUiThread(() ->
                                    salvarComoPdf(
                                            curriculoParaSalvar.getCurriculoGerado(),
                                            cargo,
                                            formatoEscolhido
                                    )
                            );

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
    // Extrai o texto de uma seção do currículo pelo nome
    // ─────────────────────────────────────────────────────────────────────────

    // ⚠️ Fora do Callback — método da Activity, acessível em todo o arquivo
    private String extrairSecao(CurriculoEstruturado estrutura, String... titulos) {
        for (SecaoCurriculo secao : estrutura.secoes) {
            for (String titulo : titulos) {
                if (secao.titulo.toUpperCase().contains(titulo.toUpperCase())) {
                    StringBuilder sb = new StringBuilder();
                    for (LinhaConteudo linha : secao.linhas) {
                        sb.append(linha.texto).append("\n");
                    }
                    return sb.toString().trim();
                }
            }
        }
        return "";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // E-mail do usuário logado
    // Substitua pelo seu método real: Firebase Auth, Intent extra, etc.
    // ─────────────────────────────────────────────────────────────────────────

    private String obterEmailUsuarioLogado() {
        return getSharedPreferences("sessao", MODE_PRIVATE)
                .getString("email", "");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Geração do PDF
    // ─────────────────────────────────────────────────────────────────────────

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

            Uri uri = getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
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

    private void gerarPdf(OutputStream outputStream, String conteudo,
                          String cargo, String formato, String nomeArquivo) throws Exception {

        com.itextpdf.kernel.colors.DeviceRgb COR_TITULO_SECAO   = new com.itextpdf.kernel.colors.DeviceRgb(60, 60, 60);
        com.itextpdf.kernel.colors.DeviceRgb COR_TEXTO_SECAO    = new com.itextpdf.kernel.colors.DeviceRgb(255, 255, 255);
        com.itextpdf.kernel.colors.DeviceRgb COR_SUBTITULO_INFO = new com.itextpdf.kernel.colors.DeviceRgb(80, 80, 80);
        com.itextpdf.kernel.colors.DeviceRgb COR_TEXTO_NORMAL   = new com.itextpdf.kernel.colors.DeviceRgb(30, 30, 30);
        com.itextpdf.kernel.colors.DeviceRgb COR_VERDE_RUMO     = new com.itextpdf.kernel.colors.DeviceRgb(46, 125, 82);

        PdfWriter writer   = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4);
        Document document  = new Document(pdfDoc);
        document.setMargins(45, 50, 45, 50);

        PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        CurriculoEstruturado estrutura = parsearCurriculo(conteudo);

        // Nome
        document.add(new Paragraph(estrutura.nome)
                .setFont(fontBold)
                .setFontSize(26)
                .setFontColor(COR_TEXTO_NORMAL)
                .setMarginBottom(2));

        // Dados pessoais
        if (!estrutura.dadosPessoais.isEmpty()) {
            document.add(new Paragraph(estrutura.dadosPessoais)
                    .setFont(fontNormal)
                    .setFontSize(9)
                    .setFontColor(COR_SUBTITULO_INFO)
                    .setMarginBottom(12));
        }

        adicionarSeparador(document, COR_VERDE_RUMO, 1f);

        // Seções
        for (SecaoCurriculo secao : estrutura.secoes) {

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

            for (LinhaConteudo linha : secao.linhas) {
                switch (linha.tipo) {
                    case BULLET:
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
                        document.add(new Paragraph(linha.texto)
                                .setFont(fontBold)
                                .setFontSize(10)
                                .setFontColor(COR_TEXTO_NORMAL)
                                .setMarginTop(6)
                                .setMarginBottom(1));
                        break;

                    case EMPRESA:
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

        document.close();

        runOnUiThread(() -> {
            reativarBotao();
            mostrarSucessoComAbertura(nomeArquivo);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parser
    // ─────────────────────────────────────────────────────────────────────────

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

            t = t.replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                    .replaceAll("\\*(.+?)\\*", "$1")
                    .replaceAll("^#{1,3}\\s*", "")
                    .trim();

            if (t.isEmpty()) continue;

            if (!nomeExtraido) {
                estrutura.nome = t;
                nomeExtraido = true;
                continue;
            }

            if (secaoAtual == null && !ehTituloDeSecao(t)) {
                if (estrutura.dadosPessoais.isEmpty()) {
                    estrutura.dadosPessoais = t;
                } else {
                    estrutura.dadosPessoais += "  |  " + t;
                }
                continue;
            }

            if (ehTituloDeSecao(t)) {
                secaoAtual = new SecaoCurriculo();
                secaoAtual.titulo = t.replaceAll(":$", "").trim().toUpperCase();
                estrutura.secoes.add(secaoAtual);
                continue;
            }

            if (secaoAtual != null) {
                LinhaConteudo lc = new LinhaConteudo();

                if (t.startsWith("-") || t.startsWith("•") || t.startsWith("*")) {
                    lc.tipo  = TipoLinha.BULLET;
                    lc.texto = t.replaceAll("^[-•*]\\s*", "").trim();
                } else if (t.toLowerCase().startsWith("empresa:")
                        || t.toLowerCase().startsWith("company:")) {
                    lc.tipo  = TipoLinha.EMPRESA;
                    lc.texto = t;
                } else if (ehSubtitulo(t)) {
                    lc.tipo  = TipoLinha.SUBTITULO;
                    lc.texto = t;
                } else {
                    lc.tipo  = TipoLinha.NORMAL;
                    lc.texto = t;
                }

                secaoAtual.linhas.add(lc);
            }
        }

        if (estrutura.nome == null || estrutura.nome.isEmpty()) {
            estrutura.nome = "Currículo";
        }

        return estrutura;
    }

    private boolean ehTituloDeSecao(String linha) {
        String upper = linha.toUpperCase();

        boolean todoMaiusculo = linha.equals(linha.toUpperCase())
                && linha.length() >= 4
                && linha.matches("[A-ZÁÉÍÓÚÃÕÂÊÔÇÀ\\s\\-:]+");

        boolean contemPalavraChave = false;
        for (String palavra : PALAVRAS_SECAO) {
            if (upper.contains(palavra)) {
                contemPalavraChave = true;
                break;
            }
        }

        return todoMaiusculo || (contemPalavraChave && linha.length() < 60);
    }

    private boolean ehSubtitulo(String linha) {
        return (linha.contains("–") || linha.contains("-") || linha.contains("—"))
                && (linha.matches(".*\\d{4}.*")
                || linha.toLowerCase().contains("atual")
                || linha.toLowerCase().contains("present"));
    }

    private void adicionarSeparador(Document doc,
                                    com.itextpdf.kernel.colors.DeviceRgb cor,
                                    float espessura) throws Exception {
        SolidLine sl = new SolidLine(espessura);
        sl.setColor(cor);
        doc.add(new LineSeparator(sl).setMarginBottom(6));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Modelos internos do parser
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
        TipoLinha tipo  = TipoLinha.NORMAL;
        String    texto = "";
    }

    private enum TipoLinha { NORMAL, BULLET, SUBTITULO, EMPRESA }

    // ─────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────

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