package com.mycompany.banka_app;
   

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;

public class RegisterFrame extends JFrame {

    private JLabel adLabel, soyadLabel, tcKimlikNoLabel, telefonLabel, emailLabel, sifreLabel;
    private JTextField adField, soyadField, tcKimlikNoField, telefonField, emailField;
    private JPasswordField sifreField;
    private JButton registerButton, goToLoginButton;

    public RegisterFrame() {
        // JFrame özelliklerini ayarla
        setTitle("Kullanıcı Kayıt Formu");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Ekranın ortasında yerleştir

        // Paneli ekleyerek düzenleme
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 2, 10, 10)); // 8 satır, 2 sütun düzeni
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Paneli kenarlardan boşluk bırakacak şekilde ayarla
        
        // Ad
        adLabel = new JLabel("Ad: ");
        adField = new JTextField();
        adField.setPreferredSize(new Dimension(200, 45)); // Ad input box boyutu

        // Soyad
        soyadLabel = new JLabel("Soyad: ");
        soyadField = new JTextField();
        soyadField.setPreferredSize(new Dimension(200, 45)); // Soyad input box boyutu


        // T.C. Kimlik No
        tcKimlikNoLabel = new JLabel("T.C. Kimlik No: ");
        tcKimlikNoField = new JTextField();
        tcKimlikNoField.setPreferredSize(new Dimension(200, 45)); // TC Kimlik No input box boyutu


        // Telefon Numarası
        telefonLabel = new JLabel("Telefon: ");
        telefonField = new JTextField();
        telefonField.setPreferredSize(new Dimension(200, 45)); // Telefon input box boyutu


        // E-mail
        emailLabel = new JLabel("E-mail: ");
        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 45)); // E-mail input box boyutu


        // Şifre
        sifreLabel = new JLabel("Şifre: ");
        sifreField = new JPasswordField();
        sifreField.setPreferredSize(new Dimension(200, 45)); // Şifre input box boyutu


        // Kayıt Ol butonu
        registerButton = new JButton("Kayıt Ol");
        registerButton.setPreferredSize(new Dimension(150,40));
        // Giriş Yap butonu
        goToLoginButton = new JButton("Zaten hesabınız var mı? Giriş Yap");
        goToLoginButton.setPreferredSize(new Dimension(150,40));



        // Kayıt Ol butonuna tıklandığında yapılacak işlem
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        // Giriş Yap butonuna tıklandığında yapılacak işlem
        goToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // RegisterFrame'i kapat
                new LoginFrame(); // LoginFrame'i aç
            }
        });

        // Frame'e paneli ekle
        add(panel, BorderLayout.CENTER);

        // Panel üzerine bileşenleri ekle
        panel.add(adLabel);
        panel.add(adField);

        panel.add(soyadLabel);
        panel.add(soyadField);

        panel.add(tcKimlikNoLabel);
        panel.add(tcKimlikNoField);

        panel.add(telefonLabel);
        panel.add(telefonField);

        panel.add(emailLabel);
        panel.add(emailField);

        panel.add(sifreLabel);
        panel.add(sifreField);

        panel.add(registerButton);
        panel.add(goToLoginButton);

           // Yeni bir panel oluşturup butonları ekleyelim
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Butonları ortalamak için

    buttonPanel.add(registerButton);
    buttonPanel.add(goToLoginButton);

    // Butonları frame'in alt kısmına ekle
    add(buttonPanel, BorderLayout.SOUTH);

    setVisible(true);
    }

private void registerUser() {
    // Kullanıcıdan alınan veriler
    String ad = adField.getText();
    String soyad = soyadField.getText();
    String tcKimlikNo = tcKimlikNoField.getText();
    String telefon = telefonField.getText();
    String email = emailField.getText();
    String sifre = new String(sifreField.getPassword());

    // Kontroller
    if (ad.isEmpty() || soyad.isEmpty() || tcKimlikNo.isEmpty() || telefon.isEmpty() || email.isEmpty() || sifre.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // T.C. Kimlik No kontrolü
    if (tcKimlikNo.length() != 11) {
        JOptionPane.showMessageDialog(this, "T.C. Kimlik No 11 haneli olmalıdır.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Telefon numarası kontrolü (sadece sayılar ve 10 haneli olmalı)
    if (telefon.length() != 10 || !telefon.matches("[0-9]+")) {
        JOptionPane.showMessageDialog(this, "Geçerli bir telefon numarası girin.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // E-mail kontrolü (@ karakteri var mı?)
    if (!email.contains("@")) {
        JOptionPane.showMessageDialog(this, "Geçerli bir e-mail adresi girin.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Şifre kontrolü (en az 6 haneli olmalı)
    if (sifre.length() < 6) {
        JOptionPane.showMessageDialog(this, "Şifre en az 6 haneli olmalıdır.", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Veritabanı işlemleri
    String iban = generateIBAN();  // Kullanıcıya göstermediğimiz IBAN
    saveUserToDatabase(ad, soyad, tcKimlikNo, telefon, email, sifre, iban);
}

    private String generateIBAN() {
        // Kısa, 6 haneli rastgele IBAN numarası üret
        Random rand = new Random();
        String iban = "TR" + (100000 + rand.nextInt(900000)); // 6 haneli IBAN formatı
        return iban;
    }

    private void saveUserToDatabase(String ad, String soyad, String tcKimlikNo, String telefon, String email, String sifre, String iban) {
        // Veritabanı bağlantısını sağla ve kayıt ekle
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "INSERT INTO kullanicilar (ad, soyad, tcKimlikNo, sifre, telefon, email, iban) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ad); // ad
            stmt.setString(2, soyad); // soyad
            stmt.setString(3, tcKimlikNo);
            stmt.setString(4, sifre);
            stmt.setString(5, telefon);
            stmt.setString(6, email);
            stmt.setString(7, iban); // IBAN veritabanına ekleniyor
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Kullanıcı kaydedildi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Veritabanına kaydedilirken hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new RegisterFrame();
    }
}
