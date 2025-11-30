package it.salspa.demo.spring.grpc.customer.core.mapper;

import it.salspa.demo.spring.grpc.customer.api.CreateCustomerRequest;
import it.salspa.demo.spring.grpc.customer.api.CustomerResponse;
import it.salspa.demo.spring.grpc.customer.core.entity.CustomerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface CustomerMapper {

    CustomerEntity toEntity(CreateCustomerRequest request);

    CustomerResponse toDto(CustomerEntity customerEntity);
}
