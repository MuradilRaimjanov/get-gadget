package com.example.getgadget.service;

import com.example.getgadget.model.Order;
import com.example.getgadget.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0L);
    private final ProductService productService;
    private final CustomerService customerService;

    public OrderService(ProductService productService, CustomerService customerService) {
        this.productService = productService;
        this.customerService = customerService;
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public Order findById(Long id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id);
        }
        return order;
    }

    public Order create(Order order) {
        validateOrderReferences(order);
        long id = idGenerator.incrementAndGet();
        order.setId(id);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(calculateTotal(order.getProductIds()));
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus("NEW");
        }
        orders.put(id, order);
        return order;
    }

    public Order update(Long id, Order order) {
        if (!orders.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id);
        }
        validateOrderReferences(order);
        order.setId(id);
        Order existingOrder = orders.get(id);
        order.setCreatedAt(existingOrder.getCreatedAt());
        order.setTotalAmount(calculateTotal(order.getProductIds()));
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus(existingOrder.getStatus());
        }
        orders.put(id, order);
        return order;
    }

    public void delete(Long id) {
        if (orders.remove(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id);
        }
    }

    private void validateOrderReferences(Order order) {
        if (order.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        customerService.findById(order.getCustomerId());

        if (order.getProductIds() == null || order.getProductIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productIds must not be empty");
        }

        for (Long productId : order.getProductIds()) {
            productService.findById(productId);
        }
    }

    private BigDecimal calculateTotal(List<Long> productIds) {
        BigDecimal total = BigDecimal.ZERO;
        for (Long productId : productIds) {
            Product product = productService.findById(productId);
            if (product.getPrice() == null) {
                continue;
            }
            total = total.add(product.getPrice());
        }
        return total;
    }
}
