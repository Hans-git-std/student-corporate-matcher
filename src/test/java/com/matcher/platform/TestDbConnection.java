package com.matcher.platform;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestDbConnection {

    @Test
    void testDirectConnection() {
        String url = "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/test?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&allowPublicKeyRetrieval=true&characterEncoding=utf8&serverTimezone=UTC";
        String user = "q3hd9LT3m4krfbn.root";
        String pass = "9DhbOPzviuuR7mYr";

        System.out.println("Attempting connection to TiDB Cloud...");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("CONNECTION SUCCESSFUL! Valid: " + conn.isValid(5));
        } catch (Exception e) {
            System.err.println("Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
