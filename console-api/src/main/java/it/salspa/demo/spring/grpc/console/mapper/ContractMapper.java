package it.salspa.demo.spring.grpc.console.mapper;

import com.google.protobuf.Timestamp;
import it.salspa.demo.spring.grpc.console.dto.ContractCodeResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.ContractDetailResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.CreateContractRequestDTO;
import it.salspa.demo.spring.grpc.console.dto.ProductDTO;
import it.salspa.demo.spring.grpc.contract.api.ContractCodeResponse;
import it.salspa.demo.spring.grpc.contract.api.ContractDetailResponse;
import it.salspa.demo.spring.grpc.contract.api.ContractStatus;
import it.salspa.demo.spring.grpc.contract.api.CreateContractRequest;
import it.salspa.demo.spring.grpc.contract.api.Product;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface ContractMapper {

    ContractCodeResponseDTO toContractResponseDTO(ContractCodeResponse response);

    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "productsList", source = "products")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    CreateContractRequest toCreateContractRequest(CreateContractRequestDTO dto);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "pricingId", source = "pricingId")
    @Mapping(target = "usageAmount", source = "usageAmount")
    Product toProduct(ProductDTO dto);

    @Mapping(target = "code", source = "code")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "products", source = "productsList")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    ContractDetailResponseDTO toContractDetailResponseDTO(ContractDetailResponse response);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "pricingId", source = "pricingId")
    @Mapping(target = "usageAmount", source = "usageAmount")
    ProductDTO toProductDTO(Product product);

    default Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    default LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    default String toString(ContractStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }
}
