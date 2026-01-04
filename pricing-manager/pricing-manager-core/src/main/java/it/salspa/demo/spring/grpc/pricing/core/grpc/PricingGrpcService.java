package it.salspa.demo.spring.grpc.pricing.core.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import it.salspa.demo.spring.grpc.pricing.api.*;
import it.salspa.demo.spring.grpc.pricing.core.service.PricingService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class PricingGrpcService extends PricingServiceGrpc.PricingServiceImplBase {

    private final PricingService pricingService;

    @Override
    public void createPrice(CreatePriceRequest request, StreamObserver<PriceCodeResponse> responseObserver) {
        PriceCodeResponse response = pricingService.create(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePrice(UpdatePriceRequest request, StreamObserver<PriceCodeResponse> responseObserver) {
        super.updatePrice(request, responseObserver);
    }

    @Override
    public void getPrice(PriceCodeRequest request, StreamObserver<PriceResponse> responseObserver) {
        PriceResponse response = pricingService.getPricing(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deletePrice(PriceCodeRequest request, StreamObserver<EmptyResponse> responseObserver) {
        super.deletePrice(request, responseObserver);
    }

    @Override
    public void listPrice(Empty request, StreamObserver<PriceListResponse> responseObserver) {
        super.listPrice(request, responseObserver);
    }

    @Override
    public void calculatePrice(CalculatePriceRequest request, StreamObserver<CalculatePriceResponse> responseObserver) {
        CalculatePriceResponse response = pricingService.calculatePrice(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
