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
import com.medilink.app.models.User;
import com.medilink.app.utils.MockData;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserRole.setText(user.getRole().toUpperCase());
        holder.tvUserInitials.setText(MockData.getInitials(user.getName()));

        // Color coding roles
        if ("admin".equals(user.getRole())) {
            holder.tvUserRole.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E74C3C")));
            holder.tvUserRole.setTextColor(Color.WHITE);
        } else if ("doctor".equals(user.getRole())) {
            holder.tvUserRole.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2ECC71")));
            holder.tvUserRole.setTextColor(Color.WHITE);
        } else {
            holder.tvUserRole.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3498DB")));
            holder.tvUserRole.setTextColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    public void updateData(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserInitials, tvUserName, tvUserEmail, tvUserRole;

        ViewHolder(View itemView) {
            super(itemView);
            tvUserInitials = itemView.findViewById(R.id.tv_user_initials);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserRole = itemView.findViewById(R.id.tv_user_role);
        }
    }
}
