package com.example.majorproject;

public class UserRequest {

    private String negotiationId;
    private String userId;
    private String workerId;
    private String workerName;
    private String proposedBudget;
    private String workerComments;
    private String status;
    private String workerPhone;
    private String userPhone;
    private String serviceName;
    private String description;
    private String message;
    private long timestamp;
    private boolean showPhoneNumbers;
    private String location;
    private String problemDescription;

    public UserRequest() {
    }

    public String getNegotiationId() { return negotiationId; }
    public String getUserId() { return userId; }
    public String getWorkerId() { return workerId; }
    public String getWorkerName() { return workerName; }
    public String getProposedBudget() { return proposedBudget; }
    public String getWorkerComments() { return workerComments; }
    public String getStatus() { return status; }
    public String getWorkerPhone() { return showPhoneNumbers ? workerPhone : "Hidden until accepted"; }
    public String getUserPhone() { return showPhoneNumbers ? userPhone : "Hidden until accepted"; }
    public String getServiceName() { return serviceName; }
    public String getDescription() { return description; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isShowPhoneNumbers() { return showPhoneNumbers; }
    public String getLocation() { return location; } // Getter for location
    public String getProblemDescription() { return problemDescription; } // Getter for problem description

    public void setNegotiationId(String negotiationId) { this.negotiationId = negotiationId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public void setProposedBudget(String proposedBudget) { this.proposedBudget = proposedBudget; }
    public void setWorkerComments(String workerComments) { this.workerComments = workerComments; }
    public void setStatus(String status) { this.status = status; }
    public void setWorkerPhone(String workerPhone) { this.workerPhone = workerPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setDescription(String description) { this.description = description; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setShowPhoneNumbers(boolean showPhoneNumbers) { this.showPhoneNumbers = showPhoneNumbers; }
    public void setLocation(String location) { this.location = location; } // Setter for location
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; } // Setter for problem description
}