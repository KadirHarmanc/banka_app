package com.mycompany.banka_app;

import javax.swing.*;
import java.awt.*;

public class BalanceOperationsFrame extends JFrame {
    private String userTC;

    public BalanceOperationsFrame(String userTC) {
        this.userTC = userTC;

        setTitle("Bakiye İşlemleri");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel ve düzen oluştur
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10)); // 5 buton olacak
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Butonlar
        JButton depositButton = new JButton("Para Yatırma");
        JButton withdrawButton = new JButton("Para Çekme");
        JButton transferButton = new JButton("Para Transferi");
        JButton backButton = new JButton("Ana Sayfaya Dön");

        // Butonların olay dinleyicileri
        depositButton.addActionListener(e -> {
            DepositMoney.showDepositDialog(userTC);
            dispose(); // Para yatırma işlemi sonrası pencereyi kapat
        });

        withdrawButton.addActionListener(e -> {
            WithdrawMoney.showWithdrawDialog(userTC);
            dispose(); // Para çekme işlemi sonrası pencereyi kapat
        });

        transferButton.addActionListener(e -> {
            TransferMoney.showTransferDialog(userTC);
            dispose(); // Para transferi işlemi sonrası pencereyi kapat
        });

        // Ana sayfaya dön butonunun olay dinleyicisi
        backButton.addActionListener(e -> {
            new Banka_App(userTC).setVisible(true); // Ana sayfaya dön
            dispose(); // Bakiye işlemleri sayfasını kapat
        });

        // Butonları panele ekle
        panel.add(depositButton);
        panel.add(withdrawButton);
        panel.add(transferButton);
        panel.add(backButton); // Ana sayfaya dön butonunu ekle

        // Paneli çerçeveye ekle
        add(panel);

        setVisible(true);
    }
}
