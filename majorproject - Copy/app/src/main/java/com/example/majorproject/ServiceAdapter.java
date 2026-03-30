package com.example.majorproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceCategory> serviceList;
    private OnServiceEditListener editListener;
    private OnServiceDeleteListener deleteListener;
    public interface OnServiceEditListener {
        void onEditService(String serviceId, String serviceName);
    }
    public interface OnServiceDeleteListener {
        void onDeleteService(String serviceId);
    }
    public ServiceAdapter(List<ServiceCategory> serviceList, OnServiceEditListener editListener, OnServiceDeleteListener deleteListener) {
        this.serviceList = serviceList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }
    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceCategory service = serviceList.get(position);
        holder.tvServiceName.setText(service.getName());

        holder.btnEditService.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditService(service.getId(), service.getName());
            }
        });

        holder.btnDeleteService.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteService(service.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        Button btnEditService, btnDeleteService;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            btnEditService = itemView.findViewById(R.id.btnEditService);
            btnDeleteService = itemView.findViewById(R.id.btnDeleteService);
        }
    }
}
