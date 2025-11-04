package it.salspa.demo.spring.grpc.contract.client;

import it.salspa.demo.spring.grpc.contract.api.ContractResponse;
import it.salspa.demo.spring.grpc.contract.api.ContractServiceGrpc;
import it.salspa.demo.spring.grpc.contract.api.CreateContractRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class ContractClient {

    @GrpcClient("contract-manager")
    private ContractServiceGrpc.ContractServiceBlockingStub blockingStub;

    public ContractResponse create(String name, String details) {
        CreateContractRequest request = CreateContractRequest.newBuilder()
                .setContractName(name)
                .setContractDetails(details)
                .build();
        return blockingStub.createContract(request);
    }
}
