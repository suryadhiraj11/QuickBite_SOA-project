package com.quickbite.restaurant.controller;

import com.quickbite.restaurant.dto.RestaurantRequest;
import org.springframework.security.core.Authentication;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }
    
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createRestaurant(
                        request,
                        ownerEmail));
    }
    
    
    @GetMapping("/my-restaurants")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<List<Restaurant>> getMyRestaurants(
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        return ResponseEntity.ok(
                service.getMyRestaurants(ownerEmail)
        );
    }

    

    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        return ResponseEntity.ok(service.getAllRestaurants());
    }

    @GetMapping("/open")
    public ResponseEntity<List<Restaurant>> getOpenRestaurants() {
        return ResponseEntity.ok(service.getOpenRestaurants());
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<Restaurant>> getByLocation(
            @PathVariable String location) {

        return ResponseEntity.ok(
                service.getByLocation(location));
    }

    

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getById(id));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<Restaurant> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                service.updateRestaurant(
                        id,
                        request,
                        ownerEmail,
                        isAdmin
                )
        );
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable Long id,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        service.deleteRestaurant(
                id,
                ownerEmail,
                isAdmin
        );

        return ResponseEntity.noContent().build();
    }
}