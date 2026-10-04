package com.bookstore.services;

public interface NotificationGateway {

    void sendEmail(String to, String subject, String body);

    void sendSms(String number, String body);

    void sendPushNotification(String deviceId, String body);

    void sendFax(String number, String body);

    void sendLetter(String address, String body);
}
