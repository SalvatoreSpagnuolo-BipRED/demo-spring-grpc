package it.salspa.demo.spring.grpc.contract.core.service;

import it.salspa.demo.spring.grpc.contract.api.*;
import it.salspa.demo.spring.grpc.contract.core.entity.ContractEntity;
import it.salspa.demo.spring.grpc.contract.core.entity.ProductItemEntity;
import it.salspa.demo.spring.grpc.contract.core.mapper.ContractMapper;
import it.salspa.demo.spring.grpc.contract.core.mapper.ProductItemMapper;
import it.salspa.demo.spring.grpc.contract.core.repository.ContractRepo;
import it.salspa.demo.spring.grpc.contract.core.repository.ProductItemRepo;
import it.salspa.demo.spring.grpc.customer.api.CustomerResponse;
import it.salspa.demo.spring.grpc.customer.api.GetCustomerRequest;
import it.salspa.demo.spring.grpc.customer.client.CustomerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepo contractRepo;
    private final ProductItemRepo productItemRepo;
    private final ContractMapper contractMapper;
    private final ProductItemMapper productItemMapper;
    private final CustomerClient customerClient;


    @Transactional
    public ContractCodeResponse createContract(CreateContractRequest request) {
        log.info("Creating contract for customerId: {}", request.getCustomerId());

        // Map and save ContractEntity
        ContractEntity contractEntity = contractMapper.toEntity(request);
        contractRepo.save(contractEntity);

        // Map and save ProductItemEntities
        List<ProductItemEntity> productItems = productItemMapper.toEntityList(request.getProductsList(), contractEntity.getCode());
        productItemRepo.saveAll(productItems);

        // Prepare and return response
        ContractCodeResponse response = contractMapper.toCodeResponseDto(contractEntity);
        log.info("Contract created with code: {}", response.getCode());

        return response;
    }

    public ContractDetailResponse getContract(ContractCodeRequest request) {
        log.info("Fetching contract details for code: {}", request.getCode());

        // Fetch ContractEntity
        ContractEntity contractEntity = contractRepo.findById(request.getCode())
                .orElseThrow(() -> new NoSuchElementException("Contract not found with code: " + request.getCode()));

        // Fetch associated ProductItemEntities
        List<ProductItemEntity> productItems = productItemRepo.findByContractCode(request.getCode());

        // Fetch associated Customer info via gRPC
        GetCustomerRequest getCustomerReq = GetCustomerRequest.newBuilder()
                .setId(contractEntity.getCustomerId())
                .build();
        CustomerResponse customerResponse = customerClient.get(getCustomerReq);

        // Map to response DTO
        ContractDetailResponse response = contractMapper.toDetailResponseDto(contractEntity, productItems, customerResponse);

        log.info("Contract details fetched for code: {}", request.getCode());
        return response;
    }
}
