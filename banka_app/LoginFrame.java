package com.mycompany.banka_app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.mail.MessagingException;

public class LoginFrame extends JFrame {

    private JLabel tcLabel, sifreLabel;
    private JTextField tcField;
    private JPasswordField sifreField;
    private JButton loginButton, registerButton;

    public LoginFrame() {
        // JFrame özelliklerini ayarla
        setTitle("Kullanıcı Giriş Formu");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Ekranın ortasında yerleştir
        setLayout(new BorderLayout());

        // İç paneli oluştur
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Alanlar arasında boşluk
        gbc.fill = GridBagConstraints.HORIZONTAL; // Yatayda alanı doldur

        // T.C. Kimlik No
        tcLabel = new JLabel("T.C. Kimlik No: ");
        tcField = new JTextField(15);
        tcField.setPreferredSize(new Dimension(200, 40));

        // Şifre
        sifreLabel = new JLabel("Şifre: ");
        sifreField = new JPasswordField(15);
        sifreField.setPreferredSize(new Dimension(200, 40));

        // GridBagLayout ile bileşenleri yerleştir
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(tcLabel, gbc);

        gbc.gridx = 1;
        panel.add(tcField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(sifreLabel, gbc);

        gbc.gridx = 1;
        panel.add(sifreField, gbc);

        // Ortada olacak şekilde butonlar için yeni panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Giriş Yap butonu
        loginButton = new JButton("Giriş Yap");
        loginButton.setPreferredSize(new Dimension(150, 40));

        // Kayıt Ol butonu
        registerButton = new JButton("Kayıt Ol");
        registerButton.setPreferredSize(new Dimension(150, 40));

        // Giriş Yap butonuna tıklama işlemi
        loginButton.addActionListener(e -> loginUser());

        // Kayıt Ol butonuna tıklama işlemi
        registerButton.addActionListener(e -> {
            new RegisterFrame(); // Kayıt ekranına yönlendirme
            dispose(); // LoginFrame'i kapat
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Frame'e paneli ekle
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

private void loginUser() {
    String tc = tcField.getText();
    String sifre = new String(sifreField.getPassword());

    if (tc.isEmpty() || sifre.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = DatabaseConnection.connect()) {
        String sql = "SELECT * FROM kullanicilar WHERE tcKimlikNo = ? AND sifre = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, tc);
        stmt.setString(2, sifre);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            // E-posta gönderimi ve doğrulama kodu kısmını yorum satırına alıyoruz
            
            String email = rs.getString("email");

            try {
                // E-posta gönderimi sırasında doğrulama kodu oluşturulacak
                EmailSender.sendVerificationCode(email);  

                String userCode = JOptionPane.showInputDialog(this, "E-posta adresinize gelen doğrulama kodunu girin:", "Doğrulama Kodu", JOptionPane.QUESTION_MESSAGE);

                if (userCode != null && EmailSender.verifyCode(userCode)) {
                    JOptionPane.showMessageDialog(this, "Giriş başarılı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    new Banka_App(tc); // Banka_App'e yönlendirme
                    dispose(); // Login ekranını kapat
                } else {
                    JOptionPane.showMessageDialog(this, "Doğrulama kodu hatalı. Giriş başarısız.", "Hata", JOptionPane.ERROR_MESSAGE);
                }

            } catch (MessagingException e) {
                JOptionPane.showMessageDialog(this, "E-posta gönderilemedi. Lütfen tekrar deneyin.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
            
            
            // E-posta doğrulama kısmını geçerek doğrudan başarılı giriş işlemi yapılacak
            JOptionPane.showMessageDialog(this, "Giriş başarılı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            new Banka_App(tc); // Banka_App'e yönlendirme
            dispose(); // Login ekranını kapat
        } else {
            JOptionPane.showMessageDialog(this, "Geçersiz TC Kimlik No veya Şifre.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
}



    public static void main(String[] args) {
        new LoginFrame();
    }
}
