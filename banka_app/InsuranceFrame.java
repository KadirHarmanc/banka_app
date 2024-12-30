package com.mycompany.banka_app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InsuranceFrame extends JFrame {
    private JComboBox<String> insuranceTypeCombo, planCombo;
    private JTextField detailsField, startDateField, endDateField;
    private JLabel totalPriceLabel, detailsLabel;
    private JButton saveButton, backButton;  // Yeni buton
    private String userTC;

    // Monthly insurance prices for each type
    private final double PHONE_INSURANCE_PRICE = 50.0;
    private final double HOUSE_INSURANCE_PRICE = 100.0;
    private final double CAR_INSURANCE_PRICE = 150.0;
    private final double TRAVEL_INSURANCE_PRICE = 75.0;
    private final double HEALTH_INSURANCE_PRICE = 200.0;

    public InsuranceFrame(String userTC) {
        this.userTC = userTC;

        // Frame settings
        setTitle("Sigorta İşlemleri");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(9, 2, 10, 10));  // Grid layout'u bir satır artırdık
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Components
        JLabel insuranceTypeLabel = new JLabel("Sigorta Türü:");
        insuranceTypeCombo = new JComboBox<>(new String[]{"Telefon", "Ev", "Araç", "Seyahat", "Sağlık"});

        detailsLabel = new JLabel("Sigorta Detayları:");
        detailsField = new JTextField();

        JLabel startDateLabel = new JLabel("Başlangıç Tarihi (YYYY-AA-GG):");
        startDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        JLabel endDateLabel = new JLabel("Bitiş Tarihi (YYYY-AA-GG):");
        endDateField = new JTextField();

        JLabel planLabel = new JLabel("Plan Süresi (Aylık):");
        planCombo = new JComboBox<>(new String[]{"3", "6", "9", "12"});

        JLabel priceLabel = new JLabel("Ödenecek Tutar:");
        totalPriceLabel = new JLabel("0 ₺");

        saveButton = new JButton("Sigortayı Kaydet");

        // Yeni eklenen geri dön butonu
        backButton = new JButton("Ana Sayfaya Dön");
        backButton.addActionListener(e -> {
            // Ana sayfaya yönlendir
            new Banka_App(userTC).setVisible(true);
            dispose();  // Mevcut pencereyi kapat
        });

        // Adding components to panel
        panel.add(insuranceTypeLabel);
        panel.add(insuranceTypeCombo);

        panel.add(detailsLabel);
        panel.add(detailsField);

        panel.add(startDateLabel);
        panel.add(startDateField);

        panel.add(endDateLabel);
        panel.add(endDateField);

        panel.add(planLabel);
        panel.add(planCombo);

        panel.add(priceLabel);
        panel.add(totalPriceLabel);

        panel.add(saveButton);

        // Geri Dön Butonu
        panel.add(backButton);  // Yeni butonu panel'e ekliyoruz

        // Add panel to frame
        add(panel);

        // Default selection
        insuranceTypeCombo.setSelectedIndex(0);

        // Event listeners
        insuranceTypeCombo.addActionListener(e -> {
            updateDetailsField();
            updateTotalPrice();
        });

        planCombo.addActionListener(e -> updateTotalPrice());

        saveButton.addActionListener(e -> saveInsurance());

        setVisible(true);
    }
    private void updateDetailsField() {
        String insuranceType = (String) insuranceTypeCombo.getSelectedItem();
        switch (insuranceType) {
            case "Telefon":
                detailsLabel.setText("IMEI Numarası:");
                break;
            case "Ev":
                detailsLabel.setText("Adres:");
                break;
            case "Araç":
                detailsLabel.setText("Plaka:");
                break;
            case "Seyahat":
                detailsLabel.setText("Gideceğiniz Ülke:");
                break;
            case "Sağlık":
                detailsLabel.setText("TC Kimlik No:");
                break;
            default:
                detailsLabel.setText("");
                break;
        }
    }

    private void updateTotalPrice() {
        String insuranceType = (String) insuranceTypeCombo.getSelectedItem();
        int months = Integer.parseInt((String) planCombo.getSelectedItem());

        double monthlyPrice = 0.0;
        switch (insuranceType) {
            case "Telefon":
                monthlyPrice = PHONE_INSURANCE_PRICE;
                break;
            case "Ev":
                monthlyPrice = HOUSE_INSURANCE_PRICE;
                break;
            case "Araç":
                monthlyPrice = CAR_INSURANCE_PRICE;
                break;
            case "Seyahat":
                monthlyPrice = TRAVEL_INSURANCE_PRICE;
                break;
            case "Sağlık":
                monthlyPrice = HEALTH_INSURANCE_PRICE;
                break;
            default:
                monthlyPrice = 0.0;
                break;
        }

        double totalPrice = monthlyPrice * months;
        totalPriceLabel.setText(String.format("%.2f ₺", totalPrice));

        LocalDate startDate = LocalDate.parse(startDateField.getText());
        LocalDate endDate = startDate.plusMonths(months);
        endDateField.setText(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
private void saveInsurance() {
    String insuranceType = (String) insuranceTypeCombo.getSelectedItem();
    String details = detailsField.getText();
    String startDate = startDateField.getText();
    String endDate = endDateField.getText();
    String totalPriceString = totalPriceLabel.getText().replace(" ₺", "").replace(",", ".");
    double totalPrice = Double.parseDouble(totalPriceString); // Noktaya dönüştürülmüş string

    if (details.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Kullanıcı ID'sini TC Kimlik Numarası ile veritabanından alalım
    String userID = getUserIDFromTC(userTC);

    if (userID == null) {
        JOptionPane.showMessageDialog(this, "Kullanıcı bulunamadı!", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Kullanıcının mevcut bakiyesini alalım
    double userBalance = getUserBalance(userTC); // TC Kimlik Numarası ile bakiyeyi al

    // Yetersiz bakiye kontrolü
    if (userBalance < totalPrice) {
        JOptionPane.showMessageDialog(this, "Yetersiz bakiye! Sigorta ödemesi için yeterli bakiyeniz yok.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    System.out.println("Mevcut Bakiye: " + userBalance); // Debug çıktısı

    // Sigorta işlemi kaydını veritabanına ekleyelim
    try (Connection conn = DatabaseConnection.connect()) {
        String sql = "INSERT INTO sigorta (kullanici_id, sigorta_turu, detaylar, baslangic_tarihi, bitis_tarihi, odenen_tutar) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, userID); // Kullanıcı ID'sini kullanarak kaydet
        stmt.setString(2, insuranceType);
        stmt.setString(3, details);
        stmt.setString(4, startDate);
        stmt.setString(5, endDate);
        stmt.setDouble(6, totalPrice);

        int rowsAffected = stmt.executeUpdate();
        System.out.println("Rows affected: " + rowsAffected); // Debug çıktısı

        if (rowsAffected > 0) {
            // Sigorta kaydedildikten sonra, kullanıcı bakiyesini güncelle
            updateUserBalance(totalPrice); // Bakiye güncelleme (ödeme yapılacak tutar kadar)
            System.out.println("Yeni Bakiye Güncellemesi Yapıldı: " + (userBalance - totalPrice)); // Debug çıktısı

            JOptionPane.showMessageDialog(this, "Sigorta başarıyla kaydedildi.");
        } else {
            JOptionPane.showMessageDialog(this, "Bir hata oluştu, sigorta kaydedilemedi.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
}

    // Kullanıcı ID'sini TC Kimlik No'suna göre almak
    private String getUserIDFromTC(String tcKimlikNo) {
        String userID = null;
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT id FROM kullanicilar WHERE tcKimlikNo = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, tcKimlikNo);  // TC Kimlik Numarasını sorguya bağla
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userID = rs.getString("id");  // Kullanıcı ID'sini al
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kullanıcı ID'si alınamadı: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
        return userID;
    }

private double getUserBalance(String userTC) {
    double balance = 0.0;
    try (Connection conn = DatabaseConnection.connect()) {
        String sql = "SELECT para FROM kullanicilar WHERE tcKimlikNo = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, userTC);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            balance = rs.getDouble("para");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Bakiye bilgisi alınamadı: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
    return balance;
}


private void updateUserBalance(double amountToDeduct) {
    double currentBalance = getUserBalance(userTC);  // Mevcut bakiyeyi al
    double newBalance = currentBalance - amountToDeduct;  // Yeni bakiye hesapla

    if (newBalance < 0) {
        JOptionPane.showMessageDialog(this, "Yetersiz bakiye!", "Hata", JOptionPane.ERROR_MESSAGE);
        return;  // Eğer yeni bakiye negatifse işlem yapma
    }

    try (Connection conn = DatabaseConnection.connect()) {
        String sql = "UPDATE kullanicilar SET para = ? WHERE tcKimlikNo = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDouble(1, newBalance);  // Yeni bakiyeyi gönder
        stmt.setString(2, userTC);  // Kullanıcının TC kimlik numarasını kullan
        stmt.executeUpdate();
        
        JOptionPane.showMessageDialog(this, "Bakiye başarıyla güncellendi.");
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Bakiye güncellenemedi: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
}

}
