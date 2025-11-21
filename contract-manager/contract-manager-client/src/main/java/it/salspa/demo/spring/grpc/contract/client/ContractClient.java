package it.salspa.demo.spring.grpc.contract.client;

import com.google.protobuf.Empty;
import it.salspa.demo.spring.grpc.contract.api.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class ContractClient {

    @GrpcClient("contract-manager")
    private ContractServiceGrpc.ContractServiceBlockingStub blockingStub;

    public ContractCodeResponse create(CreateContractRequest request) {
        return blockingStub.createContract(request);
    }

    public ContractCodeResponse update(UpdateContractRequest request) {
        return blockingStub.updateContract(request);
    }

    public ContractDetailResponse get(ContractCodeRequest request) {
        return blockingStub.getContract(request);
    }

    public EmptyResponse delete(ContractCodeRequest request) {
        return blockingStub.deleteContract(request);
    }

    public ListContractsResponse list() {
        return blockingStub.listContract(Empty.getDefaultInstance());
    }

    public EmptyResponse activate(ContractCodeRequest request) {
        return blockingStub.activateContract(request);
    }

    public EmptyResponse deactivate(ContractCodeRequest request) {
        return blockingStub.deactivateContract(request);
    }

    public EmptyResponse addUsageToContractProduct(AddUsageToContractProductRequest request) {
        return blockingStub.addUsageToContractProduct(request);
    }
}
