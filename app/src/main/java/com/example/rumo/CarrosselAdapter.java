package com.example.rumo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rumo.model.Vaga;
import com.google.android.material.chip.Chip;

import java.util.List;

public class CarrosselAdapter extends RecyclerView.Adapter<CarrosselAdapter.ViewHolder> {

    private static final int[] CORES = {
            Color.parseColor("#1A1A2E"),
            Color.parseColor("#16213E"),
            Color.parseColor("#0F3460"),
            Color.parseColor("#1B262C"),
            Color.parseColor("#162447"),
    };

    private final List<Vaga> vagas;
    private final Context context;

    public CarrosselAdapter(Context context, List<Vaga> vagas) {
        this.context = context;
        this.vagas = vagas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_carousel_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vaga vaga = vagas.get(position);

        holder.cardRaiz.setCardBackgroundColor(CORES[position % CORES.length]);

        holder.chipTag.setText(formatarTipo(vaga.getTipo()));

        holder.tvTitulo.setText(vaga.getTitulo() != null ? vaga.getTitulo() : "Vaga");
        String subtitulo = (vaga.getEmpresa() != null ? vaga.getEmpresa() : "Empresa") +
                " · " + vaga.getLocalExibicao();
        holder.tvSubtitulo.setText(subtitulo);

        holder.itemView.setOnClickListener(v -> {
            String link = vaga.getLinkCandidatura();
            if (link != null && !link.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(intent);
            }
        });
    }

    private String formatarTipo(String tipo) {
        if (tipo == null) return "Vaga";
        switch (tipo) {
            case "FULLTIME":   return "Tempo integral";
            case "PARTTIME":   return "Meio período";
            case "CONTRACTOR": return "Freelancer";
            case "INTERN":     return "Estágio";
            default:           return tipo;
        }
    }

    @Override
    public int getItemCount() { return vagas.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardRaiz;
        Chip chipTag;
        TextView tvTitulo;
        TextView tvSubtitulo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRaiz    = (com.google.android.material.card.MaterialCardView) itemView;
            chipTag     = itemView.findViewById(R.id.chipCardTag);
            tvTitulo    = itemView.findViewById(R.id.tvCardTitle);
            tvSubtitulo = itemView.findViewById(R.id.tvCardSubtitle);
        }
    }
}