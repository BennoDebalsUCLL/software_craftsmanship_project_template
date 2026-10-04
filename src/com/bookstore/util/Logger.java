package com.bookstore.util;

public class Logger {

    public static boolean DEBUG = false;

    public static Logger INSTANCE = new Logger();

    private int messageCount = 0;

    public static Logger getInstance() {
        return INSTANCE;
    }

    public void log(String message) {
        messageCount++;
        System.out.println("[LOG] " + message);
    }

    public void debug(String message) {
        if (DEBUG) {
            System.out.println("[DEBUG] " + message);
        }
    }

    public void error(String message) {
        System.out.println("[ERROR] " + message);
    }

    public int getMessageCount() {
        return messageCount;
    }
}
