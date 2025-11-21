package it.salspa.demo.spring.grpc.console.mapper;

import it.salspa.demo.spring.grpc.console.dto.ContractCodeResponseDTO;
import it.salspa.demo.spring.grpc.contract.api.ContractCodeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    ContractCodeResponseDTO toContractResponseDTO(ContractCodeResponse response);
}
