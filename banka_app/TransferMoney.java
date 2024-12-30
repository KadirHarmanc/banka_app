package com.mycompany.banka_app;

import javax.swing.*;
import java.sql.*;

public class TransferMoney {
    public static void showTransferDialog(String userTC) {
        // Kullanıcının IBAN'ını çekme
        String userIBAN = getUserIBAN(userTC);

        if (userIBAN == null) {
            JOptionPane.showMessageDialog(null, "IBAN alınamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kullanıcıdan transfer bilgilerini alma
        String receiverIBAN = JOptionPane.showInputDialog(null, "Göndermek istediğiniz IBAN'ı girin (Örnek: TR748674...):");
        String receiverName = JOptionPane.showInputDialog(null, "Alıcının ismini girin:");
        String receiverSurname = JOptionPane.showInputDialog(null, "Alıcının soyadını girin:");
        String inputAmount = JOptionPane.showInputDialog(null, "Göndermek istediğiniz miktarı girin:");

        try {
            double amount = Double.parseDouble(inputAmount);

            // Geçerli bir miktar girildiğinden emin olun
            if (amount <= 0) {
                JOptionPane.showMessageDialog(null, "Lütfen geçerli bir tutar girin.", "Hata", JOptionPane.ERROR_MESSAGE);
            } else {
                int response = JOptionPane.showConfirmDialog(null, 
                        "Transferi onaylıyor musunuz?\nAlıcı IBAN: " + receiverIBAN + "\nTutar: " + amount + " ₺", 
                        "Onay", JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    // Para transferi işlemi
                    executeTransfer(userTC, userIBAN, receiverIBAN, amount);
                    JOptionPane.showMessageDialog(null, "Transfer başarıyla gerçekleşti.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Lütfen geçerli bir sayısal değer girin.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String getUserIBAN(String userTC) {
        // Veritabanından kullanıcının IBAN'ını almak için SQL sorgusu
        String sql = "SELECT iban FROM kullanicilar WHERE tcKimlikNo = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userTC);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("iban");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void executeTransfer(String userTC, String senderIBAN, String receiverIBAN, double amount) {
        // Transferi gerçekleştirmek için SQL sorguları
        String deductSQL = "UPDATE kullanicilar SET para = para - ? WHERE tcKimlikNo = ?";
        String addSQL = "UPDATE kullanicilar SET para = para + ? WHERE iban = ?";

        try (Connection conn = DatabaseConnection.connect()) {
            // Gönderenin bakiyesinden düş
            PreparedStatement deductStmt = conn.prepareStatement(deductSQL);
            deductStmt.setDouble(1, amount);
            deductStmt.setString(2, userTC);
            deductStmt.executeUpdate();

            // Alıcının bakiyesine ekle
            PreparedStatement addStmt = conn.prepareStatement(addSQL);
            addStmt.setDouble(1, amount);
            addStmt.setString(2, receiverIBAN);
            addStmt.executeUpdate();

            // Hesap geçmişini kaydet
            String insertHistorySQL = "INSERT INTO hesap_gecmisi (tcKimlikNo, islem_turu, islem_tutari, islem_tarihi) VALUES (?, ?, ?, ?)";
            PreparedStatement insertHistoryStmt = conn.prepareStatement(insertHistorySQL);
            insertHistoryStmt.setString(1, userTC);
            insertHistoryStmt.setString(2, "Transfer");
            insertHistoryStmt.setDouble(3, amount);
            insertHistoryStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            insertHistoryStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
