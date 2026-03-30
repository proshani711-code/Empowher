package com.example.majorproject;

import java.util.HashMap;
import java.util.Map;

public class Negotiation {
    public enum Status {
        Pending,
        Worker_Proposed_Price,
        Accepted,
        Rejected,
        Completed
    }

    private String negotiationId;
    private String workerId;
    private String userId;
    private String problemDescription;
    private String address; // 🏠 User's Address
    private boolean includesMaterials;
    private Status status;
    private String proposedBudget;
    private String imageUrl;
    private String workerComments;
    private Double userLat;
    private Double userLng;

    public Negotiation() {}

    public Negotiation(String negotiationId, String workerId, String userId, String problemDescription, String address,
                       boolean includesMaterials, Status status, String proposedBudget, String imageUrl,
                       String workerComments, Double userLat, Double userLng) {
        this.negotiationId = negotiationId;
        this.workerId = workerId;
        this.userId = userId;
        this.problemDescription = problemDescription;
        this.address = address;
        this.includesMaterials = includesMaterials;
        this.status = status;
        this.proposedBudget = proposedBudget;
        this.imageUrl = imageUrl;
        this.workerComments = workerComments;
        this.userLat = userLat;
        this.userLng = userLng;
    }

    public String getNegotiationId() { return negotiationId; }
    public void setNegotiationId(String negotiationId) { this.negotiationId = negotiationId; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isIncludesMaterials() { return includesMaterials; }
    public void setIncludesMaterials(boolean includesMaterials) { this.includesMaterials = includesMaterials; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getProposedBudget() { return proposedBudget; }
    public void setProposedBudget(String proposedBudget) { this.proposedBudget = proposedBudget; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getWorkerComments() { return workerComments; }
    public void setWorkerComments(String workerComments) { this.workerComments = workerComments; } // Set worker comments

    public Double getUserLat() { return userLat; }
    public void setUserLat(Double userLat) { this.userLat = userLat; }

    public Double getUserLng() { return userLng; }
    public void setUserLng(Double userLng) { this.userLng = userLng; }

    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("negotiationId", negotiationId);
        data.put("workerId", workerId);
        data.put("userId", userId);
        data.put("problemDescription", problemDescription);
        data.put("address", address);
        data.put("includesMaterials", includesMaterials);
        data.put("status", status.name());
        data.put("proposedBudget", proposedBudget);
        data.put("imageUrl", imageUrl);
        data.put("workerComments", workerComments);
        data.put("userLat", userLat);
        data.put("userLng", userLng);
        return data;
    }
}