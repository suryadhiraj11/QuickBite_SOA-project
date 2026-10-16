package com.quickbite.restaurant.repository;

import com.quickbite.restaurant.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuCategoryRepository
        extends JpaRepository<MenuCategory, Long> {

    List<MenuCategory> findByRestaurantId(Long restaurantId);
}