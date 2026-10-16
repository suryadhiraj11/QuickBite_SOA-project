package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.MenuCategoryRequest;
import com.quickbite.restaurant.dto.MenuItemRequest;
import com.quickbite.restaurant.entity.MenuCategory;
import com.quickbite.restaurant.entity.MenuItem;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.repository.MenuCategoryRepository;
import com.quickbite.restaurant.repository.MenuItemRepository;
import com.quickbite.restaurant.repository.RestaurantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuService(
            MenuCategoryRepository categoryRepository,
            MenuItemRepository itemRepository,
            RestaurantRepository restaurantRepository) {

        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public MenuCategory createCategory(
            MenuCategoryRequest request,
            String ownerEmail,
            boolean isAdmin) {

        verifyRestaurantOwnership(
                request.getRestaurantId(),
                ownerEmail,
                isAdmin
        );

        MenuCategory category = new MenuCategory();

        category.setRestaurantId(request.getRestaurantId());
        category.setName(request.getName());

        return categoryRepository.save(category);
    }

    public List<MenuCategory> getCategories(Long restaurantId) {
        return categoryRepository.findByRestaurantId(restaurantId);
    }

    public MenuItem createItem(
            MenuItemRequest request,
            String ownerEmail,
            boolean isAdmin) {

        verifyRestaurantOwnership(
                request.getRestaurantId(),
                ownerEmail,
                isAdmin
        );

        MenuItem item = new MenuItem();

        item.setRestaurantId(request.getRestaurantId());
        item.setCategoryId(request.getCategoryId());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setImageUrl(request.getImageUrl());

        return itemRepository.save(item);
    }

    public List<MenuItem> getMenu(Long restaurantId) {
        return itemRepository.findByRestaurantId(restaurantId);
    }

    public List<MenuItem> getAvailableMenu(Long restaurantId) {
        return itemRepository
                .findByRestaurantIdAndAvailableTrue(restaurantId);
    }

    private void verifyRestaurantOwnership(
            Long restaurantId,
            String ownerEmail,
            boolean isAdmin) {

        Restaurant restaurant = restaurantRepository
                .findById(restaurantId)
                .orElseThrow(() ->
                        new RuntimeException("Restaurant not found"));

        if (!isAdmin &&
                !restaurant.getOwnerEmail().equals(ownerEmail)) {

            throw new AccessDeniedException(
                    "You are not allowed to manage this restaurant");
        }
    }
    
    public MenuCategory updateCategory(
            Long id,
            MenuCategoryRequest request,
            String ownerEmail,
            boolean isAdmin) {

        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        verifyRestaurantOwnership(
                category.getRestaurantId(),
                ownerEmail,
                isAdmin
        );

        category.setName(request.getName());

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id, String ownerEmail, boolean isAdmin) {

        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        verifyRestaurantOwnership(
                category.getRestaurantId(),
                ownerEmail,
                isAdmin
        );

        List<MenuItem> items = itemRepository.findByCategoryId(id);

        if (!items.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete category because menu items are assigned to it"
            );
        }

        categoryRepository.delete(category);
    }

    public MenuItem updateItem(
            Long id,
            MenuItemRequest request,
            String ownerEmail,
            boolean isAdmin) {

        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Menu item not found"));

        verifyRestaurantOwnership(
                item.getRestaurantId(),
                ownerEmail,
                isAdmin
        );

        item.setCategoryId(request.getCategoryId());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setImageUrl(request.getImageUrl());

        return itemRepository.save(item);
    }

    public void deleteItem(
            Long id,
            String ownerEmail,
            boolean isAdmin) {

        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Menu item not found"));

        verifyRestaurantOwnership(
                item.getRestaurantId(),
                ownerEmail,
                isAdmin
        );

        itemRepository.delete(item);
    }

    public MenuItem updateAvailability(
            Long id,
            boolean available,
            String ownerEmail,
            boolean isAdmin) {

        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Menu item not found"));

        verifyRestaurantOwnership(
                item.getRestaurantId(),
                ownerEmail,
                isAdmin
        );

        item.setAvailable(available);

        return itemRepository.save(item);
    }
}