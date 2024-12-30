package com.mycompany.banka_app;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class CurrencyTransactionFrame extends JFrame {
    private String userTC;
    private JComboBox<String> currencyCombo;
    private JComboBox<String> transactionTypeCombo;
    private JTextField amountField, exchangeRateField, totalField;
    private JLabel balanceLabel, usdLabel, eurLabel, goldLabel;
    private JButton processButton, backButton;
    
    

public CurrencyTransactionFrame(String userTC) {

        this.userTC = userTC;

        setTitle("Döviz İşlemleri");
        setSize(500, 600);  // Daha geniş bir pencere
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());  // Daha esnek bir düzen
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Bileşenler arasına boşluk ekleyelim

        // Döviz türü seçimi
        JLabel currencyLabel = new JLabel("Döviz Türü:");
        currencyCombo = new JComboBox<>(new String[]{"USD", "EUR", "Altın"});
        currencyCombo.addActionListener(e -> updateSelectedCurrencyBalance());
        currencyCombo.setPreferredSize(new Dimension(150, 30));  // Daha büyük combo box

        // İşlem türü seçimi
        JLabel transactionTypeLabel = new JLabel("İşlem Türü:");
        transactionTypeCombo = new JComboBox<>(new String[]{"Alım", "Satım"});
        transactionTypeCombo.setPreferredSize(new Dimension(150, 30));  // Daha büyük combo box

        // GridBagLayout ile öğeleri düzenleyelim
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(currencyLabel, gbc);
        gbc.gridx = 1;
        panel.add(currencyCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(transactionTypeLabel, gbc);
        gbc.gridx = 1;
        panel.add(transactionTypeCombo, gbc);

        // Miktar ve Kur Fiyatı alanları
        JLabel amountLabel = new JLabel("Miktar:");
        amountField = new JTextField(10);
        amountField.setPreferredSize(new Dimension(200, 30));  // Daha büyük text field
        JLabel exchangeRateLabel = new JLabel("Kur Fiyatı:");
        exchangeRateField = new JTextField(10);
        exchangeRateField.setEditable(false);
        exchangeRateField.setPreferredSize(new Dimension(200, 30));  // Daha büyük text field

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(amountLabel, gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(exchangeRateLabel, gbc);
        gbc.gridx = 1;
        panel.add(exchangeRateField, gbc);

        // Toplam Tutar alanı
        JLabel totalLabel = new JLabel("Toplam Tutar:");
        totalField = new JTextField(10);
        totalField.setEditable(false);
        totalField.setPreferredSize(new Dimension(200, 30));  // Daha büyük text field

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(totalLabel, gbc);
        gbc.gridx = 1;
        panel.add(totalField, gbc);

        // Bakiye bilgisi
        JPanel balancePanel = new JPanel();
        balancePanel.setLayout(new BoxLayout(balancePanel, BoxLayout.Y_AXIS));
        balanceLabel = new JLabel("TL Bakiye: ");
        usdLabel = new JLabel("USD Bakiye: ");
        eurLabel = new JLabel("EUR Bakiye: ");
        goldLabel = new JLabel("Altın Bakiye: ");
        
        balancePanel.add(balanceLabel);
        balancePanel.add(usdLabel);
        balancePanel.add(eurLabel);
        balancePanel.add(goldLabel);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(balancePanel, gbc);

        // İşlem butonları
        processButton = new JButton("İşlemi Tamamla");
        processButton.setPreferredSize(new Dimension(200, 50));  // Daha büyük buton
        processButton.addActionListener(e -> processTransaction());

        backButton = new JButton("Ana Sayfaya Dön");
        backButton.setPreferredSize(new Dimension(200, 50));  // Daha büyük buton
        backButton.addActionListener(e -> {
            new Banka_App(userTC).setVisible(true);
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(processButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // Diğer bileşenler
        currencyCombo.addActionListener(e -> fetchExchangeRate());
        amountField.addActionListener(e -> calculateTotal());
        transactionTypeCombo.addActionListener(e -> calculateTotal());

        add(panel);
        
        // Bilgileri al ve güncelle
        fetchExchangeRate();
        fetchUserBalance();
        updateSelectedCurrencyBalance(); // İlk seçilen döviz için bakiye güncellemesi

        setVisible(true);
    
    }
   
private void fetchUserBalance() {
    try (Connection conn = DatabaseConnection.connect()) {
        // Kullanıcının kullanici_id'sini almak için sorgu
        String userIdQuery = "SELECT id, para FROM kullanicilar WHERE tcKimlikNo = ?";
        PreparedStatement userIdStatement = conn.prepareStatement(userIdQuery);
        userIdStatement.setString(1, userTC);
        ResultSet userIdResultSet = userIdStatement.executeQuery();

        int kullaniciId = -1;
        double tlBalance = 0.0;

        if (userIdResultSet.next()) {
            kullaniciId = userIdResultSet.getInt("id");
            tlBalance = userIdResultSet.getDouble("para");
        } else {
            JOptionPane.showMessageDialog(this, "Kullanıcı bilgileri alınamadı!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kullanıcının döviz bakiyelerini almak için sorgu
        String currencyQuery = "SELECT doviz_turu, miktar FROM currency_balances WHERE kullanici_id = ?";
        PreparedStatement currencyStatement = conn.prepareStatement(currencyQuery);
        currencyStatement.setInt(1, kullaniciId);
        ResultSet currencyResultSet = currencyStatement.executeQuery();

        double usdBalance = 0.0;
        double eurBalance = 0.0;
        double goldBalance = 0.0;

        // Sonuçları döngüyle işleme
        while (currencyResultSet.next()) {
            String dovizTuru = currencyResultSet.getString("doviz_turu");
            double miktar = currencyResultSet.getDouble("miktar");

            switch (dovizTuru) {
                case "usd":
                    usdBalance = miktar;
                    break;
                case "EUR":
                    eurBalance = miktar;
                    break;
                case "Altın":
                    goldBalance = miktar;
                    break;
                default:
                    System.out.println("Bilinmeyen döviz türü: " + dovizTuru);
                    break;
            }
        }

        // Etiketleri güncelle
        balanceLabel.setText("TL Bakiye: " + String.format("%.2f", tlBalance));
        usdLabel.setText("USD Bakiye: " + String.format("%.2f", usdBalance));
        eurLabel.setText("EUR Bakiye: " + String.format("%.2f", eurBalance));
        goldLabel.setText("Altın Bakiye: " + String.format("%.2f", goldBalance));

        // Seçili döviz bakiyesini güncelle
        updateSelectedCurrencyBalance();
    } catch (SQLException e) {
    JOptionPane.showMessageDialog(null, "Bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
    JOptionPane.showMessageDialog(null, "Bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        JOptionPane.showMessageDialog(this, "Beklenmeyen bir hata oluştu!", "Hata", JOptionPane.ERROR_MESSAGE);
    }
}




private void fetchExchangeRate() {
    String selectedCurrency = (String) currencyCombo.getSelectedItem();
    double rate = 0.0;

    try {
        if ("USD".equals(selectedCurrency)) {
            rate = getUsdToTryRate();
        } else if ("EUR".equals(selectedCurrency)) {
            rate = getEurToTryRate();
        } else if ("Altın".equals(selectedCurrency)) {
            rate = getGoldPrice();
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Kur bilgisi alınırken hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }

    exchangeRateField.setText(String.format("%.2f", rate));
    calculateTotal();
}

private double getUsdToTryRate() throws Exception {
    // Fixer.io API URL'si
    String url = "http://data.fixer.io/api/latest?access_key=fd6d72c8e20b53138b33e6d20e6a5830&symbols=USD,TRY";

    // URL bağlantısını açıyoruz
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("GET");

    // API'den gelen yanıtı okuyoruz
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        response.append(line);
    }
    reader.close();

    // JSON yanıtını parse ediyoruz
    JSONObject jsonResponse = new JSONObject(response.toString());

    // Yanıttan "rates" objesini alıyoruz
    JSONObject rates = jsonResponse.getJSONObject("rates");
    double usdToEur = rates.getDouble("USD"); // USD/EUR
    double tryToEur = rates.getDouble("TRY"); // TRY/EUR

    // USD/TRY oranını hesaplıyoruz
    return tryToEur / usdToEur;
}

private double getEurToTryRate() throws Exception {
    // Fixer.io API URL'si - EUR/TL kuru
    String url = "http://data.fixer.io/api/latest?access_key=fd6d72c8e20b53138b33e6d20e6a5830&symbols=EUR,TRY";
    
    // URL bağlantısını açıyoruz
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("GET");

    // API'den gelen yanıtı okuyoruz
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        response.append(line);
    }
    reader.close();

    // JSON yanıtını parse ediyoruz
    JSONObject jsonResponse = new JSONObject(response.toString());

    // Yanıttan "rates" objesini alıyoruz
    JSONObject rates = jsonResponse.getJSONObject("rates");
    return rates.getDouble("TRY");  // EUR/TL kuru
}

private double getGoldPrice() throws Exception {
    String apiUrl = "https://www.goldapi.io/api/XAU/USD";
    URL url = new URL(apiUrl);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("x-access-token", "goldapi-1ivaf7sm5akkntf-io");

    // API bağlantısını kontrol et
    int responseCode = conn.getResponseCode();
    if (responseCode != 200) {
        throw new Exception("Altın fiyatı isteği başarısız oldu. HTTP Kod: " + responseCode);
    }

    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    StringBuilder response = new StringBuilder();
    String line;

    while ((line = in.readLine()) != null) {
        response.append(line);
    }
    in.close();

    // JSON verisini işle
    JSONObject json = new JSONObject(response.toString());
    return json.getDouble("price");
}

private void calculateTotal() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            double rate = Double.parseDouble(exchangeRateField.getText());
            double total = amount * rate;
            totalField.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            totalField.setText("0.00");
        }
    }
   
private void updateSelectedCurrencyBalance() {
        String selectedCurrency = (String) currencyCombo.getSelectedItem();
        double balance = getCurrencyBalance(selectedCurrency.toLowerCase());
        
        usdLabel.setVisible(false);
        eurLabel.setVisible(false);
        goldLabel.setVisible(false);

        switch (selectedCurrency) {
            case "USD":
                usdLabel.setText("USD Bakiye: " + String.format("%.2f", balance));
                usdLabel.setVisible(true);
                break;
            case "EUR":
                eurLabel.setText("EUR Bakiye: " + String.format("%.2f", balance));
                eurLabel.setVisible(true);
                break;
            case "Altın":
                goldLabel.setText("Altın Bakiye: " + String.format("%.2f", balance));
                goldLabel.setVisible(true);
                break;
        }
    }

private void processTransaction() {
    try (Connection conn = DatabaseConnection.connect()) {
        conn.setAutoCommit(false); // Transaction başlat

        // Kullanıcının ID'sini ve TL bakiyesini al
        String userIdQuery = "SELECT id, para FROM kullanicilar WHERE tcKimlikNo = ?";
        PreparedStatement userIdStatement = conn.prepareStatement(userIdQuery);
        userIdStatement.setString(1, userTC);
        ResultSet userIdResultSet = userIdStatement.executeQuery();

        int kullaniciId = -1;
        double tlBalance = 0.0;

        if (userIdResultSet.next()) {
            kullaniciId = userIdResultSet.getInt("id");
            tlBalance = userIdResultSet.getDouble("para");
        } else {
            JOptionPane.showMessageDialog(this, "Kullanıcı bilgileri alınamadı!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Seçilen döviz türü ve miktar
        String selectedCurrency = (String) currencyCombo.getSelectedItem();
        double amount = Double.parseDouble(amountField.getText());
        double exchangeRate = Double.parseDouble(exchangeRateField.getText());
        String transactionType = (String) transactionTypeCombo.getSelectedItem();

        // Döviz bakiyesi sorgulama
        String currencyQuery = "SELECT miktar FROM currency_balances WHERE kullanici_id = ? AND doviz_turu = ?";
        PreparedStatement currencyStatement = conn.prepareStatement(currencyQuery);
        currencyStatement.setInt(1, kullaniciId);
        currencyStatement.setString(2, selectedCurrency);
        ResultSet currencyResultSet = currencyStatement.executeQuery();

        double currencyBalance = 0.0;
        if (currencyResultSet.next()) {
            currencyBalance = currencyResultSet.getDouble("miktar");
        }

        // İşlem türüne göre bakiyeleri güncelle
        double totalAmount = amount * exchangeRate;

        if (transactionType.equals("Alım")) {
            if (tlBalance >= totalAmount) {
                // TL bakiyesini azalt
                UpdateBalance.updateBalance(userTC, -totalAmount, "Çıkarma");

                // Döviz bakiyesini artır
                double newCurrencyBalance = currencyBalance + amount;
                String updateCurrencyQuery = "UPDATE currency_balances SET miktar = ? WHERE kullanici_id = ? AND doviz_turu = ?";
                PreparedStatement updateCurrencyStatement = conn.prepareStatement(updateCurrencyQuery);
                updateCurrencyStatement.setDouble(1, newCurrencyBalance);
                updateCurrencyStatement.setInt(2, kullaniciId);
                updateCurrencyStatement.setString(3, selectedCurrency);
                updateCurrencyStatement.executeUpdate();

                // İşlem kaydını tut
                recordTransaction(selectedCurrency, "alım", amount, exchangeRate, totalAmount);

                JOptionPane.showMessageDialog(this, "Döviz alım işlemi başarılı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Yetersiz TL bakiyesi!", "Hata", JOptionPane.ERROR_MESSAGE);
                conn.rollback(); // İşlemi geri al
                return;
            }
        } else if (transactionType.equals("Satım")) {
            if (currencyBalance >= amount) {
                // Döviz bakiyesini azalt
                double newCurrencyBalance = currencyBalance - amount;
                String updateCurrencyQuery = "UPDATE currency_balances SET miktar = ? WHERE kullanici_id = ? AND doviz_turu = ?";
                PreparedStatement updateCurrencyStatement = conn.prepareStatement(updateCurrencyQuery);
                updateCurrencyStatement.setDouble(1, newCurrencyBalance);
                updateCurrencyStatement.setInt(2, kullaniciId);
                updateCurrencyStatement.setString(3, selectedCurrency);
                updateCurrencyStatement.executeUpdate();

                // TL bakiyesini artır
                UpdateBalance.updateBalance(userTC, totalAmount, "Ekleme");

                // İşlem kaydını tut
                recordTransaction(selectedCurrency, "satım", amount, exchangeRate, totalAmount);

                JOptionPane.showMessageDialog(this, "Döviz satım işlemi başarılı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Yetersiz döviz bakiyesi!", "Hata", JOptionPane.ERROR_MESSAGE);
                conn.rollback(); // İşlemi geri al
                return;
            }
        }

        conn.commit(); // İşlemi tamamla
        fetchUserBalance(); // Bakiyeleri güncelle
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Beklenmeyen bir hata oluştu!", "Hata", JOptionPane.ERROR_MESSAGE);
    }
}


private String getUserID(String userTC) {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT id FROM kullanicilar WHERE tcKimlikNo = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userTC);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


private void recordTransaction(String currencyType, String transactionType, double amount, double rate, double total) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = DatabaseConnection.connect();
        String sql = "INSERT INTO currency_transactions (kullanici_id, islem_tarihi, doviz_turu, islem_turu, miktar, kur_fiyat, toplam) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, getUserID(userTC));  // Pass userTC to getUserID
        stmt.setString(2, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        stmt.setString(3, currencyType);
        stmt.setString(4, transactionType);
        stmt.setDouble(5, amount);
        stmt.setDouble(6, rate);
        stmt.setDouble(7, total);
        
        stmt.executeUpdate();
    } catch (SQLException e) {
        System.err.println("SQL Error: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


private double getCurrencyBalance(String currencyType) {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT miktar FROM currency_balances WHERE kullanici_id = ? AND doviz_turu = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, getUserID(userTC));  // Pass userTC to getUserID
            stmt.setString(2, currencyType);  // Currency type (USD, EUR, Gold)
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("miktar");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


private void updateBalance(String currencyType, double amount) {
    try (Connection conn = DatabaseConnection.connect()) {
        String checkSql = "SELECT * FROM currency_balances WHERE kullanici_id = ? AND doviz_turu = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, getUserID(userTC));  // Pass userTC to getUserID
            checkStmt.setString(2, currencyType);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Eğer döviz türü zaten varsa, miktarı güncelle
                    String updateSql = "UPDATE currency_balances SET miktar = miktar + ? WHERE kullanici_id = ? AND doviz_turu = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setDouble(1, amount);
                        updateStmt.setString(2, getUserID(userTC));
                        updateStmt.setString(3, currencyType);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Eğer döviz türü yoksa, yeni bir satır ekle
                    String insertSql = "INSERT INTO currency_balances (kullanici_id, doviz_turu, miktar) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, getUserID(userTC));
                        insertStmt.setString(2, currencyType);
                        insertStmt.setDouble(3, amount);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


}
