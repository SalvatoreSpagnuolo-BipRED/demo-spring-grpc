package it.salspa.demo.spring.grpc.pricing.client;

import com.google.protobuf.Empty;
import it.salspa.demo.spring.grpc.pricing.api.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class PricingClient {

    @GrpcClient("pricing-manager")
    private PricingServiceGrpc.PricingServiceBlockingStub blockingStub;

    public PriceCodeResponse create(CreatePriceRequest request) {
        return blockingStub.createPrice(request);
    }

    public PriceCodeResponse update(UpdatePriceRequest request) {
        return blockingStub.updatePrice(request);
    }

    public PriceResponse get(PriceCodeRequest request) {
        return blockingStub.getPrice(request);
    }

    public EmptyResponse delete(PriceCodeRequest request) {
        return blockingStub.deletePrice(request);
    }

    public PriceListResponse list() {
        return blockingStub.listPrice(Empty.getDefaultInstance());
    }

    public CalculatePriceResponse calculatePrice(CalculatePriceRequest request) {
        return blockingStub.calculatePrice(request);
    }
}
