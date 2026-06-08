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
            conexao = new ConnectionFactory(context, "dbCurriculo.db", null, 1);
            // É aqui que o banco realmente tenta abrir ou criar as tabelas
            banco = conexao.getWritableDatabase();
        } catch (Exception e) {
            // Se o banco falhar, o Logcat vai te avisar exatamente o porquê
            Log.e("ERRO_BANCO", "Erro ao abrir banco: " + e.getMessage());
        }
    }

    public long Insert(Curriculo curriculo) {
        ContentValues values = new ContentValues();
        values.put("dadosPessoais", curriculo.getDadosPessoais());
        values.put("objetivo", curriculo.getObjetivo());
        values.put("experiencia", curriculo.getExperiencia());
        values.put("habilidade", curriculo.getHabilidade());
        values.put("formacao", curriculo.getFormacao());
        values.put("resumo", curriculo.getResumo());
        return banco.insert("tbcurriculo", null, values);
    }
    public void update(Curriculo curriculo) {
        ContentValues values = new ContentValues();
        values.put("dadosPessoais", curriculo.getDadosPessoais());
        values.put("objetivo", curriculo.getObjetivo());
        values.put("experiencia", curriculo.getExperiencia());
        values.put("habilidade", curriculo.getHabilidade());
        values.put("formacao", curriculo.getFormacao());
        values.put("resumo", curriculo.getResumo());
        String[] args = {String.valueOf(curriculo.getCodigo())};
        banco.update("tbcurriculo", values, "id=?", args);
    }

    public void delete(Curriculo curriculo) {
        String[] args = {String.valueOf(curriculo.getCodigo())};
        banco.delete("tbcurriculo", "id=?", args);
    }
    public List<Curriculo> obterTodos() {
        List<Curriculo> curriculos = new ArrayList<>();
        Cursor cursor = banco.query("tbcurriculo",
                new String[]{"id", "dadosPessoais", "objetivo", "experiencia", "habilidade", "formacao", "resumo"},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            Curriculo c = new Curriculo();
            c.setCodigo(cursor.getInt(0));
            c.setDadosPessoais(cursor.getString(1));
            c.setObjetivo(cursor.getString(2));
            c.setExperiencia(cursor.getString(3));
            c.setHabilidade(cursor.getString(4));
            c.setFormacao(cursor.getString(5));
            c.setResumo(cursor.getString(6));
            curriculos.add(c);
        }
        cursor.close();
        return curriculos;
    }
}
