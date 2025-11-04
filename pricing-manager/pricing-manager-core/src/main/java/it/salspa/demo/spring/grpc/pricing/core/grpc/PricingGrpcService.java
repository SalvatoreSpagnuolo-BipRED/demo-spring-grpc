package it.salspa.demo.spring.grpc.pricing.core.grpc;

import io.grpc.stub.StreamObserver;
import it.salspa.demo.spring.grpc.pricing.api.PriceRequest;
import it.salspa.demo.spring.grpc.pricing.api.PriceResponse;
import it.salspa.demo.spring.grpc.pricing.api.PricingServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class PricingGrpcService extends PricingServiceGrpc.PricingServiceImplBase {
    @Override
    public void calculatePrice(PriceRequest request, StreamObserver<PriceResponse> responseObserver) {
        double unitPrice = 10.0; // dummy
        double total = unitPrice * request.getQuantity();
        PriceResponse response = PriceResponse.newBuilder()
                .setProductCode(request.getProductCode())
                .setQuantity(request.getQuantity())
                .setTotalPrice(total)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
