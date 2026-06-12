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

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;

import java.io.IOException;
import java.io.OutputStream;

public class ManutencaoCurriculo extends AppCompatActivity {

    private EditText etDadosPessoais, etObjetivo, etExperiencia,
            etHabilidade, etFormacao, etResumo;
    private MaterialButton btnSalvar, btnExcluir, btnGerarPdf;
    private ImageView ivVoltar;

    private CurriculoDAO dao;
    private Curriculo curriculoAtual;
    private Uri pdfUriParaAbrir;

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

        int curriculoId = getIntent().getIntExtra("curriculo_id", -1);

        if (curriculoId == -1) {
            Toast.makeText(this, "Currículo não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        btnGerarPdf     = findViewById(R.id.btnGerarPdf);
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

    // ── Método isolado para não repetir código ─────────────────────────────────
    private void salvarDadosNoObjeto() {
        curriculoAtual.setDadosPessoais(etDadosPessoais.getText().toString().trim());
        curriculoAtual.setObjetivo(etObjetivo.getText().toString().trim());
        curriculoAtual.setExperiencia(etExperiencia.getText().toString().trim());
        curriculoAtual.setHabilidade(etHabilidade.getText().toString().trim());
        curriculoAtual.setFormacao(etFormacao.getText().toString().trim());
        curriculoAtual.setResumo(etResumo.getText().toString().trim());

        // Reconstrói o texto completo do currículo
        curriculoAtual.setCurriculoGerado(reconstruirTexto(curriculoAtual));
    }

    private void configurarBotoes() {

        ivVoltar.setOnClickListener(v -> finish());

        // Apenas Salvar e sair
        btnSalvar.setOnClickListener(v -> {
            salvarDadosNoObjeto();
            dao.update(curriculoAtual);
            Toast.makeText(this, "Currículo salvo!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Salvar e em seguida Gerar PDF
        btnGerarPdf.setOnClickListener(v -> {
            // 1. Salva as informações novas
            salvarDadosNoObjeto();
            dao.update(curriculoAtual);

            // 2. Define um nome de arquivo usando o Objetivo
            String nomeBase = curriculoAtual.getObjetivo();
            if (nomeBase == null || nomeBase.isEmpty()) nomeBase = "Meu_Curriculo";

            // 3. Inicia a criação do PDF
            salvarComoPdf(curriculoAtual.getCurriculoGerado(), nomeBase, "Atualizado");
        });

        // Excluir
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

    private String reconstruirTexto(Curriculo c) {
        return c.getDadosPessoais() + "\n\n"
                + "OBJETIVO\n" + c.getObjetivo() + "\n\n"
                + "RESUMO\n" + c.getResumo() + "\n\n"
                + "EXPERIÊNCIA PROFISSIONAL\n" + c.getExperiencia() + "\n\n"
                + "FORMAÇÃO ACADÊMICA\n" + c.getFormacao() + "\n\n"
                + "HABILIDADES\n" + c.getHabilidade();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LÓGICA DE GERAÇÃO DO PDF (Reutilizada da criação)
    // ─────────────────────────────────────────────────────────────────────────

    private void salvarComoPdf(String conteudo, String cargo, String formato) {
        String nomeArquivo = "curriculo_"
                + cargo.replaceAll("[^a-zA-Z0-9À-ú]", "_")
                + "_" + formato + ".pdf";

        try {
            OutputStream outputStream = obterOutputStreamParaPdf(nomeArquivo);
            if (outputStream == null) {
                Toast.makeText(this, "Erro ao criar arquivo.", Toast.LENGTH_SHORT).show();
                return;
            }
            gerarPdf(outputStream, conteudo, nomeArquivo);

        } catch (Exception e) {
            Log.e("PDF", "Erro ao gerar PDF", e);
            Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    private void gerarPdf(OutputStream outputStream, String conteudo, String nomeArquivo) throws Exception {

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

        // Nome / Cabeçalho
        document.add(new Paragraph(estrutura.nome)
                .setFont(fontBold)
                .setFontSize(26)
                .setFontColor(COR_TEXTO_NORMAL)
                .setMarginBottom(2));

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

        runOnUiThread(() -> mostrarSucessoComAbertura(nomeArquivo));
    }

    private CurriculoEstruturado parsearCurriculo(String texto) {
        CurriculoEstruturado estrutura = new CurriculoEstruturado();
        String[] linhas = texto.split("\n");

        SecaoCurriculo secaoAtual = null;
        boolean nomeExtraido = false;

        String[] PALAVRAS_SECAO = {"RESUMO", "OBJETIVO", "PERFIL", "EXPERIÊNCIA", "EXPERIENCIA", "PROFISSIONAL", "FORMAÇÃO", "FORMACAO", "ACADÊMICA", "ACADEMICA", "HABILIDADES", "COMPETÊNCIAS", "COMPETENCIAS"};

        for (String linha : linhas) {
            String t = linha.trim();
            if (t.isEmpty()) continue;

            t = t.replaceAll("\\*\\*(.+?)\\*\\*", "$1").replaceAll("\\*(.+?)\\*", "$1").replaceAll("^#{1,3}\\s*", "").trim();
            if (t.isEmpty()) continue;

            if (!nomeExtraido) {
                estrutura.nome = t;
                nomeExtraido = true;
                continue;
            }

            boolean ehTitulo = false;
            String upper = t.toUpperCase();
            boolean todoMaiusculo = t.equals(t.toUpperCase()) && t.length() >= 4 && t.matches("[A-ZÁÉÍÓÚÃÕÂÊÔÇÀ\\s\\-:]+");
            boolean contemPalavraChave = false;
            for (String palavra : PALAVRAS_SECAO) {
                if (upper.contains(palavra)) { contemPalavraChave = true; break; }
            }
            if (todoMaiusculo || (contemPalavraChave && t.length() < 60)) ehTitulo = true;

            if (secaoAtual == null && !ehTitulo) {
                if (estrutura.dadosPessoais.isEmpty()) estrutura.dadosPessoais = t;
                else estrutura.dadosPessoais += "  |  " + t;
                continue;
            }

            if (ehTitulo) {
                secaoAtual = new SecaoCurriculo();
                secaoAtual.titulo = t.replaceAll(":$", "").trim().toUpperCase();
                estrutura.secoes.add(secaoAtual);
                continue;
            }

            if (secaoAtual != null) {
                LinhaConteudo lc = new LinhaConteudo();
                if (t.startsWith("-") || t.startsWith("•") || t.startsWith("*")) {
                    lc.tipo = TipoLinha.BULLET;
                    lc.texto = t.replaceAll("^[-•*]\\s*", "").trim();
                } else if (t.toLowerCase().startsWith("empresa:") || t.toLowerCase().startsWith("company:")) {
                    lc.tipo = TipoLinha.EMPRESA;
                    lc.texto = t;
                } else if ((t.contains("–") || t.contains("-") || t.contains("—")) && (t.matches(".*\\d{4}.*") || t.toLowerCase().contains("atual") || t.toLowerCase().contains("present"))) {
                    lc.tipo = TipoLinha.SUBTITULO;
                    lc.texto = t;
                } else {
                    lc.tipo = TipoLinha.NORMAL;
                    lc.texto = t;
                }
                secaoAtual.linhas.add(lc);
            }
        }
        if (estrutura.nome == null || estrutura.nome.isEmpty()) estrutura.nome = "Currículo";
        return estrutura;
    }

    private void adicionarSeparador(Document doc, com.itextpdf.kernel.colors.DeviceRgb cor, float espessura) throws Exception {
        SolidLine sl = new SolidLine(espessura);
        sl.setColor(cor);
        doc.add(new LineSeparator(sl).setMarginBottom(6));
    }

    private void mostrarSucessoComAbertura(String nomeArquivo) {
        new AlertDialog.Builder(this)
                .setTitle("✅ Currículo Atualizado e PDF Gerado!")
                .setMessage("O arquivo \"" + nomeArquivo + "\" foi salvo em Downloads.")
                .setPositiveButton("Abrir PDF e Sair", (dialog, which) -> {
                    abrirPdf();
                    finish(); // Fecha a tela de edição após abrir
                })
                .setNegativeButton("Apenas Sair", (dialog, which) -> finish())
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
            Toast.makeText(this, "Nenhum leitor de PDF instalado.", Toast.LENGTH_LONG).show();
        }
    }

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
    private enum TipoLinha { NORMAL, BULLET, SUBTITULO, EMPRESA }
}