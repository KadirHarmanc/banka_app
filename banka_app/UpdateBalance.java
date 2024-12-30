/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.banka_app;

import java.sql.*;

public class UpdateBalance {

    public static void updateBalance(String userTC, double amount, String transactionType) {
        // amount: Kullanıcının ekleyeceği veya çıkaracağı tutar
        // transactionType: "Ekleme" veya "Çıkarma" işlemi belirtir

        // Bakiye güncelleme SQL sorgusu
        String updateBalanceSQL = "UPDATE kullanicilar SET para = para + ? WHERE tcKimlikNo = ?";

        // Hesap geçmişine kayıt ekleme SQL sorgusu
        String insertHistorySQL = "INSERT INTO hesap_gecmisi (tcKimlikNo, islem_turu, islem_tutari, islem_tarihi) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect()) {
            // İlk önce bakiyeyi güncelle
            PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceSQL);
            updateBalanceStmt.setDouble(1, amount);
            updateBalanceStmt.setString(2, userTC);
            int rowsUpdated = updateBalanceStmt.executeUpdate();

            if (rowsUpdated > 0) {
                // İkinci olarak, işlemi hesap geçmişi tablosuna kaydet
                PreparedStatement insertHistoryStmt = conn.prepareStatement(insertHistorySQL);
                insertHistoryStmt.setString(1, userTC);
                insertHistoryStmt.setString(2, transactionType);  // "Ekleme" veya "Çıkarma"
                insertHistoryStmt.setDouble(3, amount);
                insertHistoryStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

                insertHistoryStmt.executeUpdate();
                
            } else {
                System.out.println("Bakiye güncellenirken bir hata oluştu.");
            }
        } catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }
}

