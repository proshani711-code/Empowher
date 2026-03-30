package com.example.majorproject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class NegotiationAdapter extends RecyclerView.Adapter<NegotiationAdapter.ViewHolder> {

    private Context context;
    private List<Negotiation> negotiationList;
    private String workerId;
    private OnNegotiationClickListener listener;

    public interface OnNegotiationClickListener {
        void onNegotiationClick(Negotiation negotiation);
    }

    public NegotiationAdapter(Context context, List<Negotiation> negotiationList, String workerId, OnNegotiationClickListener listener) {
        this.context = context;
        this.negotiationList = negotiationList;
        this.workerId = workerId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_negotiation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Negotiation negotiation = negotiationList.get(position);

        if (holder.tvProblem != null) {
            holder.tvProblem.setText("Problem: " + negotiation.getProblemDescription());
        } else {
            Log.e("NegotiationAdapter", "tvProblem is NULL!");
        }

        if (holder.tvAddress != null) {
            holder.tvAddress.setText("Address: " + negotiation.getAddress());
        } else {
            Log.e("NegotiationAdapter", "tvAddress is NULL!");
        }

        if (holder.tvUserName != null) {
            FirebaseFirestore.getInstance().collection("Users")
                    .document(negotiation.getUserId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            if (userName != null) {
                                holder.tvUserName.setText("User: " + userName);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("NegotiationAdapter", "Failed to load user name", e));
        } else {
            Log.e("NegotiationAdapter", "tvUserName is NULL!");
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onNegotiationClick(negotiation);
            Intent intent = new Intent(context, WorkerBudgetDecisionActivity.class);
            intent.putExtra("negotiationId", negotiation.getNegotiationId());
            intent.putExtra("userId", negotiation.getUserId());
            intent.putExtra("workerId", workerId);
            intent.putExtra("problemDescription", negotiation.getProblemDescription());
            intent.putExtra("address", negotiation.getAddress());
            intent.putExtra("includesMaterials", negotiation.isIncludesMaterials());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return negotiationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProblem, tvAddress, tvUserName;
        ImageView ivUserImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvProblem = itemView.findViewById(R.id.tvProblem);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            if (tvProblem == null) Log.e("ViewHolder", "tvProblem is NULL!");
            if (tvAddress == null) Log.e("ViewHolder", "tvAddress is NULL!");
            if (tvUserName == null) Log.e("ViewHolder", "tvUserName is NULL!");
        }
    }
}
