package com.example.rumo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CurriculoAdapter extends RecyclerView.Adapter<CurriculoAdapter.ViewHolder> {

    // Modelo de dados do card de currículo
    public static class CurriculoItem {
        public String nome;
        public String dataAtualizacao;
        public String status;        // ex: "Completo", "Incompleto"
        public boolean completo;     // true = badge verde, false = badge amarelo

        public CurriculoItem(String nome, String dataAtualizacao, String status, boolean completo) {
            this.nome = nome;
            this.dataAtualizacao = dataAtualizacao;
            this.status = status;
            this.completo = completo;
        }
    }

    private final List<CurriculoItem> itens;
    private final Context context;

    public CurriculoAdapter(Context context, List<CurriculoItem> itens) {
        this.context = context;
        this.itens = itens;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_curriculo_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CurriculoItem item = itens.get(position);

        holder.tvNome.setText(item.nome);
        holder.tvSubtitulo.setText("Atualizado em " + item.dataAtualizacao);
        holder.tvStatus.setText(item.status);

        // Muda o badge de cor conforme o status
        if (item.completo) {
            holder.tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_green);
        } else {
            holder.tvStatus.setTextColor(0xFFF57F17); // amarelo escuro
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_yellow);
        }

        // Clique no card inteiro
        holder.itemView.setOnClickListener(v ->
                Toast.makeText(context, "Abrir: " + item.nome, Toast.LENGTH_SHORT).show()
        );

        // Clique nos três pontos
        holder.ivMenu.setOnClickListener(v ->
                Toast.makeText(context, "Menu: " + item.nome, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvSubtitulo;
        TextView tvStatus;
        ImageView ivMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvCurriculoNome);
            tvSubtitulo = itemView.findViewById(R.id.tvCurriculoSubtitulo);
            tvStatus = itemView.findViewById(R.id.tvCurriculoStatus);
            ivMenu = itemView.findViewById(R.id.ivCurriculoMenu);
        }
    }
}