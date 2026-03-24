package com.medilink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.medilink.app.R;
import com.medilink.app.models.Medication;
import com.medilink.app.utils.SessionManager;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {

    private List<Medication> medications;
    private SessionManager sessionManager;

    public MedicationAdapter(List<Medication> medications, SessionManager sessionManager) {
        this.medications = medications;
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication med = medications.get(position);

        holder.tvMedName.setText(med.getName());
        holder.tvMedInfo.setText(med.getDosage() + " • " + med.getFrequency());
        holder.tvMedTime.setText(med.getTime());

        // Remove listener temporarily to avoid trigger during scrolling
        holder.cbTaken.setOnCheckedChangeListener(null);
        holder.cbTaken.setChecked(med.isTaken());

        holder.cbTaken.setOnCheckedChangeListener((buttonView, isChecked) -> {
            med.setTaken(isChecked);
            // Save state to preferences
            sessionManager.updateMedications(medications);
        });
    }

    @Override
    public int getItemCount() {
        return medications != null ? medications.size() : 0;
    }

    public void updateData(List<Medication> newMedications) {
        this.medications = newMedications;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvMedInfo, tvMedTime;
        CheckBox cbTaken;

        ViewHolder(View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tv_med_name);
            tvMedInfo = itemView.findViewById(R.id.tv_med_info);
            tvMedTime = itemView.findViewById(R.id.tv_med_time);
            cbTaken = itemView.findViewById(R.id.cb_taken);
        }
    }
}
