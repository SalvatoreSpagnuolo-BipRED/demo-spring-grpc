package it.salspa.demo.spring.grpc.pricing.core.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import it.salspa.demo.spring.grpc.pricing.api.*;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class PricingGrpcService extends PricingServiceGrpc.PricingServiceImplBase {

    @Override
    public void createPrice(CreatePriceRequest request, StreamObserver<PriceCodeResponse> responseObserver) {
        super.createPrice(request, responseObserver);
    }

    @Override
    public void updatePrice(UpdatePriceRequest request, StreamObserver<PriceCodeResponse> responseObserver) {
        super.updatePrice(request, responseObserver);
    }

    @Override
    public void getPrice(PriceCodeRequest request, StreamObserver<PriceResponse> responseObserver) {
        super.getPrice(request, responseObserver);
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
    public void associatePriceToContractProduct(AssociatePriceToContractProductRequest request, StreamObserver<EmptyResponse> responseObserver) {
        super.associatePriceToContractProduct(request, responseObserver);
    }

    @Override
    public void calculatePrice(CalculatePriceRequest request, StreamObserver<CalculatePriceResponse> responseObserver) {
        super.calculatePrice(request, responseObserver);
    }

}
