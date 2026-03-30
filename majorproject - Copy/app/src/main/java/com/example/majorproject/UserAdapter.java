package com.example.majorproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserClickListener editListener;
    private OnUserClickListener deleteListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> userList, OnUserClickListener editListener, OnUserClickListener deleteListener) {
        this.userList = userList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserName.setText(user.getName() != null ? user.getName() : "Unknown User");
        holder.tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");


        String role = user.getRole() != null ? user.getRole() : "User";
        holder.tvUserRole.setText(role);

        boolean isAdmin = role.equalsIgnoreCase("admin");
        holder.btnEditUser.setVisibility(isAdmin ? View.GONE : View.VISIBLE);
        holder.btnDeleteUser.setVisibility(isAdmin ? View.GONE : View.VISIBLE);

        holder.btnEditUser.setOnClickListener(v -> editListener.onUserClick(user));
        holder.btnDeleteUser.setOnClickListener(v -> deleteListener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserRole;
        Button btnEditUser, btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
