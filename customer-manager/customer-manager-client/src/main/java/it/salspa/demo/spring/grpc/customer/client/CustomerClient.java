package it.salspa.demo.spring.grpc.customer.client;

import it.salspa.demo.spring.grpc.customer.api.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class CustomerClient {

    @GrpcClient("customer-manager")
    private CustomerServiceGrpc.CustomerServiceBlockingStub blockingStub;

    public CustomerResponse create(CreateCustomerRequest request) {
        return blockingStub.createCustomer(request);
    }

    public CustomerResponse get(GetCustomerRequest request) {
        return blockingStub.getCustomer(request);
    }

    public CustomerResponse update(UpdateCustomerRequest request) {
        return blockingStub.updateCustomer(request);
    }

    public DeleteCustomerResponse delete(DeleteCustomerRequest request) {
        return blockingStub.deleteCustomer(request);
    }

    public ListCustomersResponse list(ListCustomersRequest request) {
        return blockingStub.listCustomers(request);
    }
}
