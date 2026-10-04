package com.bookstore.util;

public class Config {

    public static double VAT_BE = 0.21;
    public static double VAT_NL = 0.21;
    public static double VAT_DEFAULT = 0.21;

    public static double STANDARD_SHIPPING = 4.99;
    public static double EXPRESS_SHIPPING = 12.50;

    public static String SHOP_NAME = "The Bookstore";
    public static String SUPPORT_EMAIL = "support@bookstore.example";

    // these flags are read by the admin panel
    public static boolean FEATURE_LOYALTY_POINTS = true;
    public static boolean FEATURE_GIFT_WRAP = true;
    public static boolean FEATURE_RECOMMENDATIONS = false;
    public static boolean FEATURE_MULTI_CURRENCY = false;
    public static boolean FEATURE_B2B_INVOICING = false;

    public static int MAX_LINES_PER_ORDER = 20;
}
