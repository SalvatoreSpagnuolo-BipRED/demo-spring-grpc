package it.salspa.demo.spring.grpc.pricing.client;

import it.salspa.demo.spring.grpc.pricing.api.PriceRequest;
import it.salspa.demo.spring.grpc.pricing.api.PriceResponse;
import it.salspa.demo.spring.grpc.pricing.api.PricingServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class PricingClient {

    @GrpcClient("pricing-manager")
    private PricingServiceGrpc.PricingServiceBlockingStub blockingStub;

    public PriceResponse calculate(String product, int quantity) {
        PriceRequest request = PriceRequest.newBuilder()
                .setProductCode(product)
                .setQuantity(quantity)
                .build();
        return blockingStub.calculatePrice(request);
    }
}
