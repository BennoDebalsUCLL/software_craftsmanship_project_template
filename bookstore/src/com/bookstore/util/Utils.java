package com.bookstore.util;

public class Utils {

    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static String formatMoney(double value) {
        return String.format("%.2f", value) + " EUR";
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    public static boolean isValidIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }
        String cleaned = isbn.replace("-", "");
        return cleaned.length() == 13;
    }

    public static double vatFor(String country) {
        if (country == null) {
            return 0.21;
        }
        if (country.equals("BE")) {
            return 0.21;
        } else if (country.equals("NL")) {
            return 0.21;
        } else if (country.equals("LU")) {
            return 0.17;
        } else if (country.equals("FR")) {
            return 0.20;
        } else if (country.equals("DE")) {
            return 0.19;
        } else if (country.equals("US")) {
            return 0.07;
        }
        return 0.21;
    }

    public static String pad(String text, int length) {
        String result = text;
        while (result.length() < length) {
            result = result + " ";
        }
        return result;
    }

    public static String today() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }
}
