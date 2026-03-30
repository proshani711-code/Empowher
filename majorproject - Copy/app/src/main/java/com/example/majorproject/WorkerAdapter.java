package com.example.majorproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {
    private Context context;
    private List<Worker> workerList;
    private OnWorkerClickListener listener;

    public WorkerAdapter(Context context, List<Worker> workerList, OnWorkerClickListener listener) {
        this.context = context;
        this.workerList = workerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker_user, parent, false);
        return new WorkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        Worker worker = workerList.get(position);

        String name = worker.getName() != null ? worker.getName() : "Unknown";
        String category = worker.getCategory() != null ? worker.getCategory() : "N/A";
        String city = worker.getCity() != null ? worker.getCity() : "Not Specified";
        String experience = worker.getExperience() != null ? worker.getExperience() + " years" : "No Experience";
        String rating = worker.getRating() != null ? worker.getRating() + " ⭐" : "No Rating";
        String profileUrl = worker.getProfileUrl() != null ? worker.getProfileUrl() : "";
        String availability = worker.getAvailability() != null ? worker.getAvailability() : "Unknown";

        holder.tvName.setText(name);
        holder.tvCategory.setText(category);
        holder.tvCity.setText(city);
        holder.tvExperience.setText("Experience: " + experience);
        holder.tvRating.setText("Rating: " + rating);

        Glide.with(context)
                .load(profileUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.ivProfile);

        if ("available".equalsIgnoreCase(availability)) {
            holder.tvAvailability.setText("Available");
            holder.tvAvailability.setTextColor(Color.GREEN);
        } else if ("busy".equalsIgnoreCase(availability)) {
            holder.tvAvailability.setText("Busy");
            holder.tvAvailability.setTextColor(Color.RED);
        } else {
            holder.tvAvailability.setText("Status Unknown");
            holder.tvAvailability.setTextColor(Color.GRAY);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkerClick(worker);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workerList != null ? workerList.size() : 0;
    }

    public static class WorkerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvCategory, tvCity, tvExperience, tvRating, tvAvailability;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivWorkerProfile);
            tvName = itemView.findViewById(R.id.tvWorkerName);
            tvCategory = itemView.findViewById(R.id.tvWorkerCategory);
            tvCity = itemView.findViewById(R.id.tvWorkerCity);
            tvExperience = itemView.findViewById(R.id.tvWorkerExperience);
            tvRating = itemView.findViewById(R.id.tvWorkerRating);
            tvAvailability = itemView.findViewById(R.id.tvWorkerAvailability);
        }
    }

    public interface OnWorkerClickListener {
        void onWorkerClick(Worker worker);
    }
}
