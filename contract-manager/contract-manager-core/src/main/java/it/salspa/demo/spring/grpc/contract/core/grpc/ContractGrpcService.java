package it.salspa.demo.spring.grpc.contract.core.grpc;

import io.grpc.stub.StreamObserver;
import it.salspa.demo.spring.grpc.contract.api.ContractResponse;
import it.salspa.demo.spring.grpc.contract.api.ContractServiceGrpc;
import it.salspa.demo.spring.grpc.contract.api.CreateContractRequest;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ContractGrpcService extends ContractServiceGrpc.ContractServiceImplBase {
    @Override
    public void createContract(CreateContractRequest request, StreamObserver<ContractResponse> responseObserver) {
        ContractResponse response = ContractResponse.newBuilder()
                .setCode("CONTRACT-" + request.getContractName())
                .setVersion(1)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
