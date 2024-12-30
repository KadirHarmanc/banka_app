package com.mycompany.banka_app;

import javax.swing.*;
import java.awt.*;

public class Banka_App extends JFrame {

    private String userTC; // Kullanıcının TC kimlik numarası

    public Banka_App(String userTC) {
        this.userTC = userTC;

        setTitle("Banka Uygulaması");
        setSize(400, 500); // Yeni buton için pencere boyutunu biraz artırdık
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel ve düzen oluştur
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10)); // 6 buton olacak
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Butonlar
        JButton accountInfoButton = new JButton("Hesap Bilgileri");
        JButton billPaymentButton = new JButton("Fatura Ödemeleri");
        JButton currencyTransactionButton = new JButton("Döviz İşlemleri");
        JButton insuranceButton = new JButton("Sigorta İşlemleri");
        JButton balanceOperationsButton = new JButton("Bakiye İşlemleri");
        JButton creditOperationsButton = new JButton("Kredi İşlemleri"); // Yeni buton

        // Butonların olay dinleyicileri
        accountInfoButton.addActionListener(e -> {
            new AccountInfoFrame(userTC);
            dispose(); // Hesap bilgileri işlemi sonrası pencereyi kapat
        });

        billPaymentButton.addActionListener(e -> {
            new BillPaymentFrame(userTC);
            dispose(); // Fatura ödeme işlemi sonrası pencereyi kapat
        });

        currencyTransactionButton.addActionListener(e -> {
            new CurrencyTransactionFrame(userTC);
            dispose(); // Döviz işlemleri sonrası pencereyi kapat
        });

        insuranceButton.addActionListener(e -> {
            new InsuranceFrame(userTC);
            dispose(); // Sigorta işlemleri sonrası pencereyi kapat
        });

        balanceOperationsButton.addActionListener(e -> {
            new BalanceOperationsFrame(userTC);
            dispose(); // Bakiye işlemleri sonrası pencereyi kapat
        });

        // Yeni butonun olay dinleyicisi
        creditOperationsButton.addActionListener(e -> {
            new CreditOperationsFrame(userTC);
            dispose(); // Kredi işlemleri sonrası pencereyi kapat
        });

        // Butonları panele ekle
        panel.add(accountInfoButton);
        panel.add(billPaymentButton);
        panel.add(currencyTransactionButton);
        panel.add(insuranceButton);
        panel.add(balanceOperationsButton);
        panel.add(creditOperationsButton); // Yeni butonu ekle

        // Paneli çerçeveye ekle
        add(panel);

        setVisible(true); // Pencereyi görünür yap
    }
}
