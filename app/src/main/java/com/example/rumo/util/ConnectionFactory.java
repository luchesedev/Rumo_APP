package com.example.rumo.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class ConnectionFactory extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3; // ← incrementado para 3
    private static final String DATABASE_NAME = "dbCurriculo.db";

    public ConnectionFactory(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE tbcurriculo (" +
                "id              INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email           TEXT, " +
                "dadosPessoais   TEXT, " +
                "objetivo        TEXT, " +
                "experiencia     TEXT, " +
                "habilidade      TEXT, " +
                "formacao        TEXT, " +
                "resumo          TEXT, " +
                "curriculoGerado TEXT)"; // ← coluna nova
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // migration v1 → v2: adiciona email
            db.execSQL("ALTER TABLE tbcurriculo ADD COLUMN email TEXT");
        }
        if (oldVersion < 3) {
            // migration v2 → v3: adiciona curriculoGerado
            db.execSQL("ALTER TABLE tbcurriculo ADD COLUMN curriculoGerado TEXT");
        }
    }
}