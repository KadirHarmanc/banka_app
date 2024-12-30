package com.mycompany.banka_app;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public class EmailSender {
    private static final Logger LOGGER = Logger.getLogger(EmailSender.class.getName());
    private static final String FROM_EMAIL = "gekkowearr@gmail.com"; // Gmail adresinizi buraya yazın
    private static final String PASSWORD = "bqsm asiq xadh hmto"; // Gmail uygulama şifrenizi buraya yazın
    private static String lastGeneratedCode = null;

    // E-posta gönderme işlemi
    public static void sendEmail(String toEmail, String subject, String body) throws MessagingException {
        LOGGER.info("E-posta gönderme işlemi başlatıldı: " + toEmail);
        
        Properties props = new Properties();
        // Gmail SMTP Ayarları
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            
            // HTML formatında e-posta içeriği
            message.setContent(
                "<html><body>" +
                "<div style='font-family: Arial, sans-serif;'>" +
                "<h3 style='color: #2E7D32;'>Bankacılık Sistemi</h3>" +
                "<p>" + body + "</p>" +
                "</div>" +
                "</body></html>",
                "text/html; charset=UTF-8"
            );

            Transport.send(message);
            LOGGER.info("E-posta başarıyla gönderildi: " + toEmail);

        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "E-posta gönderiminde hata oluştu", e);
            throw new MessagingException("E-posta gönderimi başarısız: " + e.getMessage());
        }
    }

    // Rastgele doğrulama kodu oluşturma
    public static String generateVerificationCode() {
        lastGeneratedCode = String.format("%06d", (int)(Math.random() * 1000000));
        LOGGER.info("Yeni doğrulama kodu oluşturuldu: " + lastGeneratedCode);
        return lastGeneratedCode;
    }

    // Doğrulama kodunu kontrol etme
    public static boolean verifyCode(String enteredCode) {
        boolean isValid = lastGeneratedCode != null && lastGeneratedCode.equals(enteredCode);
        LOGGER.info("Kod doğrulama sonucu: " + isValid);
        return isValid;
    }

    // Doğrulama kodu gönderme
    public static void sendVerificationCode(String email) throws MessagingException {
        LOGGER.info("Doğrulama kodu gönderiliyor: " + email);
        
        // Daha önce oluşturulmuş bir doğrulama kodu var mı?
        if (lastGeneratedCode == null) {
            lastGeneratedCode = generateVerificationCode();
        }

        String subject = "Bankacılık Sistemi - Doğrulama Kodu";
        String body = String.format(
            "Giriş yapmak için doğrulama kodunuz: <b>%s</b><br><br>" +
            "<i>Bu kodu kimseyle paylaşmayınız.</i><br><br>" +
            "Saygılarımızla,<br>" +
            "<b>Bankacılık Sistemi</b>", 
            lastGeneratedCode
        );

        try {
            sendEmail(email, subject, body);
            LOGGER.info("Doğrulama kodu başarıyla gönderildi: " + email);
        } catch (MessagingException e) {
            LOGGER.severe("Doğrulama kodu gönderilemedi: " + e.getMessage());
            throw e;
        }
    }
}
