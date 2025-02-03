package com.example.payment;

import java.sql.PreparedStatement;

public interface DatabaseConnection {
    public PreparedStatement getInstance();

    void executeUpdate(String s);
}



