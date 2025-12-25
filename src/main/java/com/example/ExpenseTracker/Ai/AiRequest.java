package com.example.ExpenseTracker.Ai;

public class AiRequest {
    private String message;
    private String month; // optional, format YYYY-MM

    public AiRequest() {}
    public AiRequest(String message, String month) { this.message = message; this.month = month; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
}
