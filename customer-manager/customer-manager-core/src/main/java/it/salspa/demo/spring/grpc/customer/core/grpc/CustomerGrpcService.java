package it.salspa.demo.spring.grpc.customer.core.grpc;

import io.grpc.stub.StreamObserver;
import it.salspa.demo.spring.grpc.customer.api.CreateCustomerRequest;
import it.salspa.demo.spring.grpc.customer.api.CustomerResponse;
import it.salspa.demo.spring.grpc.customer.api.CustomerServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
public class CustomerGrpcService extends CustomerServiceGrpc.CustomerServiceImplBase {
    @Override
    public void createCustomer(CreateCustomerRequest request, StreamObserver<CustomerResponse> responseObserver) {
        CustomerResponse response = CustomerResponse.newBuilder()
                .setId("CUST-" + UUID.randomUUID())
                .setName(request.getName())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
