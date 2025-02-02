package com.example.payment;

import java.sql.SQLException;

public class PaymentProcessor {
    private static final String API_KEY = "sk_test_123456";
    private final PaymentApi paymentApi;
    private final EmailService emailService;
    private final DatabaseConnection databaseConnection;

    public PaymentProcessor(PaymentApi paymentApi, EmailService emailService, DatabaseConnection databaseConnection) {
        this.paymentApi = paymentApi;
        this.emailService = emailService;
        this.databaseConnection = databaseConnection;
    }

    public boolean processPayment(double amount) throws SQLException {
        // Anropar extern betaltjänst direkt med statisk API-nyckel
        PaymentApiResponse response = paymentApi.charge(API_KEY, amount);

        // Skriver till databas direkt
        if (response.isSuccess()) {
            databaseConnection.getInstance()
                    .executeUpdate("INSERT INTO payments (amount, status) VALUES (" + amount + ", 'SUCCESS')");
        }

        // Skickar e-post direkt
        if (response.isSuccess()) {
            emailService.sendPaymentConfirmation("user@example.com", amount);
        }

        return response.isSuccess();
    }
}
