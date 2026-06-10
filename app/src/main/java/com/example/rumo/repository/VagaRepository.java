package com.example.rumo.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.rumo.api.RetrofitClient;
import com.example.rumo.api.VagaService;
import com.example.rumo.model.Vaga;
import com.example.rumo.model.VagaResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VagaRepository {

    private static final String API_KEY  = "3b61c6bcbamshe766c9a32b85160p17812ejsn396b24bb043f";
    private static final String API_HOST = "jsearch.p.rapidapi.com";

    // Cache válido por 12 horas
    private static final long CACHE_VALIDADE_MS = 12 * 60 * 60 * 1000;

    private static final String PREF_NAME       = "vaga_cache";
    private static final String KEY_VAGAS_JSON  = "vagas_json";
    private static final String KEY_ULTIMO_FETCH = "ultimo_fetch";
    private static final String KEY_ULTIMA_QUERY = "ultima_query";

    private final Context context;
    private final Gson gson = new Gson();

    public VagaRepository(Context context) {
        this.context = context;
    }

    public interface VagaCallback {
        void onSucesso(List<Vaga> vagas);
        void onErro(String mensagem);
    }

    public void buscarVagas(String area, String nivel, VagaCallback callback) {
        String query = area + " " + nivel + " em São Paulo, SP";

        // Tenta usar o cache primeiro
        List<Vaga> vagasCache = getCacheValido(query);
        if (vagasCache != null) {
            Log.d("VAGA_API", "Usando cache: " + vagasCache.size() + " vagas");
            callback.onSucesso(vagasCache);
            return;
        }

        // Cache vencido ou vazio — busca da API
        Log.d("VAGA_API", "Buscando da API: " + query);
        buscarDaApi(query, callback);
    }

    private void buscarDaApi(String query, VagaCallback callback) {
        VagaService service = RetrofitClient.getInstance().getVagaService();

        Call<VagaResponse> call = service.buscarVagas(
                API_KEY,
                API_HOST,
                query,
                1,
                "month",
                "br",
                "São Paulo, Brazil" // Passando a localização explicitly
        );

        call.enqueue(new Callback<VagaResponse>() {
            @Override
            public void onResponse(Call<VagaResponse> call, Response<VagaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Vaga> vagas = response.body().getVagas();
                    Log.d("VAGA_API", "API retornou: " + (vagas != null ? vagas.size() : 0) + " vagas");

                    if (vagas != null && !vagas.isEmpty()) {
                        salvarCache(query, vagas);
                        callback.onSucesso(vagas);
                    } else {
                        List<Vaga> cacheAntigo = getCacheAntigo();
                        if (cacheAntigo != null) {
                            Log.d("VAGA_API", "API vazia, usando cache antigo");
                            callback.onSucesso(cacheAntigo);
                        } else {
                            callback.onErro("Nenhuma vaga encontrada");
                        }
                    }
                } else {
                    Log.e("VAGA_API", "Erro HTTP: " + response.code());
                    List<Vaga> cacheAntigo = getCacheAntigo();
                    if (cacheAntigo != null) {
                        callback.onSucesso(cacheAntigo);
                    } else {
                        callback.onErro("Erro ao buscar vagas: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<VagaResponse> call, Throwable t) {
                Log.e("VAGA_API", "Falha: " + t.getMessage());
                List<Vaga> cacheAntigo = getCacheAntigo();
                if (cacheAntigo != null) {
                    Log.d("VAGA_API", "Sem internet, usando cache antigo");
                    callback.onSucesso(cacheAntigo);
                } else {
                    callback.onErro("Sem conexão com a internet");
                }
            }
        });
    }

    private List<Vaga> getCacheValido(String query) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long ultimoFetch = prefs.getLong(KEY_ULTIMO_FETCH, 0);
        String ultimaQuery = prefs.getString(KEY_ULTIMA_QUERY, "");
        String vagasJson = prefs.getString(KEY_VAGAS_JSON, null);

        long agora = System.currentTimeMillis();
        boolean cacheValido = (agora - ultimoFetch) < CACHE_VALIDADE_MS;
        boolean mesmaQuery = query.equalsIgnoreCase(ultimaQuery);

        if (cacheValido && mesmaQuery && vagasJson != null) {
            Type tipo = new TypeToken<List<Vaga>>() {}.getType();
            return gson.fromJson(vagasJson, tipo);
        }
        return null;
    }

    private List<Vaga> getCacheAntigo() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String vagasJson = prefs.getString(KEY_VAGAS_JSON, null);
        if (vagasJson != null) {
            Type tipo = new TypeToken<List<Vaga>>() {}.getType();
            return gson.fromJson(vagasJson, tipo);
        }
        return null;
    }

    private void salvarCache(String query, List<Vaga> vagas) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String vagasJson = gson.toJson(vagas);
        prefs.edit()
                .putString(KEY_VAGAS_JSON, vagasJson)
                .putLong(KEY_ULTIMO_FETCH, System.currentTimeMillis())
                .putString(KEY_ULTIMA_QUERY, query)
                .apply();
        Log.d("VAGA_API", "Cache salvo: " + vagas.size() + " vagas");
    }

    public void limparCache() {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Log.d("VAGA_API", "Cache limpo");
    }
}