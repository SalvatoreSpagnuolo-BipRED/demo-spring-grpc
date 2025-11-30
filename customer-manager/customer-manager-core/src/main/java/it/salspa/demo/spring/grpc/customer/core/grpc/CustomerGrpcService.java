package it.salspa.demo.spring.grpc.customer.core.grpc;

import io.grpc.stub.StreamObserver;
import it.salspa.demo.spring.grpc.customer.api.*;
import it.salspa.demo.spring.grpc.customer.core.service.CustomerService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class CustomerGrpcService extends CustomerServiceGrpc.CustomerServiceImplBase {

    private final CustomerService customerService;

    @Override
    public void createCustomer(CreateCustomerRequest request, StreamObserver<CustomerResponse> responseObserver) {
        CustomerResponse response = customerService.create(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getCustomer(GetCustomerRequest request, StreamObserver<CustomerResponse> responseObserver) {
        CustomerResponse response = customerService.getCustomer(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateCustomer(UpdateCustomerRequest request, StreamObserver<CustomerResponse> responseObserver) {
        super.updateCustomer(request, responseObserver);
    }

    @Override
    public void deleteCustomer(DeleteCustomerRequest request, StreamObserver<DeleteCustomerResponse> responseObserver) {
        super.deleteCustomer(request, responseObserver);
    }

    @Override
    public void listCustomers(ListCustomersRequest request, StreamObserver<ListCustomersResponse> responseObserver) {
        super.listCustomers(request, responseObserver);
    }

}
