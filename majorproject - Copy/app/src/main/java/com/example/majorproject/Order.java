package com.example.majorproject;

import java.util.Date;

public class Order {
    private String orderId;
    private String serviceName;
    private String customerName;
    private String userId;
    private String userName;
    private String userPhone;
    private String workerId;
    private String workerName;
    private String workerPhone;
    private String status;
    private String price;
    private String paymentMethod;
    private String paymentStatus;
    private String orderAddress;
    private boolean showPhoneNumbers;
    private long timestamp;
    private double userLat;
    private double userLng;
    private double workerLat;
    private double workerLng;
    public Order() {
        this.orderId = "";
        this.serviceName = "";
        this.customerName = "";
        this.userId = "";
        this.userName = "";
        this.userPhone = "";
        this.workerId = "";
        this.workerName = "";
        this.workerPhone = "";
        this.status = "Pending";
        this.price = "0";
        this.paymentMethod = "COD";
        this.paymentStatus = "Pending";
        this.orderAddress = "Not Available";
        this.showPhoneNumbers = false;
        this.timestamp = new Date().getTime();
        this.userLat = 0.0;
        this.userLng = 0.0;
        this.workerLat = 0.0;
        this.workerLng = 0.0;
    }

    public Order(String orderId, String serviceName, String customerName, String userId, String userName,
                 String userPhone, String workerId, String workerName, String workerPhone, String status,
                 String price, String paymentMethod, String paymentStatus, String orderAddress, boolean showPhoneNumbers,
                 long timestamp, double userLat, double userLng, double workerLat, double workerLng) {
        this.orderId = orderId;
        this.serviceName = serviceName;
        this.customerName = customerName;
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.workerId = workerId;
        this.workerName = workerName;
        this.workerPhone = workerPhone;
        this.status = status;
        this.price = price;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.orderAddress = orderAddress;
        this.showPhoneNumbers = showPhoneNumbers;
        this.timestamp = timestamp;
        this.userLat = userLat;
        this.userLng = userLng;
        this.workerLat = workerLat;
        this.workerLng = workerLng;
    }

    public String getOrderId() { return orderId; }
    public String getServiceName() { return serviceName; }
    public String getCustomerName() { return customerName; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserPhone() { return userPhone; }
    public String getWorkerId() { return workerId; }
    public String getWorkerName() { return workerName; }
    public String getWorkerPhone() { return workerPhone; }
    public String getStatus() { return status; }
    public String getPrice() { return price; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getOrderAddress() { return orderAddress; }
    public boolean isShowPhoneNumbers() { return showPhoneNumbers; }
    public Long getTimestamp() { return timestamp; }
    public double getUserLat() { return userLat; }
    public double getUserLng() { return userLng; }
    public double getWorkerLat() { return workerLat; } // Get worker latitude
    public double getWorkerLng() { return workerLng; } // Get worker longitude
    public Date getFormattedTimestamp() { return new Date(timestamp); }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public void setWorkerPhone(String workerPhone) { this.workerPhone = workerPhone; }
    public void setStatus(String status) { this.status = status; }
    public void setPrice(String price) { this.price = price; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setOrderAddress(String orderAddress) { this.orderAddress = orderAddress; }
    public void setShowPhoneNumbers(boolean showPhoneNumbers) { this.showPhoneNumbers = showPhoneNumbers; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setUserLat(double userLat) { this.userLat = userLat; }
    public void setUserLng(double userLng) { this.userLng = userLng; }
    public void setWorkerLat(double workerLat) { this.workerLat = workerLat; } // Set worker latitude
    public void setWorkerLng(double workerLng) { this.workerLng = workerLng; } // Set worker longitude
}