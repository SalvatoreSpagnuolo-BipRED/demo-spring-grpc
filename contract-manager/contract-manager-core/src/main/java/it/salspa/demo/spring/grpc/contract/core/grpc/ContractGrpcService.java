package it.salspa.demo.spring.grpc.contract.core.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import it.salspa.demo.spring.grpc.contract.api.*;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ContractGrpcService extends ContractServiceGrpc.ContractServiceImplBase {

    @Override
    public void createContract(CreateContractRequest request, StreamObserver<ContractCodeResponse> responseObserver) {
        super.createContract(request, responseObserver);
    }

    @Override
    public void updateContract(UpdateContractRequest request, StreamObserver<ContractCodeResponse> responseObserver) {
        super.updateContract(request, responseObserver);
    }

    @Override
    public void getContract(ContractCodeRequest request, StreamObserver<ContractDetailResponse> responseObserver) {
        super.getContract(request, responseObserver);
    }

    @Override
    public void deleteContract(ContractCodeRequest request, StreamObserver<EmptyResponse> responseObserver) {
        super.deleteContract(request, responseObserver);
    }

    @Override
    public void listContract(Empty request, StreamObserver<ListContractsResponse> responseObserver) {
        super.listContract(request, responseObserver);
    }

    @Override
    public void activateContract(ContractCodeRequest request, StreamObserver<EmptyResponse> responseObserver) {
        super.activateContract(request, responseObserver);
    }

    @Override
    public void deactivateContract(ContractCodeRequest request, StreamObserver<EmptyResponse> responseObserver) {
        super.deactivateContract(request, responseObserver);
    }

    @Override
    public void addUsageToContractProduct(AddUsageToContractProductRequest request, StreamObserver<EmptyResponse> responseObserver) {
        super.addUsageToContractProduct(request, responseObserver);
    }

}
