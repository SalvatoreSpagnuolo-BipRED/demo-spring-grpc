package it.salspa.demo.spring.grpc.console.mapper;

import it.salspa.demo.spring.grpc.console.dto.customer.CreateCustomerRequestDTO;
import it.salspa.demo.spring.grpc.console.dto.customer.CustomerResponseDTO;
import it.salspa.demo.spring.grpc.customer.api.CreateCustomerRequest;
import it.salspa.demo.spring.grpc.customer.api.CustomerResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CreateCustomerRequest toCreateRequest(CreateCustomerRequestDTO requestDTO);

    CustomerResponseDTO toResponseDTO(CustomerResponse grpcResponse);
}
