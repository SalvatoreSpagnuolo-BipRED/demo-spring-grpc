package it.salspa.demo.spring.grpc.customer.client;

import it.salspa.demo.spring.grpc.customer.api.CreateCustomerRequest;
import it.salspa.demo.spring.grpc.customer.api.CustomerResponse;
import it.salspa.demo.spring.grpc.customer.api.CustomerServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class CustomerClient {

    @GrpcClient("customer-manager")
    private CustomerServiceGrpc.CustomerServiceBlockingStub blockingStub;

    public CustomerResponse create(String name) {
        CreateCustomerRequest request = CreateCustomerRequest.newBuilder()
                .setName(name)
                .build();
        return blockingStub.createCustomer(request);
    }
}
