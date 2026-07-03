package hcmuaf.sad.pet_store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ResetDatabase {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "123456";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Dropping database pet_store...");
            stmt.executeUpdate("DROP DATABASE IF EXISTS pet_store");
            
            System.out.println("Recreating database pet_store...");
            stmt.executeUpdate("CREATE DATABASE pet_store");
            
            System.out.println("Database reset successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
