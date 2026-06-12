package com.example.rumo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rumo.model.Curriculo;

import java.util.List;

public class CurriculoAdapter extends RecyclerView.Adapter<CurriculoAdapter.ViewHolder> {

    private final Context context;
    private final List<Curriculo> lista;

    public CurriculoAdapter(Context context, List<Curriculo> lista) {
        this.context = context;
        this.lista   = lista;
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
        Curriculo curriculo = lista.get(position);

        // Título: cargo/objetivo salvo, ou fallback
        String titulo = (curriculo.getObjetivo() != null && !curriculo.getObjetivo().isEmpty())
                ? curriculo.getObjetivo()
                : "Currículo sem título";

        // Subtítulo: trecho dos dados pessoais
        String sub = curriculo.getDadosPessoais() != null
                ? curriculo.getDadosPessoais() : "";
        if (sub.length() > 60) sub = sub.substring(0, 60) + "…";

        holder.tvNome.setText(titulo);
        holder.tvSubtitulo.setText(sub.isEmpty() ? "Sem dados pessoais" : sub);

        // Status: se tem resumo preenchido, considera completo
        boolean completo = curriculo.getResumo() != null && !curriculo.getResumo().isEmpty();
        holder.tvStatus.setText(completo ? "Completo" : "Incompleto");
        holder.tvStatus.setTextColor(
                context.getResources().getColor(
                        completo ? android.R.color.holo_green_dark
                                : android.R.color.holo_orange_dark,
                        null
                )
        );

        // Clique no card inteiro → abre ManutencaoCurriculo
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ManutencaoCurriculo.class);
            intent.putExtra("curriculo_id", curriculo.getId());
            context.startActivity(intent);
        });

        // Clique nos três pontos → menu de opções rápidas
        holder.ivMenu.setOnClickListener(v -> {
            android.widget.PopupMenu popup =
                    new android.widget.PopupMenu(context, holder.ivMenu);
            popup.getMenu().add(0, 1, 0, "Editar");
            popup.getMenu().add(0, 2, 1, "Excluir");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    // Editar — mesma ação do clique no card
                    Intent intent = new Intent(context, ManutencaoCurriculo.class);
                    intent.putExtra("curriculo_id", curriculo.getId());
                    context.startActivity(intent);
                    return true;
                }
                if (item.getItemId() == 2) {
                    // Excluir com confirmação
                    new androidx.appcompat.app.AlertDialog.Builder(context)
                            .setTitle("Excluir currículo")
                            .setMessage("Deseja excluir \"" + titulo + "\"?")
                            .setPositiveButton("Excluir", (dialog, which) -> {
                                com.example.rumo.dao.CurriculoDAO dao =
                                        new com.example.rumo.dao.CurriculoDAO(context);
                                dao.delete(curriculo);
                                lista.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, lista.size());
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // IDs que existem de fato no seu item_curriculo_card.xml
        TextView  tvNome, tvSubtitulo, tvStatus;
        ImageView ivMenu;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome      = itemView.findViewById(R.id.tvCurriculoNome);      // ← id correto
            tvSubtitulo = itemView.findViewById(R.id.tvCurriculoSubtitulo); // ← id correto
            tvStatus    = itemView.findViewById(R.id.tvCurriculoStatus);    // ← id correto
            ivMenu      = itemView.findViewById(R.id.ivCurriculoMenu);      // ← id correto
        }
    }
}