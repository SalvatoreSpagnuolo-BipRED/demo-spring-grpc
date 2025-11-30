package it.salspa.demo.spring.grpc.console.service;

import it.salspa.demo.spring.grpc.console.dto.contract.ContractCodeResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.contract.ContractDetailResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.contract.CreateContractRequestDTO;
import it.salspa.demo.spring.grpc.console.dto.customer.CreateCustomerRequestDTO;
import it.salspa.demo.spring.grpc.console.dto.customer.CustomerResponseDTO;
import it.salspa.demo.spring.grpc.console.mapper.CustomerMapper;
import it.salspa.demo.spring.grpc.contract.api.ContractCodeRequest;
import it.salspa.demo.spring.grpc.contract.api.ContractCodeResponse;
import it.salspa.demo.spring.grpc.contract.api.ContractDetailResponse;
import it.salspa.demo.spring.grpc.contract.api.CreateContractRequest;
import it.salspa.demo.spring.grpc.customer.api.CreateCustomerRequest;
import it.salspa.demo.spring.grpc.customer.api.CustomerResponse;
import it.salspa.demo.spring.grpc.customer.api.GetCustomerRequest;
import it.salspa.demo.spring.grpc.customer.client.CustomerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerClient customerClient;
    private final CustomerMapper customerMapper;

    public CustomerResponseDTO create(CreateCustomerRequestDTO requestDTO) {
        CreateCustomerRequest grpcRequest = customerMapper.toCreateRequest(requestDTO);
        CustomerResponse grpcResponse = customerClient.create(grpcRequest);
        return customerMapper.toResponseDTO(grpcResponse);
    }

    public CustomerResponseDTO getByCode(String code) {
        GetCustomerRequest grpcRequest = GetCustomerRequest.newBuilder()
                .setId(code)
                .build();
        CustomerResponse grpcResponse = customerClient.get(grpcRequest);
        return customerMapper.toResponseDTO(grpcResponse);
    }
}
