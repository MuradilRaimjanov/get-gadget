package com.example.getgadget.service;

import com.example.getgadget.model.Order;
import com.example.getgadget.model.Product;
import com.example.getgadget.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final CustomerService customerService;

    public OrderService(OrderRepository orderRepository, ProductService productService, CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.customerService = customerService;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
    }

    public Order create(Order order) {
        validateOrderReferences(order);
        order.setId(null);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(calculateTotal(order.getProductIds()));
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus("NEW");
        }
        return orderRepository.save(order);
    }

    public Order update(Long id, Order order) {
        Order existingOrder = findById(id);
        validateOrderReferences(order);
        order.setId(id);
        order.setCreatedAt(existingOrder.getCreatedAt());
        order.setTotalAmount(calculateTotal(order.getProductIds()));
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus(existingOrder.getStatus());
        }
        return orderRepository.save(order);
    }

    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id);
        }
        orderRepository.deleteById(id);
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
