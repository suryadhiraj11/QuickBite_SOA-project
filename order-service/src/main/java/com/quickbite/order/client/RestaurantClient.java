package com.quickbite.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.quickbite.order.config.FeignConfig;

@FeignClient(
        name = "RESTAURANT-SERVICE",
        configuration = FeignConfig.class
)
public interface RestaurantClient {

    @GetMapping("/api/restaurants/{id}")
    RestaurantResponse getRestaurant(@PathVariable Long id);

    @GetMapping("/api/restaurants/{restaurantId}/menu")
    java.util.List<MenuItemResponse> getMenu(
            @PathVariable Long restaurantId
    );
}