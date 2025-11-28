package it.salspa.demo.spring.grpc.contract.core.mapper;

import it.salspa.demo.spring.grpc.contract.api.ContractCodeResponse;
import it.salspa.demo.spring.grpc.contract.api.ContractDetailResponse;
import it.salspa.demo.spring.grpc.contract.api.ContractStatus;
import it.salspa.demo.spring.grpc.contract.api.CreateContractRequest;
import it.salspa.demo.spring.grpc.contract.core.entity.ContractEntity;
import it.salspa.demo.spring.grpc.contract.core.entity.ProductItemEntity;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DateMapper.class, ProductItemMapper.class}, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface ContractMapper {

    ContractStatus ON_CREATE_STATUS = ContractStatus.CONTRACT_STATUS_PENDING;

    @Mapping(target = "code", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "status", expression = "java(ON_CREATE_STATUS)")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    ContractEntity toEntity(CreateContractRequest request);

    @Mapping(target = "code", source = "code")
    @Mapping(target = "version", source = "version")
    ContractCodeResponse toCodeResponseDto(ContractEntity contractEntity);

    @Mapping(target = "code", source = "contract.code")
    @Mapping(target = "version", source = "contract.version")
    @Mapping(target = "customerId", source = "contract.customerId") //TODO: in futuro chiamare il servizio Customer per avere i dettagli
    @Mapping(target = "status", source = "contract.status")
    @Mapping(target = "productsList", source = "productItems")
    @Mapping(target = "startDate", source = "contract.startDate")
    @Mapping(target = "endDate", source = "contract.endDate")
    ContractDetailResponse toDetailResponseDto(ContractEntity contract, List<ProductItemEntity> productItems);
}
