package com.medilink.app.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.medilink.app.R;
import com.medilink.app.models.Doctor;
import com.medilink.app.utils.MockData;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private List<Doctor> doctors;
    private OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctors, OnDoctorClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);

        holder.tvDoctorInitials.setText(MockData.getInitials(doctor.getName()));
        try {
            holder.tvDoctorInitials.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(doctor.getColor())));
        } catch (Exception e) {}

        holder.tvDoctorName.setText(doctor.getName());
        holder.tvSpecialty.setText(doctor.getSpecialty());
        holder.tvClinic.setText(doctor.getClinic());

        String ratingText = String.format("%.1f (%d reviews)", doctor.getRating(), doctor.getReviews());
        holder.tvRating.setText(ratingText);

        holder.tvSlots.setText(String.format("%d slots", doctor.getTimeSlots().size()));
        holder.tvNextSlot.setText(doctor.getNextAvailable());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDoctorClick(doctor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctors != null ? doctors.size() : 0;
    }

    public void updateList(List<Doctor> newList) {
        this.doctors = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorInitials, tvDoctorName, tvSpecialty, tvClinic, tvRating, tvSlots, tvNextSlot;

        ViewHolder(View itemView) {
            super(itemView);
            tvDoctorInitials = itemView.findViewById(R.id.tv_doctor_initials);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvSpecialty = itemView.findViewById(R.id.tv_specialty);
            tvClinic = itemView.findViewById(R.id.tv_clinic);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvSlots = itemView.findViewById(R.id.tv_slots);
            tvNextSlot = itemView.findViewById(R.id.tv_next_slot);
        }
    }
}
