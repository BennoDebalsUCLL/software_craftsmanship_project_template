package com.bookstore.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Database {

    public static final String DB_URL = "jdbc:mysql://prod-db-01.bookstore.internal:3306/bookstore";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "root123"; // TODO move to a config file

    public static int queryCount = 0;

    private static Database instance;

    private static Map<String, Map<String, Object>> tables = new HashMap<String, Map<String, Object>>();

    static {
        tables.put("books", new LinkedHashMap<String, Object>());
        tables.put("customers", new LinkedHashMap<String, Object>());
        tables.put("orders", new LinkedHashMap<String, Object>());
        tables.put("order_lines", new LinkedHashMap<String, Object>());
        tables.put("invoices", new LinkedHashMap<String, Object>());
        tables.put("shipments", new LinkedHashMap<String, Object>());
        tables.put("stock", new LinkedHashMap<String, Object>());
        tables.put("carts", new LinkedHashMap<String, Object>());
    }

    private Database() {
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public String getConnection() {
        return "connected to " + DB_URL + " as " + DB_USER + "/" + DB_PASSWORD;
    }

    // saves a row in a table
    public void save(String table, String id, Object row) {
        queryCount++;
        execute("INSERT INTO " + table + " VALUES (" + id + ", ...) ON DUPLICATE KEY UPDATE");
        tables.get(table).put(id, row);
    }

    public Object find(String table, String id) {
        queryCount++;
        execute("SELECT * FROM " + table + " WHERE id = '" + id + "'");
        return tables.get(table).get(id);
    }

    public List<Object> findAll(String table) {
        queryCount++;
        execute("SELECT * FROM " + table);
        return new ArrayList<Object>(tables.get(table).values());
    }

    public void delete(String table, String id) {
        queryCount++;
        execute("DELETE FROM " + table + " WHERE id = '" + id + "'");
        tables.get(table).remove(id);
    }

    public int count(String table) {
        return tables.get(table).size();
    }

    public void execute(String sql) {
        if (com.bookstore.util.Logger.DEBUG) {
            System.out.println("[SQL] " + sql);
        }
    }
}
