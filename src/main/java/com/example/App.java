package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class App {
    public void unsafeQuery(String userInput) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        Statement stmt = conn.createStatement();

        // ⚠️ This should trigger a SQL injection rule
        String query = "SELECT * FROM users WHERE username = '" + userInput + "'";
        stmt.executeQuery(query);
    }

    public void unsafeXSS(String input) {
        // ⚠️ This should trigger an XSS rule in some HTML contexts
        System.out.println("<div>" + input + "</div>");
    }
}

