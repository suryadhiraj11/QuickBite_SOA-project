package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    public Restaurant createRestaurant(
            RestaurantRequest request,
            String ownerEmail) {

        Restaurant restaurant = new Restaurant();

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setLocation(request.getLocation());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());

        restaurant.setOwnerEmail(ownerEmail);

        return repository.save(restaurant);
    }
    
    public Restaurant updateRestaurant(
            Long id,
            RestaurantRequest request,
            String ownerEmail,
            boolean isAdmin) {

        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Restaurant not found"));

        if (!isAdmin && !restaurant.getOwnerEmail().equals(ownerEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not allowed to modify this restaurant");
        }

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setLocation(request.getLocation());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());

        return repository.save(restaurant);
    }

    public void deleteRestaurant(
            Long id,
            String ownerEmail,
            boolean isAdmin) {

        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Restaurant not found"));

        if (!isAdmin && !restaurant.getOwnerEmail().equals(ownerEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not allowed to delete this restaurant");
        }

        repository.delete(restaurant);
    }

    public List<Restaurant> getAllRestaurants() {
        return repository.findAll();
    }

    public List<Restaurant> getOpenRestaurants() {
        return repository.findByOpenTrue();
    }

    public List<Restaurant> getByLocation(String location) {
        return repository.findByLocationIgnoreCase(location);
    }

    
    public List<Restaurant> getMyRestaurants(String ownerEmail) {

        return repository.findByOwnerEmail(ownerEmail);

    }

    public Restaurant getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Restaurant not found"));
    }
}