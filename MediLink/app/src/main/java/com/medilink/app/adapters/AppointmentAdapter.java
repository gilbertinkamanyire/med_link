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
import com.medilink.app.models.Appointment;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Appointment> appointments;

    public AppointmentAdapter(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appt = appointments.get(position);

        holder.tvDoctorName.setText(appt.getDoctorName());
        holder.tvSpecialtyClinic.setText(appt.getSpecialty() + " • " + appt.getClinic());
        holder.tvDate.setText(appt.getDate());
        holder.tvTime.setText(appt.getTime());

        try {
            holder.viewAccent.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(appt.getColor())));
        } catch (Exception e) {
            // Default color if parsing fails
            holder.viewAccent.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#008080")));
        }
    }

    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    public void updateData(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewAccent;
        TextView tvDoctorName, tvSpecialtyClinic, tvDate, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            viewAccent = itemView.findViewById(R.id.view_accent);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvSpecialtyClinic = itemView.findViewById(R.id.tv_specialty_clinic);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
