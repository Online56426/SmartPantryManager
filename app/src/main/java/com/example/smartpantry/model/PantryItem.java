package com.example.smartpantry.model;

/**
 * One ingredient the user currently has at home.
 *
 * This is a plain Java object with no Android imports, which keeps it easy to
 * unit test. A new item has an id of
 * -1 until the database assigns a real one, so the save code can tell whether it
 * must insert or update.
 */
public class PantryItem {

    public static final long NO_ID = -1;

    /** Expiry is stored as milliseconds since the epoch; 0 means "no expiry date". */
    public static final long NO_EXPIRY = 0;

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private long expiryMillis;

    public PantryItem() {
        this.id = NO_ID;
        this.name = "";
        this.quantity = 0;
        this.unit = "pcs";
        this.expiryMillis = NO_EXPIRY;
    }

    public PantryItem(long id, String name, double quantity, String unit, long expiryMillis) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryMillis = expiryMillis;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public long getExpiryMillis() {
        return expiryMillis;
    }

    public void setExpiryMillis(long expiryMillis) {
        this.expiryMillis = expiryMillis;
    }

    public boolean hasExpiry() {
        return expiryMillis != NO_EXPIRY;
    }
}
