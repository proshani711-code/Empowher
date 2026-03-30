// PaymentSummaryAdapter.java
package com.example.majorproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentSummaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private final Context context;
    private final List<Order> paymentList;
    private boolean isLoadingAdded = false;

    public PaymentSummaryAdapter(Context context, List<Order> paymentList) {
        this.context = context;
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_ITEM) {
            view = LayoutInflater.from(context).inflate(R.layout.item_payment_summary, parent, false);
            return new PaymentViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            Order order = paymentList.get(position);
            PaymentViewHolder paymentHolder = (PaymentViewHolder) holder;

            paymentHolder.bindOrderData(order);
        }
    }

    @Override
    public int getItemCount() {
        return paymentList.size() + (isLoadingAdded ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == paymentList.size() && isLoadingAdded) ? TYPE_LOADING : TYPE_ITEM;
    }

    public void showLoading(boolean isLoading) {
        if (isLoading) {
            addLoadingFooter();
        } else {
            removeLoadingFooter();
        }
    }

    private void addLoadingFooter() {
        isLoadingAdded = true;
        notifyItemInserted(paymentList.size());
    }

    private void removeLoadingFooter() {
        isLoadingAdded = false;
        notifyItemRemoved(paymentList.size());
    }

    public void add(Order order) {
        paymentList.add(order);
        notifyItemInserted(paymentList.size() - 1);
    }

    public void addAll(List<Order> orders) {
        for (Order order : orders) {
            add(order);
        }
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId, tvCustomerName, tvAmount, tvDate, tvStatus;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bindOrderData(Order order) {
            tvOrderId.setText(itemView.getContext().getString(R.string.order_id, order.getOrderId()));
            tvCustomerName.setText(itemView.getContext().getString(R.string.customer_name, order.getCustomerName()));
            tvAmount.setText(itemView.getContext().getString(R.string.amount, order.getPrice()));
            tvStatus.setText(itemView.getContext().getString(R.string.status, order.getStatus()));

            String formattedDate = getFormattedDate(order.getTimestamp());
            tvDate.setText(itemView.getContext().getString(R.string.date, formattedDate));

            int statusColor = getStatusColor(order.getStatus());
            tvStatus.setTextColor(statusColor);
        }

        private String getFormattedDate(long timestamp) {
            if (timestamp > 0) {
                return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(new Date(timestamp));
            } else {
                return itemView.getContext().getString(R.string.date_not_available);
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "Completed":
                    return ContextCompat.getColor(itemView.getContext(), R.color.green);
                case "Pending":
                    return ContextCompat.getColor(itemView.getContext(), R.color.orange);
                case "Cancelled":
                    return ContextCompat.getColor(itemView.getContext(), R.color.red);
                default:
                    return ContextCompat.getColor(itemView.getContext(), R.color.black);
            }
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
