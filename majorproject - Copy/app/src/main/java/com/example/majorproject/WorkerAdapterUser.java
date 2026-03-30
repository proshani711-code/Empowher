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

public class WorkerAdapterUser extends RecyclerView.Adapter<WorkerAdapterUser.WorkerViewHolder> {
    private Context context;
    private List<Worker> workerList;

    public WorkerAdapterUser(Context context, List<Worker> workerList) {
        this.context = context;
        this.workerList = workerList;
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

        holder.tvName.setText(worker.getName());
        holder.tvCategory.setText(worker.getCategory());
        holder.tvCity.setText(worker.getCity());

        Glide.with(context).load(worker.getProfileUrl()).placeholder(R.drawable.ic_placeholder).into(holder.ivProfile);

        if (worker.getAvailability().equalsIgnoreCase("Available")) {
            holder.tvAvailability.setText("Available");
            holder.tvAvailability.setTextColor(Color.GREEN);
        } else {
            holder.tvAvailability.setText("Busy");
            holder.tvAvailability.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return workerList.size();
    }

    public static class WorkerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvCategory, tvCity, tvAvailability;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivWorkerProfile);
            tvName = itemView.findViewById(R.id.tvWorkerName);
            tvCategory = itemView.findViewById(R.id.tvWorkerCategory);
            tvCity = itemView.findViewById(R.id.tvWorkerCity);
            tvAvailability = itemView.findViewById(R.id.tvWorkerAvailability);
        }
    }
}
