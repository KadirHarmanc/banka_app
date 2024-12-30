package com.mycompany.banka_app;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AccountInfoFrame extends JFrame {
    private JLabel nameLabel, ibanLabel, balanceLabel, emailLabel, phoneLabel;
    private JTextField emailField, phoneField;
    private JButton editButton, saveButton, backButton;  // Yeni buton
    private String userTC;
    private boolean isEditing = false;  // Kullanıcının düzenleme modunda olup olmadığını kontrol eder

    public AccountInfoFrame(String userTC) {
        this.userTC = userTC;

        // Frame Ayarları
        setTitle("Hesap Bilgileri");
        setSize(500, 400);  // Formu daha geniş yaptık
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(9, 2, 10, 10));  // 9 satır, 2 sütun düzeni (Yeni buton için bir satır daha ekledik)
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Bileşenler
        nameLabel = new JLabel();
        ibanLabel = new JLabel();
        balanceLabel = new JLabel();
        emailLabel = new JLabel();
        phoneLabel = new JLabel();

        emailField = new JTextField();
        phoneField = new JTextField();

        editButton = new JButton("Bilgileri Düzenle");
        saveButton = new JButton("Değişiklikleri Kaydet");

        // Yeni eklenen geri dön butonu
        backButton = new JButton("Ana Sayfaya Dön");
        backButton.addActionListener(e -> {
            // Ana sayfaya yönlendir
            new Banka_App(userTC).setVisible(true);
            dispose();  // Mevcut pencereyi kapat
        });

        // Panel'e bileşenleri ekle
        panel.add(new JLabel("Ad Soyad:"));
        panel.add(nameLabel);

        panel.add(new JLabel("IBAN:"));
        panel.add(ibanLabel);

        panel.add(new JLabel("Bakiye:"));
        panel.add(balanceLabel);

        panel.add(new JLabel("E-mail:"));
        panel.add(emailLabel);

        panel.add(new JLabel("Telefon:"));
        panel.add(phoneLabel);

        panel.add(new JLabel("Yeni E-mail:"));
        panel.add(emailField);

        panel.add(new JLabel("Yeni Telefon:"));
        panel.add(phoneField);

        panel.add(editButton);  // Düzenleme butonu
        panel.add(saveButton);  // Kaydetme butonu
        panel.add(backButton);  // Geri dön butonu

        // Frame'e Paneli ekle
        add(panel);

        // Kullanıcı bilgilerini yükle
        loadUserInfo();

        // Düzenleme butonuna tıklanması için event
        editButton.addActionListener(e -> enableEditing());

        // Değişiklikleri kaydetmek için event
        saveButton.addActionListener(e -> saveChanges());

        // Başlangıçta input alanlarını gizle
        emailField.setVisible(false);
        phoneField.setVisible(false);
        saveButton.setVisible(false);  // Kaydet butonu da başlangıçta gizli

        setVisible(true);
    }


    private void loadUserInfo() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT ad, soyad, iban, para, email, telefon FROM kullanicilar WHERE tcKimlikNo = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userTC);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("ad") + " " + rs.getString("soyad");
                String iban = rs.getString("iban");
                double balance = rs.getDouble("para");
                String email = rs.getString("email");
                String phone = rs.getString("telefon");

                // Veritabanındaki bilgileri ekrana yazdır
                nameLabel.setText(name);
                ibanLabel.setText(iban);
                balanceLabel.setText(balance + " ₺");
                emailLabel.setText(email);
                phoneLabel.setText(phone);

                // Kullanıcı eğer değiştirmek isterse, email ve telefon bilgilerini ilgili alanda göster
                emailField.setText(email);
                phoneField.setText(phone);
            } else {
                JOptionPane.showMessageDialog(this, "Kullanıcı bilgileri bulunamadı!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enableEditing() {
        isEditing = true;
        
        // Düzenleme moduna geçildiğinde input alanlarını göster
        emailField.setVisible(true);
        phoneField.setVisible(true);
        saveButton.setVisible(true);  // Kaydet butonunu da göster

        editButton.setVisible(false);  // Düzenleme butonunu gizle
    }

    private void saveChanges() {
        String newEmail = emailField.getText();
        String newPhone = phoneField.getText();

        if (newEmail.isEmpty() || newPhone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen e-mail ve telefon numarasını girin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "UPDATE kullanicilar SET email = ?, telefon = ? WHERE tcKimlikNo = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newEmail);
            stmt.setString(2, newPhone);
            stmt.setString(3, userTC);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Bilgiler başarıyla güncellendi.");
                loadUserInfo();  // Güncellenen bilgileri tekrar yükle

                // Düzenleme modunu sonlandır
                emailField.setVisible(false);
                phoneField.setVisible(false);
                saveButton.setVisible(false);
                editButton.setVisible(true);  // Düzenleme butonunu tekrar göster
            } else {
                JOptionPane.showMessageDialog(this, "Bir hata oluştu, bilgiler güncellenemedi.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
