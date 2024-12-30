package com.mycompany.banka_app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection connect() throws SQLException {
        try {
            // Doğru bağlantı bilgilerini kontrol edin
            String url = "jdbc:mysql://localhost:3306/banka_uygulamasi"; // Veritabanı adı
            String username = "root";  // Kullanıcı adı
            String password = "";      // Şifre (boş bırakılmış olabilir)

            Connection conn = DriverManager.getConnection(url, username, password);
            return conn;
        } catch (SQLException e) {
            throw new SQLException("Veritabanı bağlantı hatası: " + e.getMessage());
        }
    }
}
