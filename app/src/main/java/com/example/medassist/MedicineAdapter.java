package com.example.medassist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private final List<Medicine> medicineList;
    private final OnMedicineClickListener listener;

    public interface OnMedicineClickListener {
        void onMedicineClick(Medicine medicine);
    }

    public MedicineAdapter(List<Medicine> medicineList, OnMedicineClickListener listener) {
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);
        holder.bind(medicine, listener);
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView name, shortUse, price;
        ImageView image;
        ImageButton btnSpeak;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.med_image);
            name = itemView.findViewById(R.id.med_name);
            shortUse = itemView.findViewById(R.id.med_short_use);
            price = itemView.findViewById(R.id.med_price);
            btnSpeak = itemView.findViewById(R.id.btnSpeak); // new icon button
        }

        public void bind(final Medicine medicine, final OnMedicineClickListener listener) {
            name.setText(medicine.getName());
            shortUse.setText(medicine.getShortUse());
            price.setText(medicine.getPrice());

            Glide.with(itemView.getContext())
                    .load(medicine.getImageUrl())
                    .into(image);

            // 1️⃣ Normal card click → open medicine details
            itemView.setOnClickListener(v -> listener.onMedicineClick(medicine));

            // 2️⃣ Speak icon click → show dialog + TTS
            btnSpeak.setOnClickListener(v ->
                    SpeakDialogUtil.showAndSpeak(v.getContext(), medicine, false)  // ❌ no “I found …”
            );

        }
    }
}
