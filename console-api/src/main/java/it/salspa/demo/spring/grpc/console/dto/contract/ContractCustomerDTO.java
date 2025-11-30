package it.salspa.demo.spring.grpc.console.dto.contract;

public record ContractCustomerDTO(
        String id,
        String name,
        String email,
        String phoneNumber
) {}
