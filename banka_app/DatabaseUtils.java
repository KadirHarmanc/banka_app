package com.mycompany.banka_app;

import java.sql.*;

public class DatabaseUtils {

    public static String getEmailByTC(String userTC) {
        String email = null;
        String query = "SELECT email FROM kullanicilar WHERE tcKimlikNo = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userTC);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                email = rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return email;
    }
}
