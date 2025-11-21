package it.salspa.demo.spring.grpc.console.service;

import it.salspa.demo.spring.grpc.console.dto.ContractCodeResponseDTO;
import it.salspa.demo.spring.grpc.console.mapper.ContractMapper;
import it.salspa.demo.spring.grpc.contract.client.ContractClient;
import org.springframework.stereotype.Service;

@Service
public class ContractService {

    private final ContractClient contractClient;
    private final ContractMapper contractMapper;

    public ContractService(ContractClient contractClient, ContractMapper contractMapper) {
        this.contractClient = contractClient;
        this.contractMapper = contractMapper;
    }

    public ContractCodeResponseDTO create(/* to implement */) {
        return null;
    }
}
