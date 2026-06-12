package com.example.rumo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Cadastro extends Tela_Base {

    private TextInputLayout layoutNome, layoutEmail, layoutSenha, layoutSenha2;
    private TextInputEditText editNome, editEmail, editSenha, editSenha2;
    private Button btnCadastro;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iniciarComponentes();
        configurarLimpadoresDeErro();

        btnCadastro.setOnClickListener(vw -> {
            if (validarCampos()) {
                cadastrarUser(
                        editNome.getText().toString().trim(),
                        editEmail.getText().toString().trim(),
                        editSenha.getText().toString().trim()
                );
            }
        });
    }

    private boolean validarCampos() {
        String nome   = editNome.getText().toString().trim();
        String email  = editEmail.getText().toString().trim();
        String senha  = editSenha.getText().toString().trim();
        String senha2 = editSenha2.getText().toString().trim();
        boolean valido = true;

        if (nome.isEmpty() || nome.length() < 3) {
            layoutNome.setError("Informe um nome válido (mínimo 3 caracteres)");
            valido = false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Formato de e-mail inválido");
            valido = false;
        }
        if (senha.isEmpty() || senha.length() < 8) {
            layoutSenha.setError("A senha deve ter no mínimo 8 caracteres");
            valido = false;
        }
        if (senha2.isEmpty() || !senha.equals(senha2)) {
            layoutSenha2.setError("As senhas não coincidem");
            valido = false;
        }

        return valido;
    }

    private void cadastrarUser(String nome, String email, String senha) {
        setCarregando(true);

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest perfilUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nome)
                                    .build();
                            user.updateProfile(perfilUpdate)
                                    .addOnCompleteListener(profileTask -> {
                                        setCarregando(false);
                                        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Cadastro.this, AreaUsuario.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        setCarregando(false);
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            layoutSenha.setError("Sua senha é muito fraca");
                        } catch (FirebaseAuthUserCollisionException e) {
                            layoutEmail.setError("Este e-mail já está cadastrado");
                        } catch (FirebaseNetworkException e) {
                            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Erro ao criar conta. Tente novamente.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void configurarLimpadoresDeErro() {
        TextWatcher limpador = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutNome.setError(null);
                layoutEmail.setError(null);
                layoutSenha.setError(null);
                layoutSenha2.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        editNome.addTextChangedListener(limpador);
        editEmail.addTextChangedListener(limpador);
        editSenha.addTextChangedListener(limpador);
        editSenha2.addTextChangedListener(limpador);
    }

    private void iniciarComponentes() {
        layoutNome   = findViewById(R.id.layoutNome);
        layoutEmail  = findViewById(R.id.layoutEmail);
        layoutSenha  = findViewById(R.id.layoutSenha);
        layoutSenha2 = findViewById(R.id.layoutSenha2);
        editNome     = findViewById(R.id.edtNomeCadastro);
        editEmail    = findViewById(R.id.edtEmailCadastro);
        editSenha    = findViewById(R.id.edtSenhaCadastro);
        editSenha2   = findViewById(R.id.edtSenha2Cadastro);
        btnCadastro  = findViewById(R.id.btn_cadastro);
        progressBar  = findViewById(R.id.progressBar);
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnCadastro.setEnabled(!carregando);
    }

    public void telaLogin(View view) {
        startActivity(new Intent(this, LoginCadastro.class));
        finish();
    }
}