package it.salspa.demo.spring.grpc.customer.core.service;

import it.salspa.demo.spring.grpc.customer.api.CreateCustomerRequest;
import it.salspa.demo.spring.grpc.customer.api.CustomerResponse;
import it.salspa.demo.spring.grpc.customer.api.GetCustomerRequest;
import it.salspa.demo.spring.grpc.customer.core.entity.CustomerEntity;
import it.salspa.demo.spring.grpc.customer.core.mapper.CustomerMapper;
import it.salspa.demo.spring.grpc.customer.core.repository.CustomerRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepo customerRepo;
    private final CustomerMapper customerMapper;

    public CustomerResponse create(CreateCustomerRequest request) {
        log.info("Creating customer with name: {}", request.getName());

        CustomerEntity customerEntity = customerMapper.toEntity(request);
        customerRepo.save(customerEntity);

        return customerMapper.toDto(customerEntity);
    }

    public CustomerResponse getCustomer(GetCustomerRequest request) {
        log.info("Fetching customer with ID: {}", request.getId());

        CustomerEntity customerEntity = customerRepo.findById(request.getId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found with ID: " + request.getId()));

        return customerMapper.toDto(customerEntity);
    }
}
