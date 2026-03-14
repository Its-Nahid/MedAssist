package com.example.medassist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Added Glide import
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {
    private Context context;
    private List<Doctor> doctorList;

    public DoctorAdapter(Context context, List<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doctor_item, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);
        holder.name.setText(doctor.getName());
        holder.specialty.setText(doctor.getSpecialty());
        holder.phone.setText(doctor.getPhone());
        holder.rating.setText("⭐ " + doctor.getRating() + " | " + doctor.getReviews() + " Reviews");

        // Load image using Glide
        // Assuming your Doctor class has a getImageUrl() method that returns the image URL string
        if (doctor.getImageUrl() != null && !doctor.getImageUrl().isEmpty()) {
            Glide.with(context) // Use the context field from the adapter
                    .load(doctor.getImageUrl())
                    .placeholder(android.R.drawable.stat_sys_download_done) // Example placeholder
                    .error(android.R.drawable.ic_menu_report_image) // Example error image
                    .into(holder.image); // 'image' is the ImageView in DoctorViewHolder
        } else {
            // Optionally, set a default image if no URL is available
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery); // Example default image
        }
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialty, phone, rating;
        ImageView image; // This is the ImageView for the doctor's picture

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.doctorName);
            specialty = itemView.findViewById(R.id.doctorSpecialty);
            phone = itemView.findViewById(R.id.doctorPhone);
            rating = itemView.findViewById(R.id.doctorRating);
            image = itemView.findViewById(R.id.doctorImage); // Ensure this ID matches your doctor_item.xml
        }
    }
}
