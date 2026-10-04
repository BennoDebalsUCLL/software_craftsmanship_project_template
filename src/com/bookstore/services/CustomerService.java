package com.bookstore.services;

import com.bookstore.dao.CustomerDao;
import com.bookstore.models.Customer;
import com.bookstore.models.Order;
import com.bookstore.util.Logger;
import com.bookstore.util.Utils;
import java.util.List;

public class CustomerService {

    private CustomerDao customerDao = new CustomerDao();
    private Logger logger = Logger.getInstance();

    public Customer register(String name, String email, String street, String houseNumber, String zipCode,
            String city, String country, String type) {
        if (name == null || name.trim().length() == 0) {
            throw new RuntimeException("name is required");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new RuntimeException("invalid email");
        }
        if (customerDao.findByEmail(email) != null) {
            throw new RuntimeException("email already used");
        }
        if (country == null || country.length() != 2) {
            throw new RuntimeException("country must be a 2 letter code");
        }
        Customer customer = new Customer();
        customer.setId(customerDao.nextId());
        customer.setName(name);
        customer.setEmail(email);
        customer.setStreet(street);
        customer.setHouseNumber(houseNumber);
        customer.setZipCode(zipCode);
        customer.setCity(city);
        customer.setCountry(country);
        customer.setType(type);
        customer.setLoyaltyPoints(0);
        customerDao.save(customer);
        logger.log("registered client " + customer.getId() + " (" + email + ")");
        return customer;
    }

    public Customer getUser(String id) {
        return customerDao.findById(id);
    }

    public List<Customer> getAllUsers() {
        return customerDao.findAll();
    }

    public boolean isGold(Customer customer) {
        return customer.getType().equals("GOLD");
    }

    public void addLoyaltyPoints(Customer customer, double orderTotal) {
        int points = (int) (orderTotal / 10);
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        customerDao.save(customer);
    }

    public String getFullAddress(Customer customer) {
        return customer.getStreet() + " " + customer.getHouseNumber() + ", " + customer.getZipCode() + " "
                + customer.getCity() + " (" + customer.getCountry() + ")";
    }

    public double getLifetimeValue(String customerId) {
        Customer customer = customerDao.findById(customerId);
        double total = 0.0;
        List<Order> orders = customer.getOrders();
        for (int i = 0; i < orders.size(); i++) {
            total = total + orders.get(i).getTotal();
        }
        return Utils.round2(total);
    }
}
