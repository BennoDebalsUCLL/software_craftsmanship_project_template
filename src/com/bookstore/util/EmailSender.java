package com.bookstore.util;

import com.bookstore.services.NotificationGateway;

public class EmailSender implements NotificationGateway {

    private String smtpHost = "smtp.bookstore.internal";
    private int smtpPort = 25;
    private String smtpUser = "noreply@bookstore.example";
    private String smtpPassword = "Sm7p!2019";

    public void sendEmail(String to, String subject, String body) {
        System.out.println("[MAIL] to=" + to + " subject=" + subject);
        System.out.println("       " + body.replace("\n", "\n       "));
    }

    public void sendSms(String number, String body) {
        throw new UnsupportedOperationException("sms not supported");
    }

    public void sendPushNotification(String deviceId, String body) {
        throw new UnsupportedOperationException("push not supported");
    }

    public void sendFax(String number, String body) {
        throw new UnsupportedOperationException("fax not supported");
    }

    public void sendLetter(String address, String body) {
        throw new UnsupportedOperationException("letter not supported");
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }
}
