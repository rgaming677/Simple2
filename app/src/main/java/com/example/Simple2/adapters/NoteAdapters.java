package com.example.Simple2.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Simple2.R;
import com.example.Simple2.database.entities.Note;

import java.util.List;

public class NoteAdapters extends RecyclerView.Adapter<NoteAdapters.NodeViewHolder> {

    private List<Note> notes;

    public NoteAdapters(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NodeViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NodeViewHolder holder, int position) {
        holder.setNote(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NodeViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvSubTitle, tvDatetime;
        LinearLayout llNote;
        ImageView ivNote;

        NodeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
            tvDatetime = itemView.findViewById(R.id.tvDateTime);
            llNote = itemView.findViewById(R.id.llNote);
            ivNote = itemView.findViewById(R.id.ivNote);
        }

        void setNote(Note note) {
            tvTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                tvSubTitle.setVisibility(View.GONE);
            } else {
                tvSubTitle.setText(note.getSubtitle());
            }
            tvDatetime.setText(note.getDateTime());

            //Pewarnaan
            GradientDrawable gradientDrawable = (GradientDrawable) llNote.getBackground();
            if (note.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#FFFFFF"));
            }

            //Menampilkan gambar
            if (note.getImagePath() !=null){
                ivNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                ivNote.setVisibility(View.VISIBLE);
            } else {
                ivNote.setVisibility(View.GONE);
            }
        }

    }
}
