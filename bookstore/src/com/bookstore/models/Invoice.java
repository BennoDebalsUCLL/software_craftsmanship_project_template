package com.bookstore.models;

import java.util.Date;

public class Invoice {

    private String id;
    private String orderId;
    private String clientEmail;
    private String clientName;
    private double amountExclVat;
    private double vatAmount;
    private double amountInclVat;
    private String status;
    private Date issuedAt;

    public Invoice() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public double getAmountExclVat() {
        return amountExclVat;
    }

    public void setAmountExclVat(double amountExclVat) {
        this.amountExclVat = amountExclVat;
    }

    public double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public double getAmountInclVat() {
        return amountInclVat;
    }

    public void setAmountInclVat(double amountInclVat) {
        this.amountInclVat = amountInclVat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }
}
