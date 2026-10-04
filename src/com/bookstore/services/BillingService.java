package com.bookstore.services;

import com.bookstore.dao.InvoiceDao;
import com.bookstore.dao.OrderDao;
import com.bookstore.external.LegacyPaymentGatewayClient;
import com.bookstore.models.Invoice;
import com.bookstore.models.Order;
import com.bookstore.util.Logger;
import com.bookstore.util.Utils;
import java.util.Date;
import java.util.Map;

public class BillingService {

    private InvoiceDao invoiceDao = new InvoiceDao();
    private OrderDao orderDao = new OrderDao();
    private Logger logger = Logger.getInstance();

    public Invoice createInvoice(Order order) {
        double amountExclVat = 0.0;
        for (int i = 0; i < order.getLines().size(); i++) {
            amountExclVat = amountExclVat + order.getLines().get(i).getLineTotal();
        }
        amountExclVat = amountExclVat - order.getDiscountAmount();

        double vat;
        if (order.getCustomer().getCountry().equals("BE")) {
            vat = 0.21;
        } else if (order.getCustomer().getCountry().equals("NL")) {
            vat = 0.21;
        } else if (order.getCustomer().getCountry().equals("LU")) {
            vat = 0.17;
        } else if (order.getCustomer().getCountry().equals("FR")) {
            vat = 0.20;
        } else if (order.getCustomer().getCountry().equals("DE")) {
            vat = 0.19;
        } else if (order.getCustomer().getCountry().equals("US")) {
            vat = 0.07;
        } else {
            vat = 0.21;
        }

        Invoice invoice = new Invoice();
        invoice.setId(invoiceDao.nextId());
        invoice.setOrderId(order.getId());
        invoice.setClientEmail(order.getCustomer().getEmail());
        invoice.setClientName(order.getCustomer().getName());
        invoice.setAmountExclVat(Utils.round2(amountExclVat));
        invoice.setVatAmount(Utils.round2(amountExclVat * vat));
        double inclVat = amountExclVat + (amountExclVat * vat) + order.getShippingCost();
        if (order.isGiftWrap()) {
            inclVat = inclVat + 3.5;
        }
        invoice.setAmountInclVat(Utils.round2(inclVat));
        if (order.getStatus().equals("P")) {
            invoice.setStatus("PAID");
        } else {
            invoice.setStatus("OPEN");
        }
        invoice.setIssuedAt(new Date());
        invoiceDao.save(invoice);
        logger.log("invoice " + invoice.getId() + " created for order " + order.getId());
        return invoice;
    }

    public boolean payInvoice(String invoiceId, String cardNumber) {
        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null) {
            throw new RuntimeException("unknown invoice " + invoiceId);
        }
        if (invoice.getStatus().equals("PAID")) {
            logger.error("invoice " + invoiceId + " is already paid");
            return false;
        }
        LegacyPaymentGatewayClient gateway = new LegacyPaymentGatewayClient();
        Map<String, String> response = gateway.doPayment(invoice.getClientEmail(), cardNumber,
                (int) (invoice.getAmountInclVat() * 100), "EUR");
        if (response.get("txn_stat").equals("OK")) {
            invoice.setStatus("PAID");
            invoiceDao.save(invoice);
            Order order = orderDao.findById(invoice.getOrderId());
            if (order != null && order.getStatus().equals("NEW")) {
                order.setStatus("P");
                order.setPaidAt(new Date());
                order.setPaymentTransactionId(response.get("txn_id"));
                orderDao.save(order);
            }
            logger.log("invoice " + invoiceId + " paid, txn " + response.get("txn_id"));
            return true;
        }
        logger.error("payment of invoice " + invoiceId + " failed: " + response.get("err_cd"));
        return false;
    }

    public void refund(String invoiceId, String transactionId) {
        Invoice invoice = invoiceDao.findById(invoiceId);
        LegacyPaymentGatewayClient gateway = new LegacyPaymentGatewayClient();
        Map<String, String> response = gateway.doRefund(transactionId, (int) (invoice.getAmountInclVat() * 100));
        if (response.get("txn_stat").equals("OK")) {
            invoice.setStatus("REFUNDED");
            invoiceDao.save(invoice);
            logger.log("invoice " + invoiceId + " refunded");
        }
    }

    public String printInvoice(String orderId) {
        Invoice invoice = invoiceDao.findByOrderId(orderId);
        String text = "";
        text = text + "INVOICE " + invoice.getId() + " (" + invoice.getStatus() + ")\n";
        text = text + "order    : " + invoice.getOrderId() + "\n";
        text = text + "client   : " + invoice.getClientName() + " <" + invoice.getClientEmail() + ">\n";
        text = text + "excl vat : " + Utils.formatMoney(invoice.getAmountExclVat()) + "\n";
        text = text + "vat      : " + Utils.formatMoney(invoice.getVatAmount()) + "\n";
        text = text + "total    : " + Utils.formatMoney(invoice.getAmountInclVat());
        return text;
    }

    public Invoice getInvoiceForOrder(String orderId) {
        return invoiceDao.findByOrderId(orderId);
    }
}
