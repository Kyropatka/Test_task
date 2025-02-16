package com.gmail.deniska1406sme.test_task;

public class Trade {
    private String date;
    private String productName;
    private String currency;
    private Double price;

    public Trade(String date, String productId, String currency, Double price) {
        this.date = date;
        this.productName = productId;
        this.currency = currency;
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productId) {
        this.productName = productId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
