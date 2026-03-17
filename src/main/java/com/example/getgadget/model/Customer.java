package com.example.getgadget.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "firstName is required")
    @Size(max = 255, message = "firstName must be <= 255 characters")
    private String firstName;

    @Column(nullable = false)
    @NotBlank(message = "lastName is required")
    @Size(max = 255, message = "lastName must be <= 255 characters")
    private String lastName;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 320, message = "email must be <= 320 characters")
    private String email;

    @Size(max = 50, message = "phone must be <= 50 characters")
    private String phone;
}
