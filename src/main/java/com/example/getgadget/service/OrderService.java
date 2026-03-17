package com.example.getgadget.service;

import com.example.getgadget.dto.order.OrderCreateRequest;
import com.example.getgadget.dto.order.OrderPatchRequest;
import com.example.getgadget.dto.order.OrderResponse;
import com.example.getgadget.dto.order.OrderUpdateRequest;
import com.example.getgadget.model.Order;
import com.example.getgadget.model.Product;
import com.example.getgadget.repository.CustomerRepository;
import com.example.getgadget.repository.OrderRepository;
import com.example.getgadget.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(OrderService::toResponse).toList();
    }

    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        return toResponse(order);
    }

    public OrderResponse create(OrderCreateRequest request) {
        validateOrderReferences(request.getCustomerId(), request.getProductIds());
        Order order = new Order();
        order.setId(null);
        order.setCustomerId(request.getCustomerId());
        order.setProductIds(request.getProductIds());
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(calculateTotal(request.getProductIds()));
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            order.setStatus("NEW");
        } else {
            order.setStatus(request.getStatus());
        }
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse update(Long id, OrderUpdateRequest request) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));

        validateOrderReferences(request.getCustomerId(), request.getProductIds());

        existingOrder.setCustomerId(request.getCustomerId());
        existingOrder.setProductIds(request.getProductIds());
        existingOrder.setTotalAmount(calculateTotal(request.getProductIds()));
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            existingOrder.setStatus(request.getStatus());
        }

        return toResponse(orderRepository.save(existingOrder));
    }

    public OrderResponse patch(Long id, OrderPatchRequest request) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));

        if (request.getCustomerId() != null) {
            customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + request.getCustomerId()));
            existingOrder.setCustomerId(request.getCustomerId());
        }

        if (request.getProductIds() != null) {
            if (request.getProductIds().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productIds must not be empty");
            }
            for (Long productId : request.getProductIds()) {
                productRepository.findById(productId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));
            }
            existingOrder.setProductIds(request.getProductIds());
            existingOrder.setTotalAmount(calculateTotal(request.getProductIds()));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            existingOrder.setStatus(request.getStatus());
        }

        return toResponse(orderRepository.save(existingOrder));
    }

    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    private void validateOrderReferences(Long customerId, List<Long> productIds) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + customerId));

        if (productIds == null || productIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productIds must not be empty");
        }

        for (Long productId : productIds) {
            productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));
        }
    }

    private BigDecimal calculateTotal(List<Long> productIds) {
        BigDecimal total = BigDecimal.ZERO;
        for (Long productId : productIds) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));
            if (product.getPrice() == null) {
                continue;
            }
            total = total.add(product.getPrice());
        }
        return total;
    }

    private static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .productIds(order.getProductIds())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
