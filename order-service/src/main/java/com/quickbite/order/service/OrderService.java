package com.quickbite.order.service;

import com.quickbite.order.client.MenuItemResponse;
import com.quickbite.order.client.RestaurantClient;
import com.quickbite.order.client.RestaurantResponse;
import com.quickbite.order.dto.CreateOrderRequest;
import com.quickbite.order.dto.OrderItemRequest;
import com.quickbite.order.entity.Order;
import com.quickbite.order.entity.OrderItem;
import com.quickbite.order.entity.OrderStatus;
import com.quickbite.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;

    public OrderService(
            OrderRepository orderRepository,
            RestaurantClient restaurantClient) {

        this.orderRepository = orderRepository;
        this.restaurantClient = restaurantClient;
    }

    @Transactional
    public Order createOrder(
            CreateOrderRequest request,
            String customerEmail) {

        // 1. Validate restaurant
        RestaurantResponse restaurant =
                restaurantClient.getRestaurant(request.getRestaurantId());

        if (restaurant == null) {
            throw new RuntimeException("Restaurant not found");
        }

        if (!restaurant.isOpen()) {
            throw new IllegalStateException(
                    "Restaurant is currently closed"
            );
        }

        // 2. Get restaurant menu
        List<MenuItemResponse> menuItems =
                restaurantClient.getMenu(request.getRestaurantId());

        Map<Long, MenuItemResponse> menuMap =
                menuItems.stream()
                        .collect(Collectors.toMap(
                                MenuItemResponse::getId,
                                item -> item
                        ));

        // 3. Create order
        Order order = new Order();

        order.setCustomerEmail(customerEmail);
        order.setRestaurantId(request.getRestaurantId());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(OrderStatus.PLACED);

        BigDecimal total = BigDecimal.ZERO;

        // 4. Validate every requested item
        for (OrderItemRequest requestedItem : request.getItems()) {

            MenuItemResponse menuItem =
                    menuMap.get(requestedItem.getMenuItemId());

            if (menuItem == null) {
                throw new IllegalArgumentException(
                        "Menu item not found: "
                                + requestedItem.getMenuItemId()
                );
            }

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException(
                        "Menu item is unavailable: "
                                + menuItem.getName()
                );
            }

            BigDecimal subtotal =
                    menuItem.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            requestedItem.getQuantity()
                                    )
                            );

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setMenuItemId(menuItem.getId());
            orderItem.setItemName(menuItem.getName());
            orderItem.setUnitPrice(menuItem.getPrice());
            orderItem.setQuantity(requestedItem.getQuantity());
            orderItem.setSubtotal(subtotal);

            order.getItems().add(orderItem);

            total = total.add(subtotal);
        }

        // 5. Set total
        order.setTotalAmount(total);

        // 6. Save order
        return orderRepository.save(order);
    }

    public Order getOrder(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));
    }

    public List<Order> getCustomerOrders(String customerEmail) {

        return orderRepository
                .findByCustomerEmailOrderByCreatedAtDesc(
                        customerEmail
                );
    }

    public List<Order> getRestaurantOrders(Long restaurantId) {

        return orderRepository
                .findByRestaurantIdOrderByCreatedAtDesc(
                        restaurantId
                );
    }
    
    public RestaurantResponse getRestaurant(Long restaurantId) {

        return restaurantClient.getRestaurant(restaurantId);
    }
    
    private void validateTransition(
            OrderStatus current,
            OrderStatus next) {

        if (next == OrderStatus.CANCELLED &&
                current == OrderStatus.PLACED) {
            return;
        }

        if (next == OrderStatus.REJECTED &&
                current == OrderStatus.PLACED) {
            return;
        }

        validateRestaurantTransition(current, next);
    }
    
    private void validateRestaurantTransition(
            OrderStatus current,
            OrderStatus next) {

        boolean valid = switch (current) {

            case PLACED ->
                    next == OrderStatus.ACCEPTED ||
                    next == OrderStatus.REJECTED;

            case ACCEPTED ->
                    next == OrderStatus.PREPARING;

            case PREPARING ->
                    next == OrderStatus.READY;

            case READY ->
                    next == OrderStatus.OUT_FOR_DELIVERY;

            case OUT_FOR_DELIVERY ->
                    next == OrderStatus.DELIVERED;

            default -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Invalid order status transition: "
                            + current + " -> " + next
            );
        }
    }
    
    public Order updateStatus(
            Long orderId,
            OrderStatus newStatus,
            String userEmail,
            boolean isAdmin,
            boolean isRestaurantOwner) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        // CUSTOMER cancellation
        if (!isAdmin && !isRestaurantOwner) {

            if (!order.getCustomerEmail().equals(userEmail)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "You are not allowed to modify this order"
                );
            }

            if (newStatus != OrderStatus.CANCELLED) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Customer can only cancel an order"
                );
            }

            if (currentStatus != OrderStatus.PLACED) {
                throw new IllegalStateException(
                        "Order can only be cancelled while it is PLACED"
                );
            }

            order.setStatus(OrderStatus.CANCELLED);
            return orderRepository.save(order);
        }

        // ADMIN can perform valid transitions
        if (isAdmin) {
            validateTransition(currentStatus, newStatus);
            order.setStatus(newStatus);
            return orderRepository.save(order);
        }

        // RESTAURANT OWNER
        if (isRestaurantOwner) {

            RestaurantResponse restaurant =
                    restaurantClient.getRestaurant(order.getRestaurantId());

            if (restaurant == null ||
                    !restaurant.getOwnerEmail().equals(userEmail)) {

                throw new org.springframework.security.access.AccessDeniedException(
                        "You are not allowed to manage this restaurant's orders"
                );
            }

            validateRestaurantTransition(currentStatus, newStatus);

            order.setStatus(newStatus);
            return orderRepository.save(order);
        }

        throw new org.springframework.security.access.AccessDeniedException(
                "You are not allowed to modify this order"
        );
    }
    
    public Order updatePaymentStatus(Long orderId, OrderStatus paymentStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (paymentStatus == OrderStatus.PAYMENT_FAILED) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            return orderRepository.save(order);
        }

        if (paymentStatus == OrderStatus.ACCEPTED) {

            if (order.getStatus() != OrderStatus.PLACED) {
                throw new IllegalStateException(
                        "Only PLACED orders can be accepted after payment"
                );
            }

            order.setStatus(OrderStatus.ACCEPTED);
            return orderRepository.save(order);
        }

        throw new IllegalArgumentException(
                "Invalid payment status: " + paymentStatus
        );
    }
    
    
}