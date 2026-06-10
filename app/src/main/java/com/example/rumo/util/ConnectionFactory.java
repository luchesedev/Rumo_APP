package com.example.rumo.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
public class ConnectionFactory extends SQLiteOpenHelper{
    public ConnectionFactory(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tbcurriculo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "dadosPessoais TEXT, " +
                "objetivo TEXT, " +
                "experiencia TEXT, " +
                "habilidade TEXT, " +
                "formacao TEXT, " +
                "resumo TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tbcurriculo");
        onCreate(db);
    }

}