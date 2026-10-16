package com.quickbite.restaurant.controller;

import com.quickbite.restaurant.dto.MenuCategoryRequest;
import org.springframework.security.core.Authentication;
import com.quickbite.restaurant.dto.MenuItemRequest;
import com.quickbite.restaurant.entity.MenuCategory;
import com.quickbite.restaurant.entity.MenuItem;
import com.quickbite.restaurant.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @PostMapping("/categories")
    public ResponseEntity<MenuCategory> createCategory(
            @Valid @RequestBody MenuCategoryRequest request,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(menuService.createCategory(
                        request,
                        ownerEmail,
                        isAdmin
                ));
    }

    @GetMapping("/{restaurantId}/categories")
    public ResponseEntity<List<MenuCategory>> getCategories(
            @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
                menuService.getCategories(restaurantId));
    }
    
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @PostMapping("/menu-items")
    public ResponseEntity<MenuItem> createItem(
            @Valid @RequestBody MenuItemRequest request,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(menuService.createItem(
                        request,
                        ownerEmail,
                        isAdmin
                ));
    }
    
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<MenuCategory> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody MenuCategoryRequest request,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                menuService.updateCategory(
                        id,
                        request,
                        ownerEmail,
                        isAdmin
                )
        );
    }
    
    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<?> deleteCategory(
            @PathVariable Long id,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            menuService.deleteCategory(id, ownerEmail, isAdmin);
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "Category cannot be deleted",
                            "message", e.getMessage()
                    ));
        }
    }

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<MenuItem>> getMenu(
            @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
                menuService.getMenu(restaurantId));
    }

    @GetMapping("/{restaurantId}/menu/available")
    public ResponseEntity<List<MenuItem>> getAvailableMenu(
            @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
                menuService.getAvailableMenu(restaurantId));
    }
    
    
    @PutMapping("/menu-items/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<MenuItem> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                menuService.updateItem(
                        id,
                        request,
                        ownerEmail,
                        isAdmin
                )
        );
    }
    
    @DeleteMapping("/menu-items/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN"));

        menuService.deleteItem(
                id,
                ownerEmail,
                isAdmin
        );

        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/menu-items/{id}/availability")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<MenuItem> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                menuService.updateAvailability(
                        id,
                        available,
                        ownerEmail,
                        isAdmin
                )
        );
    }
}