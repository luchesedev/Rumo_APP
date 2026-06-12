package com.example.rumo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rumo.dao.CurriculoDAO;
import com.example.rumo.model.Curriculo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginCadastro extends Tela_Base {

    private TextInputLayout layoutEmail, layoutSenha;
    private TextInputEditText editEmail, editSenha;
    private Button btnLogin;
    private TextView txtEsqueciSenha;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        try {
            com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {}

        iniciarComponentes();
        configurarLimpadoresDeErro();

        btnLogin.setOnClickListener(vw -> {
            if (validarCampos()) {
                fazerLogin(
                        editEmail.getText().toString().trim(),
                        editSenha.getText().toString().trim()
                );
            }
        });

        txtEsqueciSenha.setOnClickListener(vw -> recuperarSenha());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            irParaMain();
        }
    }

    private boolean validarCampos() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();
        boolean valido = true;

        if (email.isEmpty()) {
            layoutEmail.setError("Informe seu e-mail");
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Formato de e-mail inválido");
            valido = false;
        }

        if (senha.isEmpty()) {
            layoutSenha.setError("Informe sua senha");
            valido = false;
        }

        return valido;
    }

    private void fazerLogin(String email, String senha) {
        btnLogin.setEnabled(false);

        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        irParaMain();
                    } else {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            layoutEmail.setError("Conta não encontrada");
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            layoutSenha.setError("E-mail ou senha incorretos");
                        } catch (FirebaseNetworkException e) {
                            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Erro ao fazer login. Tente novamente.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void recuperarSenha() {
        String email = editEmail.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Digite um e-mail válido para recuperar a senha");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "E-mail de recuperação enviado!", Toast.LENGTH_LONG).show();
                    } else {
                        layoutEmail.setError("Conta não encontrada");
                    }
                });
    }

    private void configurarLimpadoresDeErro() {
        TextWatcher limpador = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutEmail.setError(null);
                layoutSenha.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        editEmail.addTextChangedListener(limpador);
        editSenha.addTextChangedListener(limpador);
    }

    private void iniciarComponentes() {
        layoutEmail     = findViewById(R.id.layoutEmail);
        layoutSenha     = findViewById(R.id.layoutSenha);
        editEmail       = findViewById(R.id.editEmail);
        editSenha       = findViewById(R.id.editSenha);
        btnLogin        = findViewById(R.id.btnLogin);
        txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha);
    }

    private void irParaMain() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        CurriculoDAO dao = new CurriculoDAO(this);
        Curriculo curriculo = dao.buscarPorEmail(email);

        Intent intent;
        if (curriculo == null) {
            intent = new Intent(this, AreaUsuario.class);
        } else {
            intent = new Intent(this, Rumo.class);
        }
        startActivity(intent);
        finish();
    }

    public void telaCadastro(View view) {
        startActivity(new Intent(this, Cadastro.class));
    }
}