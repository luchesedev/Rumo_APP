package com.example.rumo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

public class CarrosselAdapter extends RecyclerView.Adapter<CarrosselAdapter.ViewHolder> {

    // Modelo de dados do card do carrossel
    public static class CarrosselItem {
        public String tag;
        public String titulo;
        public String subtitulo;
        public int corFundo; // cor em int (use Color.parseColor ou ContextCompat.getColor)

        public CarrosselItem(String tag, String titulo, String subtitulo, int corFundo) {
            this.tag = tag;
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.corFundo = corFundo;
        }
    }

    private final List<CarrosselItem> itens;
    private final Context context;

    public CarrosselAdapter(Context context, List<CarrosselItem> itens) {
        this.context = context;
        this.itens = itens;
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
        CarrosselItem item = itens.get(position);

        holder.chipTag.setText(item.tag);
        holder.tvTitulo.setText(item.titulo);
        holder.tvSubtitulo.setText(item.subtitulo);
        holder.cardRaiz.setCardBackgroundColor(item.corFundo);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardRaiz;
        Chip chipTag;
        TextView tvTitulo;
        TextView tvSubtitulo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRaiz = (com.google.android.material.card.MaterialCardView) itemView;
            chipTag = itemView.findViewById(R.id.chipCardTag);
            tvTitulo = itemView.findViewById(R.id.tvCardTitle);
            tvSubtitulo = itemView.findViewById(R.id.tvCardSubtitle);
        }
    }
}