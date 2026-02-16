package com.example.getgadget.service;

import com.example.getgadget.model.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CustomerService {

    private final Map<Long, Customer> customers = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0L);

    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    public Customer findById(Long id) {
        Customer customer = customers.get(id);
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + id);
        }
        return customer;
    }

    public Customer create(Customer customer) {
        long id = idGenerator.incrementAndGet();
        customer.setId(id);
        customers.put(id, customer);
        return customer;
    }

    public Customer update(Long id, Customer customer) {
        if (!customers.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + id);
        }
        customer.setId(id);
        customers.put(id, customer);
        return customer;
    }

    public void delete(Long id) {
        if (customers.remove(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + id);
        }
    }
}
