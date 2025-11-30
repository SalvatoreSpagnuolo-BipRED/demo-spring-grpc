package it.salspa.demo.spring.grpc.console.dto.customer;

import it.salspa.demo.spring.grpc.customer.api.CustomerStatus;

public record CreateCustomerRequestDTO(
        String name,
        String email,
        String phoneNumber,
        CustomerStatus status
) {}
