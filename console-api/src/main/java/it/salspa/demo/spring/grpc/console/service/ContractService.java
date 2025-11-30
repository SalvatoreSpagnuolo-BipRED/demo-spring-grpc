package it.salspa.demo.spring.grpc.console.service;

import it.salspa.demo.spring.grpc.console.dto.contract.ContractCodeResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.contract.ContractDetailResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.contract.CreateContractRequestDTO;
import it.salspa.demo.spring.grpc.console.mapper.ContractMapper;
import it.salspa.demo.spring.grpc.contract.api.ContractCodeRequest;
import it.salspa.demo.spring.grpc.contract.api.ContractCodeResponse;
import it.salspa.demo.spring.grpc.contract.api.ContractDetailResponse;
import it.salspa.demo.spring.grpc.contract.api.CreateContractRequest;
import it.salspa.demo.spring.grpc.contract.client.ContractClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractClient contractClient;
    private final ContractMapper contractMapper;

    public ContractCodeResponseDTO create(CreateContractRequestDTO requestDTO) {
        CreateContractRequest grpcRequest = contractMapper.toCreateContractRequest(requestDTO);
        ContractCodeResponse grpcResponse = contractClient.create(grpcRequest);
        return contractMapper.toContractResponseDTO(grpcResponse);
    }

    public ContractDetailResponseDTO getByCode(String code) {
        ContractCodeRequest grpcRequest = ContractCodeRequest.newBuilder()
                .setCode(code)
                .build();
        ContractDetailResponse grpcResponse = contractClient.get(grpcRequest);
        return contractMapper.toContractDetailResponseDTO(grpcResponse);
    }
}
