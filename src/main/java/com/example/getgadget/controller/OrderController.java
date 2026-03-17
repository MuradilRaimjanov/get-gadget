package com.example.getgadget.controller;

import com.example.getgadget.dto.order.OrderCreateRequest;
import com.example.getgadget.dto.order.OrderPatchRequest;
import com.example.getgadget.dto.order.OrderResponse;
import com.example.getgadget.dto.order.OrderUpdateRequest;
import com.example.getgadget.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> getAll() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.create(request);
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable Long id, @Valid @RequestBody OrderUpdateRequest request) {
        return orderService.update(id, request);
    }

    @PatchMapping("/{id}")
    public OrderResponse patch(@PathVariable Long id, @Valid @RequestBody OrderPatchRequest request) {
        return orderService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        orderService.delete(id);
    }
}
