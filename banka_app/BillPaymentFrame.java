
package com.mycompany.banka_app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BillPaymentFrame extends JFrame {
    private JLabel phoneNumberLabel;
    private JTextField phoneNumberField;
    private JButton payButton;
    private JButton backButton;  // Yeni buton
    private JComboBox<String> billTypeComboBox;
    private JTextField addressCityField, addressDistrictField, addressStreetField, addressBuildingField, addressApartmentField;
    private JComboBox<String> phoneBillOptionComboBox;  // Kendi faturası ya da başkasının faturası
    private String phoneNumber;
    private String billType;  // Fatura türü (Elektrik, Su, Doğalgaz)
    private String userTC;


    public BillPaymentFrame(String userTC) {
        this.userTC = userTC;
        setTitle("Fatura Ödeme");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Ana Panel ve Layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Boşluk ayarı
        gbc.fill = GridBagConstraints.HORIZONTAL;
        setContentPane(mainPanel);

        // Kullanıcı Telefon Numarası
        phoneNumberLabel = new JLabel("Telefon Numarası:");
        phoneNumberField = new JTextField(getPhoneNumberByTC(userTC));
        phoneNumberField.setPreferredSize(new Dimension(200, 25));

        // Fatura Türü
        billTypeComboBox = new JComboBox<>(new String[]{"Telefon Faturası", "Elektrik Faturası", "Su Faturası", "Doğalgaz Faturası"});
        billTypeComboBox.addActionListener(e -> handleBillTypeSelection());

        // Telefon Faturası Seçeneği
        phoneBillOptionComboBox = new JComboBox<>(new String[]{"Kendi Faturam", "Başkasının Faturası"});
        phoneBillOptionComboBox.setEnabled(false);

        // Adres Bilgileri
        addressCityField = new JTextField();
        addressDistrictField = new JTextField();
        addressStreetField = new JTextField();
        addressBuildingField = new JTextField();
        addressApartmentField = new JTextField();

        // Ödeme Butonu
        payButton = new JButton("Öde");
        payButton.addActionListener(e -> payBill(userTC));

        // Geri Dön Butonu (Yeni eklenen buton)
        backButton = new JButton("Ana Sayfaya Dön");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ana sayfaya yönlendir
                new Banka_App(userTC).setVisible(true);
                dispose();  // Mevcut pencereyi kapat
            }
        });

        // Telefon Bilgileri
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(phoneNumberLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(phoneNumberField, gbc);

        // Fatura Türü
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Fatura Türü:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(billTypeComboBox, gbc);

        // Telefon Faturası Seçeneği
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Telefon Faturası Seçeneği:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(phoneBillOptionComboBox, gbc);

        // Adres Bilgileri
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("İl:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(addressCityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("İlçe:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(addressDistrictField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("Sokak:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(addressStreetField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(new JLabel("Bina No:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(addressBuildingField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        mainPanel.add(new JLabel("Daire No:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(addressApartmentField, gbc);

        // Ödeme Butonu
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(payButton, gbc);

        // Geri Dön Butonu
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(backButton, gbc);

        // Başlangıçta adres alanlarını gizle
        hideAddressFields();

        setVisible(true);
    }
    
    // Veritabanı sorgusu ile TC'ye karşılık gelen telefon numarasını alır
    private String getPhoneNumberByTC(String userTC) {
        try (Connection conn = DatabaseConnection.connect()) {
            // TC numarasına göre telefon numarasını al
            String sql = "SELECT telefon FROM kullanicilar WHERE tcKimlikNo = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userTC);  // TC numarasını kullanarak sorgu yap

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("telefon");
            } else {
                return null;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Fatura türü seçildiğinde gerekli alanları göster
    private void handleBillTypeSelection() {
        billType = (String) billTypeComboBox.getSelectedItem();  // Seçilen fatura türünü al

        if ("Telefon Faturası".equals(billType)) {
            // Telefon faturası seçildiyse telefon faturası seçeneklerini göster
            phoneBillOptionComboBox.setEnabled(true);
            showAddressFields(false);  // Adres alanlarını gizle
        } else {
            // Diğer fatura türleri seçildiyse adres alanlarını göster
            phoneBillOptionComboBox.setEnabled(false);
            showAddressFields(true);  // Adres alanlarını göster
        }
    }

    // Adres bilgilerini göster ya da gizle
    private void showAddressFields(boolean show) {
        addressCityField.setVisible(show);
        addressDistrictField.setVisible(show);
        addressStreetField.setVisible(show);
        addressBuildingField.setVisible(show);
        addressApartmentField.setVisible(show);
        
           // Adres kutularını içeren paneli yeniden çizdir
    addressCityField.getParent().revalidate();
    addressCityField.getParent().repaint();
        
    }

    // Adres bilgilerini gizle
    private void hideAddressFields() {
        addressCityField.setVisible(false);
        addressDistrictField.setVisible(false);
        addressStreetField.setVisible(false);
        addressBuildingField.setVisible(false);
        addressApartmentField.setVisible(false);
    }

    // Kullanıcının seçtiği faturayı ödemek için gerekli işlemleri yapar
    private void payBill(String userTC) {
        phoneNumber = phoneNumberField.getText();  // Kullanıcının telefon numarasını al

        // Telefon numarası boş olamaz
        if (phoneNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Telefon numarası boş olamaz.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Seçilen fatura türüne göre ödeme işlemi yap
        switch (billType) {
            case "Telefon Faturası":
                payPhoneBill(userTC);
                break;
            case "Elektrik Faturası":
                payElectricBill(userTC);
                break;
            case "Su Faturası":
                payWaterBill(userTC);
                break;
            case "Doğalgaz Faturası":
                payGasBill(userTC);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Geçersiz fatura türü.", "Hata", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

 
private void payPhoneBill(String userTC) {
    // Kullanıcı seçimini al
    String billOption = (String) phoneBillOptionComboBox.getSelectedItem();

    if ("Kendi Faturam".equals(billOption)) {
        // Kullanıcının telefon numarasını otomatik doldur
        phoneNumberField.setText(getUserPhoneNumber(userTC));
        payOwnPhoneBill(userTC);
    } else if ("Başkasının Faturası".equals(billOption)) {
        // Eğer başkasının faturası seçildiyse telefon numarasını temizle, ancak bu sadece ilk seçimde yapılır
        if (phoneNumberField.getText().isEmpty()) {
            phoneNumberField.setText(""); // Kutuyu temizle
        }
        payOtherPhoneBill(userTC);
    }
}


private void payOwnPhoneBill(String userTC) {
    String phoneNumber = phoneNumberField.getText().trim(); // Kullanıcının telefon numarası

    if (phoneNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Telefon numarası boş olamaz.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = DatabaseConnection.connect()) {
        // Girilen telefon numarasının veritabanında olup olmadığını kontrol et
        String sqlCheck = "SELECT * FROM telefon_faturasi WHERE telefonNumarasi = ?";
        PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck);
        stmtCheck.setString(1, phoneNumber);
        ResultSet rs = stmtCheck.executeQuery();

        if (!rs.next()) {
            // Telefon numarası bulunamadı
            JOptionPane.showMessageDialog(this, "Girilen telefon numarasına ait bir fatura bulunamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Telefon numarasına ait ödenmemiş fatura var mı kontrol et
        boolean odemeDurumu = rs.getBoolean("odemeDurumu");
        if (odemeDurumu) {
            JOptionPane.showMessageDialog(this, "Bu telefon numarasına ait fatura zaten ödenmiş.", "Hata", JOptionPane.ERROR_MESSAGE);
        } else {
            // Faturayı ödendi olarak güncelle
            String sqlUpdate = "UPDATE telefon_faturasi SET odemeDurumu = 1 WHERE telefonNumarasi = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setString(1, phoneNumber);
            stmtUpdate.executeUpdate();

            JOptionPane.showMessageDialog(this, "Telefon faturası başarıyla ödendi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
}


private void payOtherPhoneBill(String userTC) {
    String phoneNumber = phoneNumberField.getText().trim(); // Kullanıcının girdiği telefon numarası

    if (phoneNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Lütfen bir telefon numarası girin.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = DatabaseConnection.connect()) {
        // Girilen telefon numarasının veritabanında olup olmadığını kontrol et
        String sqlCheck = "SELECT * FROM telefon_faturasi WHERE telefonNumarasi = ?";
        PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck);
        stmtCheck.setString(1, phoneNumber);
        ResultSet rs = stmtCheck.executeQuery();

        if (!rs.next()) {
            // Telefon numarası bulunamadı
            JOptionPane.showMessageDialog(this, "Girilen telefon numarasına ait bir fatura bulunamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Telefon numarasına ait ödenmemiş fatura var mı kontrol et
        boolean odemeDurumu = rs.getBoolean("odemeDurumu");
        if (odemeDurumu) {
            JOptionPane.showMessageDialog(this, "Bu telefon numarasına ait fatura zaten ödenmiş.", "Hata", JOptionPane.ERROR_MESSAGE);
        } else {
            // Faturayı ödendi olarak güncelle
            String sqlUpdate = "UPDATE telefon_faturasi SET odemeDurumu = 1 WHERE telefonNumarasi = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setString(1, phoneNumber);
            stmtUpdate.executeUpdate();

            JOptionPane.showMessageDialog(this, "Telefon faturası başarıyla ödendi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
}



// Kullanıcının telefon numarasını al (örnek bir fonksiyon, veritabanı sorgusuyla değiştirilebilir)
private String getUserPhoneNumber(String userTC) {
    try (Connection conn = DatabaseConnection.connect()) {
        // kullanıcılar tablosundan telefon numarasını al
        String sql = "SELECT telefon FROM kullanicilar WHERE tcKimlikNo = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, userTC);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("telefon");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
    return "";
}




    // Elektrik faturası ödeme işlemi
private void payElectricBill(String userTC) {
    String city = addressCityField.getText();
    String district = addressDistrictField.getText();
    String street = addressStreetField.getText();
    String buildingNumber = addressBuildingField.getText();
    String apartmentNumber = addressApartmentField.getText();

    if (city.isEmpty() || district.isEmpty() || street.isEmpty() || buildingNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Adres bilgileri eksik. Lütfen tamamlayınız.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = DatabaseConnection.connect()) {
        // Fatura ödeme durumu kontrolü
        String checkSql = "SELECT odemeDurumu FROM adresli_faturalar WHERE faturaTuru = ? AND adresIl = ? AND adresIlce = ? AND adresSokak = ? AND adresBinaNumarasi = ? AND adresDaireNumarasi = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, "Elektrik Faturası");
        checkStmt.setString(2, city);
        checkStmt.setString(3, district);
        checkStmt.setString(4, street);
        checkStmt.setString(5, buildingNumber);
        checkStmt.setString(6, apartmentNumber);

        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            int paymentStatus = rs.getInt("odemeDurumu");
            if (paymentStatus == 1) {
                JOptionPane.showMessageDialog(this, "Bu elektrik faturası zaten ödendi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                return;  // Fatura zaten ödendiyse işlem yapılmaz.
            }
        }

        // Fatura ödemesi işlemi
        String sql = "INSERT INTO adresli_faturalar (faturaTuru, adresIl, adresIlce, adresSokak, adresBinaNumarasi, adresDaireNumarasi, odemeDurumu) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, "Elektrik Faturası");
        stmt.setString(2, city);
        stmt.setString(3, district);
        stmt.setString(4, street);
        stmt.setString(5, buildingNumber);
        stmt.setString(6, apartmentNumber);
        stmt.setInt(7, 1); // Ödeme durumu (ödendi: 1)

        stmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Elektrik faturası başarıyla ödendi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Fatura ödemesi sırasında hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
}



    // Su faturası ödeme işlemi
private void payWaterBill(String userTC) {
    String city = addressCityField.getText();
    String district = addressDistrictField.getText();
    String street = addressStreetField.getText();
    String buildingNumber = addressBuildingField.getText();
    String apartmentNumber = addressApartmentField.getText();

    if (city.isEmpty() || district.isEmpty() || street.isEmpty() || buildingNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Adres bilgileri eksik. Lütfen tamamlayınız.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = DatabaseConnection.connect()) {
        // Fatura ödeme durumu kontrolü
        String checkSql = "SELECT odemeDurumu FROM adresli_faturalar WHERE faturaTuru = ? AND adresIl = ? AND adresIlce = ? AND adresSokak = ? AND adresBinaNumarasi = ? AND adresDaireNumarasi = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, "Su Faturası");
        checkStmt.setString(2, city);
        checkStmt.setString(3, district);
        checkStmt.setString(4, street);
        checkStmt.setString(5, buildingNumber);
        checkStmt.setString(6, apartmentNumber);

        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            int paymentStatus = rs.getInt("odemeDurumu");
            if (paymentStatus == 1) {
                JOptionPane.showMessageDialog(this, "Bu su faturası zaten ödendi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                return;  // Fatura zaten ödendiyse işlem yapılmaz.
            }
        }

        // Fatura ödemesi işlemi
        String sql = "INSERT INTO adresli_faturalar (faturaTuru, adresIl, adresIlce, adresSokak, adresBinaNumarasi, adresDaireNumarasi, odemeDurumu) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, "Su Faturası");
        stmt.setString(2, city);
        stmt.setString(3, district);
        stmt.setString(4, street);
        stmt.setString(5, buildingNumber);
        stmt.setString(6, apartmentNumber);
        stmt.setInt(7, 1); // Ödeme durumu (ödendi: 1)

        stmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Su faturası başarıyla ödendi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Fatura ödemesi sırasında hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
}



    // Doğalgaz faturası ödeme işlemi
private void payGasBill(String userTC) {
    String city = addressCityField.getText();
    String district = addressDistrictField.getText();
    String street = addressStreetField.getText();
    String buildingNumber = addressBuildingField.getText();
    String apartmentNumber = addressApartmentField.getText();

    if (city.isEmpty() || district.isEmpty() || street.isEmpty() || buildingNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Adres bilgileri eksik. Lütfen tamamlayınız.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = DatabaseConnection.connect()) {
        // Fatura ödeme durumu kontrolü
        String checkSql = "SELECT odemeDurumu FROM adresli_faturalar WHERE faturaTuru = ? AND adresIl = ? AND adresIlce = ? AND adresSokak = ? AND adresBinaNumarasi = ? AND adresDaireNumarasi = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, "Doğalgaz Faturası");
        checkStmt.setString(2, city);
        checkStmt.setString(3, district);
        checkStmt.setString(4, street);
        checkStmt.setString(5, buildingNumber);
        checkStmt.setString(6, apartmentNumber);

        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            int paymentStatus = rs.getInt("odemeDurumu");
            if (paymentStatus == 1) {
                JOptionPane.showMessageDialog(this, "Bu doğalgaz faturası zaten ödendi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                return;  // Fatura zaten ödendiyse işlem yapılmaz.
            }
        }

        // Fatura ödemesi işlemi
        String sql = "INSERT INTO adresli_faturalar (faturaTuru, adresIl, adresIlce, adresSokak, adresBinaNumarasi, adresDaireNumarasi, odemeDurumu) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, "Doğalgaz Faturası");
        stmt.setString(2, city);
        stmt.setString(3, district);
        stmt.setString(4, street);
        stmt.setString(5, buildingNumber);
        stmt.setString(6, apartmentNumber);
        stmt.setInt(7, 1); // Ödeme durumu (ödendi: 1)

        stmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Doğalgaz faturası başarıyla ödendi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Fatura ödemesi sırasında hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
}

}

