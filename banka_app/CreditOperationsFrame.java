package com.mycompany.banka_app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;


public class CreditOperationsFrame extends JFrame {
    private String userTC;

    public CreditOperationsFrame(String userTC) {
        this.userTC = userTC;
        setTitle("Kredi İşlemleri");
        setSize(400, 500); // Pencere boyutu
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ana panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Butonlar için panel
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10)); // 4 buton olacak
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Butonlar
        JButton applyButton = new JButton("Kredi Çekme");
        JButton payButton = new JButton("Kredi Ödeme");
        JButton backToMenuButton = new JButton("Ana Menüyü Görüntüle");

        // Butonların olay dinleyicileri
        applyButton.addActionListener(e -> openCreditApplication());
        payButton.addActionListener(e -> openCreditPayment());
        backToMenuButton.addActionListener(e -> returnToMainMenu());

        // Butonları buton paneline ekle
        buttonPanel.add(applyButton);
        buttonPanel.add(payButton);
        buttonPanel.add(backToMenuButton);

        // Paneli çerçeveye ekle
        panel.add(buttonPanel, BorderLayout.CENTER);
        add(panel);

        setVisible(true);
    }

    // Kredi Başvuru Formu
    private void openCreditApplication() {
        // Kredi başvurusu için yeni bir pencere açıyoruz
        JPanel formPanel = createCreditFormPanel();
        JOptionPane.showOptionDialog(this, formPanel, "Kredi Başvurusu", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
    }

private JPanel createCreditFormPanel() {
    JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10)); // Added an extra row for the interest label
    
    JLabel amountLabel = new JLabel("Kredi Miktarı:");
    JTextField amountField = new JTextField();
    JLabel durationLabel = new JLabel("Vade Seçimi:");
    JComboBox<String> durationCombo = new JComboBox<>(new String[]{"6 Ay", "12 Ay", "24 Ay", "36 Ay"});
    JLabel interestLabel = new JLabel("Tahmini Faiz: 0.00 TL");

    formPanel.add(amountLabel);
    formPanel.add(amountField);
    formPanel.add(durationLabel);
    formPanel.add(durationCombo);
    formPanel.add(interestLabel);

    // Adding caret listener to update the interest calculation
    amountField.addCaretListener(e -> calculateInterest(amountField, durationCombo, interestLabel));

    // Submit button to process the application
    JButton applyButton = new JButton("Başvuruyu Tamamla");
    applyButton.addActionListener(e -> submitCreditApplication(amountField, durationCombo, interestLabel));

    formPanel.add(new JLabel()); // Empty space
    formPanel.add(applyButton);

    return formPanel;
}

private void calculateInterest(JTextField amountField, JComboBox<String> durationCombo, JLabel interestLabel) {
        try {
            double amount = Double.parseDouble(amountField.getText());
            double interestRate = getInterestRateByDuration((String) durationCombo.getSelectedItem());
            double interest = amount * interestRate / 100;
            interestLabel.setText(String.format("Tahmini Faiz: %.2f TL", interest));
        } catch (NumberFormatException e) {
            interestLabel.setText("Tahmini Faiz: 0.00 TL");
        }
    }

private boolean hasOutstandingDebt(String userTC) {
    try (Connection conn = DatabaseConnection.connect()) {
        String query = "SELECT remainingDebt FROM credits WHERE userTC = ? AND status = 'Başvuru' ORDER BY creditDate DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userTC);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getDouble("remainingDebt") > 0) {
                return true; // Kullanıcının borcu varsa true döner
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false; // Borç yoksa false döner
}

private void submitCreditApplication(JTextField amountField, JComboBox<String> durationCombo, JLabel interestLabel) {
    // Geçerli kredi miktarını al
    double amount;
    try {
        amount = Double.parseDouble(amountField.getText());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Lütfen geçerli bir kredi miktarı giriniz.");
        return; // Geçersiz girişte işlemi durdur
    }

    // Kullanıcının mevcut borcu olup olmadığını kontrol et
    if (hasOutstandingDebt(userTC)) {
        JOptionPane.showMessageDialog(this, "Mevcut borcunuz olduğu için yeni kredi başvurusu yapamazsınız.");
        return; // Borcu olan kullanıcıya yeni kredi başvurusunu engelle
    }

    // Seçilen vade ve faiz oranını hesapla
    String selectedDuration = (String) durationCombo.getSelectedItem();
    int duration = Integer.parseInt(selectedDuration.split(" ")[0]); // Örneğin "6" Ay'dan alınır
    double interestRate = getInterestRateByDuration(selectedDuration);

    // Faiz ve toplam ödeme miktarını hesapla
    double interest = amount * interestRate / 100;
    double totalAmount = amount + interest;

    // Veritabanına kredi başvurusunu ekle
    try (Connection conn = DatabaseConnection.connect()) {
        String insertQuery = "INSERT INTO credits (userTC, creditAmount, remainingDebt, creditDate, status, duration, interestRate) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, userTC); // Kullanıcı TC kimlik numarası
            stmt.setDouble(2, amount); // Kredi miktarı
            stmt.setDouble(3, totalAmount); // Kredi borcu (faiz dahil)
            stmt.setString(4, java.time.LocalDate.now().toString()); // Başvuru tarihi
            stmt.setString(5, "Başvuru"); // Başvuru durumu
            stmt.setInt(6, duration); // Vade süresi
            stmt.setDouble(7, interestRate); // Faiz oranı

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Kullanıcı bakiyesini güncelle ve işlem geçmişine kaydı ekle
                updateUserBalanceAndHistory(userTC, amount, "Kredi");
                JOptionPane.showMessageDialog(this, "Kredi başvurunuz alındı ve bakiyenize eklendi!\nMiktar: " + amount +
                        "\nFaiz: " + interest + "\nVade: " + duration + " ay");
            } else {
                JOptionPane.showMessageDialog(this, "Başvuru işlemi sırasında bir hata oluştu. Lütfen tekrar deneyiniz.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Hatası: " + e.getMessage());
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Veritabanı bağlantısı hatası: " + e.getMessage());
    }
}

// Kullanıcının bakiyesini güncelleme ve işlem geçmişine "Kredi" kaydını ekleme
private void updateUserBalanceAndHistory(String userTC, double amount, String transactionType) {
    // Bakiye güncellemeyi ve işlem geçmişine kaydını burada yapıyoruz
    String updateBalanceSQL = "UPDATE kullanicilar SET para = para + ? WHERE tcKimlikNo = ?";
    String insertHistorySQL = "INSERT INTO hesap_gecmisi (tcKimlikNo, islem_turu, islem_tutari, islem_tarihi) VALUES (?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.connect()) {
        // İlk olarak bakiyeyi güncelle
        PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceSQL);
        updateBalanceStmt.setDouble(1, amount); // Kredi miktarını bakiyeye ekliyoruz
        updateBalanceStmt.setString(2, userTC);
        int rowsUpdated = updateBalanceStmt.executeUpdate();

        if (rowsUpdated > 0) {
            // Hesap geçmişine kaydı ekle
            PreparedStatement insertHistoryStmt = conn.prepareStatement(insertHistorySQL);
            insertHistoryStmt.setString(1, userTC);
            insertHistoryStmt.setString(2, transactionType); // "Kredi"
            insertHistoryStmt.setDouble(3, amount); // Kredi miktarı
            insertHistoryStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            insertHistoryStmt.executeUpdate();
        } else {
            System.out.println("Bakiye güncellenirken bir hata oluştu.");
        }
    } catch (SQLException e) {
        System.out.println("Veritabanı hatası: " + e.getMessage());
    }
}


// Vadeye göre faiz oranını belirlemek için bir yardımcı metod
private double getInterestRateByDuration(String duration) {
    switch (duration) {
        case "6 Ay":
            return 5.0; 
        case "12 Ay":
            return 7.0; 
        case "24 Ay":
            return 9.0;
        case "36 Ay":
            return 12.0;
        default:
            return 0.0; 
    }
}

// Kredi Ödeme Formu
private void openCreditPayment() {
    JPanel formPanel = createCreditPaymentFormPanel(userTC); // Kullanıcı TC kimlik numarasını parametre olarak gönderiyoruz
    JOptionPane.showOptionDialog(this, formPanel, "Kredi Ödeme", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
}

private JPanel createCreditPaymentFormPanel(String userTC) {
    JPanel formPanel = new JPanel();
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

    // Veritabanından kullanıcıya ait kredi bilgilerini çekiyoruz
    CreditInfo creditInfo = getCreditInfo(userTC); // Kredi bilgilerini al

    if (creditInfo != null && creditInfo.remainingDebt > 0) {
        // Kredi borcunu (remainingDebt) al
        double remainingDebt = creditInfo.remainingDebt;  // Faiz hesaplaması yapılmaz
        DecimalFormat df = new DecimalFormat("#,##0.00");
        double monthlyPayment = remainingDebt / creditInfo.duration;  // Aylık ödeme
        String formattedMonthlyPayment = df.format(monthlyPayment);  // Formatlanmış aylık ödeme

        // Kullanıcıya ödeme planını göster
        formPanel.add(new JLabel("Kalan Borç: " + df.format(remainingDebt) + " TL"));
        formPanel.add(new JLabel("Aylık Taksit: " + formattedMonthlyPayment + " TL"));

        // Kullanıcının bakiyesini al
        double userBalance = getUserBalance(userTC);

        // Taksit ödeme butonu
        JButton payInstallmentButton = new JButton("Taksit Öde");
        payInstallmentButton.setEnabled(userBalance >= monthlyPayment);
        payInstallmentButton.addActionListener(e -> makePayment(userTC, monthlyPayment, "Taksit Ödeme"));

        // Borcu kapama butonu
        JButton payFullDebtButton = new JButton("Borcu Kapat");
        payFullDebtButton.setEnabled(userBalance >= remainingDebt);
        payFullDebtButton.addActionListener(e -> makePayment(userTC, remainingDebt, "Borcu Kapat"));

        // Geri dön butonu
        JButton backButton = new JButton("Ana Menüye Dön");
        backButton.addActionListener(e -> {
            // Geri dönme işlemi
            System.out.println("Ana Menüye dönülüyor...");
            new CreditOperationsFrame(userTC);
        });

        // Butonları form paneline ekle
        formPanel.add(payInstallmentButton);
        formPanel.add(payFullDebtButton);
        formPanel.add(backButton);

    } else {
        // Kullanıcının borcu yoksa, bunu göster
        formPanel.add(new JLabel("Hiçbir borcunuz yok."));
    }

    return formPanel;
}

private void makePayment(String userTC, double paymentAmount, String paymentType) {
    try (Connection conn = DatabaseConnection.connect()) {
        conn.setAutoCommit(false);  // Atomic transaction için

        // Kullanıcının kredi bilgilerini al
        CreditInfo creditInfo = getCreditInfo(userTC);  // Kredi bilgilerini çek

        if (creditInfo == null) {
            JOptionPane.showMessageDialog(this, "Kredi kaydı bulunamadı.");
            return;
        }

        // Kredi borcunu (remainingDebt) al
        double remainingDebt = creditInfo.remainingDebt;

        // Eğer ödeme borcu karşılamıyorsa, hata mesajı göster
        if (paymentAmount < remainingDebt) {
            JOptionPane.showMessageDialog(this, "Ödemeniz yeterli değil.");
            return;
        }

        // Kullanıcı bakiyesini kontrol et
        double userBalance = getUserBalance(userTC); // Kullanıcı bakiyesini al
        if (userBalance < paymentAmount) {
            JOptionPane.showMessageDialog(this, "Yetersiz bakiye.");
            return;
        }

        // Ödeme geçmişi kaydı oluştur
        String insertHistoryQuery = "INSERT INTO hesap_gecmisi (tcKimlikNo, islem_turu, islem_tutari, islem_tarihi) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(insertHistoryQuery)) {
            stmt.setString(1, userTC);
            stmt.setString(2, paymentType);  // Taksit ödeme veya borcu kapama
            stmt.setDouble(3, paymentAmount);
            stmt.executeUpdate();
        }

        // Kredi borcunu güncelle
        double newRemainingDebt = remainingDebt - paymentAmount;
        if (newRemainingDebt < 0) {
            newRemainingDebt = 0;  // Borç tamamen ödendiğinde sıfırlanır
        }

        // Kredi kaydını güncelle
        String updateDebtQuery = "UPDATE credits SET remainingDebt = ? WHERE userTC = ? AND status = 'Başvuru' ORDER BY creditDate DESC LIMIT 1";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateDebtQuery)) {
            updateStmt.setDouble(1, newRemainingDebt);
            updateStmt.setString(2, userTC);
            updateStmt.executeUpdate();
        }

        // Kullanıcı bakiyesini güncelle
        updateUserBalance(userTC, -paymentAmount, conn);  // Kullanıcının bakiyesinden ödeme tutarını düşüyoruz

        // Borç sıfırlandığında kredi kaydını sil
        if (newRemainingDebt == 0) {
            String deleteQuery = "DELETE FROM credits WHERE userTC = ? AND status = 'Başvuru' ORDER BY creditDate DESC LIMIT 1";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setString(1, userTC);
                deleteStmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Borç tamamen ödendi ve kredi kaydı silindi.");
        }

        // İşlem başarılı
        conn.commit();
        JOptionPane.showMessageDialog(this, "Ödeme başarıyla yapıldı! Kalan borç: " + newRemainingDebt);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage());
    }
}

private CreditInfo getCreditInfo(String userTC) {
    CreditInfo creditInfo = null;
    try (Connection conn = DatabaseConnection.connect()) {
        String query = "SELECT remainingDebt, duration, interestRate FROM credits WHERE userTC = ? AND status = 'Başvuru' ORDER BY creditDate DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userTC);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                creditInfo = new CreditInfo(
                        rs.getDouble("remainingDebt"),
                        rs.getInt("duration"),
                        rs.getDouble("interestRate")
                );
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return creditInfo;
}

private double getRemainingDebt(String userTC, Connection conn) {
    double remainingDebt = 0.0;
    try (PreparedStatement stmt = conn.prepareStatement("SELECT remainingDebt FROM credits WHERE userTC = ? AND status = 'Başvuru' ORDER BY creditDate DESC LIMIT 1")) {
        stmt.setString(1, userTC);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            remainingDebt = rs.getDouble("remainingDebt");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return remainingDebt;
}

private double getUserBalance(String userTC) {
    double balance = 0.0;
    try (Connection conn = DatabaseConnection.connect()) {
        String query = "SELECT para FROM kullanicilar WHERE tcKimlikNo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userTC);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                balance = rs.getDouble("para");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return balance;
}

private void updateUserBalance(String userTC, double amount, Connection conn) throws SQLException {
    String updateBalanceQuery = "UPDATE kullanicilar SET para = para + ? WHERE tcKimlikNo = ?";
    try (PreparedStatement stmt = conn.prepareStatement(updateBalanceQuery)) {
        stmt.setDouble(1, amount);
        stmt.setString(2, userTC);
        stmt.executeUpdate();
    }
}

private class CreditInfo {
    double remainingDebt;
    int duration;
    double interestRate;

    CreditInfo(double remainingDebt, int duration, double interestRate) {
        this.remainingDebt = remainingDebt;
        this.duration = duration;
        this.interestRate = interestRate;
    }
}

    // Ana Menüyü Göster
    private void returnToMainMenu() {
        // Ana menüyü görüntüleyen bir yeni ekran açılacak
        dispose(); // Mevcut pencereyi kapat
        new Banka_App(userTC); // Ana menü penceresini aç
    }

}
