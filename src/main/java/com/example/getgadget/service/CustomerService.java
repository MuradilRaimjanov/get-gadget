package com.example.getgadget.service;

import com.example.getgadget.model.Customer;
import com.example.getgadget.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + id));
    }

    public Customer create(Customer customer) {
        customer.setId(null);
        return customerRepository.save(customer);
    }

    public Customer update(Long id, Customer customer) {
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + id);
        }
        customer.setId(id);
        return customerRepository.save(customer);
    }

    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + id);
        }
        customerRepository.deleteById(id);
    }
}
