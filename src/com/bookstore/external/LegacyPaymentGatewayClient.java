package com.bookstore.external;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for the payment provider. Do not change the field names, they come
 * straight from their API documentation (v1.3, 2018).
 */
public class LegacyPaymentGatewayClient {

    private static int counter = 1000;

    public Map<String, String> doPayment(String cust_ref, String card_no, int amt_cents, String cur) {
        Map<String, String> response = new HashMap<String, String>();
        counter++;
        if (card_no == null || card_no.length() < 12) {
            response.put("txn_stat", "NOK");
            response.put("err_cd", "E42_INVALID_CARD");
            response.put("txn_id", "");
            response.put("amt_cents", String.valueOf(amt_cents));
            return response;
        }
        if (amt_cents <= 0) {
            response.put("txn_stat", "NOK");
            response.put("err_cd", "E07_BAD_AMOUNT");
            response.put("txn_id", "");
            response.put("amt_cents", String.valueOf(amt_cents));
            return response;
        }
        if (amt_cents > 500000) {
            response.put("txn_stat", "NOK");
            response.put("err_cd", "E13_LIMIT_EXCEEDED");
            response.put("txn_id", "");
            response.put("amt_cents", String.valueOf(amt_cents));
            return response;
        }
        response.put("txn_stat", "OK");
        response.put("err_cd", "");
        response.put("txn_id", "TXN" + counter);
        response.put("amt_cents", String.valueOf(amt_cents));
        response.put("cur", cur);
        return response;
    }

    public Map<String, String> doRefund(String txn_id, int amt_cents) {
        Map<String, String> response = new HashMap<String, String>();
        response.put("txn_stat", "OK");
        response.put("txn_id", txn_id + "-R");
        response.put("amt_cents", String.valueOf(amt_cents));
        return response;
    }
}
