package com.example.uninaswapoobd2425.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    // MODIFICA in base al tuo DB
    private static final String URL  = "jdbc:postgresql://localhost:5432/uninaswap";
    private static final String USER = "postgres";
    private static final String PASS = "2005cristian";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
