package com.example.majorproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private Context context;
    private List<UserRequest> requestList;
    private OnRequestClickListener listener;

    public RequestAdapter(Context context, List<UserRequest> requestList, OnRequestClickListener listener) {
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserRequest request = requestList.get(position);
        holder.tvServiceName.setText("Service: " + (request.getServiceName() != null ? request.getServiceName() : "N/A"));
        holder.tvDescription.setText("Description: " + (request.getDescription() != null ? request.getDescription() : "N/A"));
        holder.tvWorkerName.setText("Worker: " + (request.getWorkerName() != null ? request.getWorkerName() : "N/A"));
        holder.tvProposedPrice.setText("Budget: ₹" + (request.getProposedBudget() != null ? request.getProposedBudget() : "N/A"));
        holder.tvWorkerComments.setText("Comments: " + (request.getWorkerComments() != null ? request.getWorkerComments() : "N/A"));
        holder.tvStatus.setText("Status: " + (request.getStatus() != null ? request.getStatus() : "N/A"));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRequestClick(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvDescription, tvWorkerName, tvProposedPrice, tvWorkerComments, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvProposedPrice = itemView.findViewById(R.id.tvProposedBudget);
            tvWorkerComments = itemView.findViewById(R.id.tvWorkerComments);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
    public interface OnRequestClickListener {
        void onRequestClick(UserRequest request);
    }
}