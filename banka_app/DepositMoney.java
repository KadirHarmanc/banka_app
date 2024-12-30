/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.banka_app;

import javax.swing.*;

public class DepositMoney {
    public static void showDepositDialog(String userTC) {
        // Kullanıcıdan yatırılacak miktarı alma
        String inputAmount = JOptionPane.showInputDialog(null, "Yatırmak istediğiniz tutarı girin:");

        try {
            double amount = Double.parseDouble(inputAmount);

            if (amount <= 0) {
                JOptionPane.showMessageDialog(null, "Lütfen geçerli bir tutar girin.", "Hata", JOptionPane.ERROR_MESSAGE);
            } else {
                // Bakiye güncelleme işlemi
                UpdateBalance.updateBalance(userTC, amount, "Ekleme");
                JOptionPane.showMessageDialog(null, "Bakiye başarıyla güncellendi.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Lütfen geçerli bir sayısal değer girin.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}

