package com.quickbite.order.client;

public class RestaurantResponse {

    private Long id;
    private String name;
    private String location;
    private boolean open;
    private String ownerEmail;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public boolean isOpen() {
        return open;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }
}