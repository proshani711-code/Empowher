package com.example.majorproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentApprovalAdapter extends RecyclerView.Adapter<PaymentApprovalAdapter.ViewHolder> {

    private Context context;
    private List<Order> paymentList;
    private OnPaymentApprovalListener approvalListener;

    public interface OnPaymentApprovalListener {
        void onApprovePayment(String orderId);
    }

    public PaymentApprovalAdapter(Context context, List<Order> paymentList, OnPaymentApprovalListener approvalListener) {
        this.context = context;
        this.paymentList = paymentList;
        this.approvalListener = approvalListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = paymentList.get(position);
        holder.tvServiceName.setText("Service: " + order.getServiceName());
        holder.tvCustomerName.setText("Customer: " + order.getCustomerName());
        holder.tvPrice.setText("Amount: ₹" + order.getPrice());
        holder.tvPaymentMethod.setText("Method: " + order.getPaymentMethod());

        holder.btnApprove.setOnClickListener(v -> approvalListener.onApprovePayment(order.getOrderId()));
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvCustomerName, tvPrice, tvPaymentMethod;
        Button btnApprove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
