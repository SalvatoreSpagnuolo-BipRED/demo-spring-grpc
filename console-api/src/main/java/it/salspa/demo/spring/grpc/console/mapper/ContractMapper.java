package it.salspa.demo.spring.grpc.console.mapper;

import it.salspa.demo.spring.grpc.console.dto.ContractResponseDTO;
import it.salspa.demo.spring.grpc.contract.api.ContractResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    ContractResponseDTO toContractResponseDTO(ContractResponse response);
}
