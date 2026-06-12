package com.example.rumo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.rumo.model.Curriculo;
import com.example.rumo.util.ConnectionFactory;

import java.util.ArrayList;
import java.util.List;

public class CurriculoDAO {
    private ConnectionFactory conexao;
    private SQLiteDatabase banco;

    public CurriculoDAO(Context context) {
        try {
            conexao = new ConnectionFactory(context);
            banco = conexao.getWritableDatabase();
        } catch (Exception e) {
            Log.e("ERRO_BANCO", "Erro ao abrir banco: " + e.getMessage());
        }
    }

    // ── Insert ──────────────────────────────────────────────────────────────
    public long Insert(Curriculo curriculo) {
        return banco.insert("tbcurriculo", null, montarValues(curriculo));
    }

    // ── Update ──────────────────────────────────────────────────────────────
    public void update(Curriculo curriculo) {
        String[] args = {String.valueOf(curriculo.getId())};
        banco.update("tbcurriculo", montarValues(curriculo), "id=?", args);
    }

    // ── Salva ou atualiza pelo e-mail ────────────────────────────────────────
    // Se já existe um currículo com aquele e-mail, sobrescreve.
    // Se não existe, insere um novo.
    // Retorna o Curriculo com o id correto preenchido.
    public Curriculo salvarOuAtualizar(Curriculo curriculo) {
        Curriculo existente = buscarPorEmail(curriculo.getEmail());
        if (existente != null) {
            curriculo.setId(existente.getId());
            update(curriculo);
        } else {
            long novoId = Insert(curriculo);
            curriculo.setId((int) novoId);
        }
        return curriculo;
    }

    // ── Busca por e-mail ─────────────────────────────────────────────────────
    public Curriculo buscarPorEmail(String email) {
        if (email == null) return null;

        Cursor cursor = banco.query("tbcurriculo",
                new String[]{"id", "email", "dadosPessoais", "objetivo",
                        "experiencia", "habilidade", "formacao", "resumo", "curriculoGerado"},
                "email = ?", new String[]{email},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Curriculo c = mapearCursor(cursor);
            cursor.close();
            return c;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    // ── Busca por id ─────────────────────────────────────────────────────────
    public Curriculo buscarPorId(int id) {
        Cursor cursor = banco.query("tbcurriculo",
                new String[]{"id", "email", "dadosPessoais", "objetivo",
                        "experiencia", "habilidade", "formacao", "resumo", "curriculoGerado"},
                "id = ?", new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Curriculo c = mapearCursor(cursor);
            cursor.close();
            return c;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    // ── Lista todos ──────────────────────────────────────────────────────────
    public List<Curriculo> obterTodos() {
        List<Curriculo> curriculos = new ArrayList<>();

        Cursor cursor = banco.query("tbcurriculo",
                new String[]{"id", "email", "dadosPessoais", "objetivo",
                        "experiencia", "habilidade", "formacao", "resumo", "curriculoGerado"},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            curriculos.add(mapearCursor(cursor));
        }
        cursor.close();
        return curriculos;
    }

    // ── Delete ───────────────────────────────────────────────────────────────
    public void delete(Curriculo curriculo) {
        String[] args = {String.valueOf(curriculo.getId())};
        banco.delete("tbcurriculo", "id=?", args);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private ContentValues montarValues(Curriculo c) {
        ContentValues values = new ContentValues();
        values.put("email",           c.getEmail());
        values.put("dadosPessoais",   c.getDadosPessoais());
        values.put("objetivo",        c.getObjetivo());
        values.put("experiencia",     c.getExperiencia());
        values.put("habilidade",      c.getHabilidade());
        values.put("formacao",        c.getFormacao());
        values.put("resumo",          c.getResumo());
        values.put("curriculoGerado", c.getCurriculoGerado());
        return values;
    }

    private Curriculo mapearCursor(Cursor cursor) {
        Curriculo c = new Curriculo();
        c.setId(            cursor.getInt(   cursor.getColumnIndexOrThrow("id")));
        c.setEmail(         cursor.getString(cursor.getColumnIndexOrThrow("email")));
        c.setDadosPessoais( cursor.getString(cursor.getColumnIndexOrThrow("dadosPessoais")));
        c.setObjetivo(      cursor.getString(cursor.getColumnIndexOrThrow("objetivo")));
        c.setExperiencia(   cursor.getString(cursor.getColumnIndexOrThrow("experiencia")));
        c.setHabilidade(    cursor.getString(cursor.getColumnIndexOrThrow("habilidade")));
        c.setFormacao(      cursor.getString(cursor.getColumnIndexOrThrow("formacao")));
        c.setResumo(        cursor.getString(cursor.getColumnIndexOrThrow("resumo")));
        c.setCurriculoGerado(cursor.getString(cursor.getColumnIndexOrThrow("curriculoGerado")));
        return c;
    }

    // ── Lista apenas os currículos que já foram gerados pela IA ──────────────
    public List<Curriculo> obterCurriculosProntos() {
        List<Curriculo> curriculos = new ArrayList<>();

        // O filtro "curriculoGerado IS NOT NULL AND curriculoGerado != ''"
        // garante que o perfil de manutenção não apareça na lista.
        Cursor cursor = banco.query("tbcurriculo",
                new String[]{"id", "email", "dadosPessoais", "objetivo",
                        "experiencia", "habilidade", "formacao", "resumo", "curriculoGerado"},
                "curriculoGerado IS NOT NULL AND curriculoGerado != ''",
                null, null, null, null);

        while (cursor.moveToNext()) {
            curriculos.add(mapearCursor(cursor));
        }
        cursor.close();
        return curriculos;
    }
}