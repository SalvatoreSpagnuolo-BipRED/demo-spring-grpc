package it.salspa.demo.spring.grpc.customer.core.grpc;

import io.grpc.stub.StreamObserver;
import it.salspa.demo.spring.grpc.customer.api.*;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class CustomerGrpcService extends CustomerServiceGrpc.CustomerServiceImplBase {

    @Override
    public void createCustomer(CreateCustomerRequest request, StreamObserver<CustomerResponse> responseObserver) {
        super.createCustomer(request, responseObserver);
    }

    @Override
    public void getCustomer(GetCustomerRequest request, StreamObserver<CustomerResponse> responseObserver) {
        super.getCustomer(request, responseObserver);
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
