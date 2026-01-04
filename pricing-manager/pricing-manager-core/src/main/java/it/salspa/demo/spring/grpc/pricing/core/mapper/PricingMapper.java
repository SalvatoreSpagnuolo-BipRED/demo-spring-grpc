package it.salspa.demo.spring.grpc.pricing.core.mapper;

import it.salspa.demo.spring.grpc.pricing.api.Bracket;
import it.salspa.demo.spring.grpc.pricing.api.CreatePriceRequest;
import it.salspa.demo.spring.grpc.pricing.api.PriceCodeResponse;
import it.salspa.demo.spring.grpc.pricing.api.PriceResponse;
import it.salspa.demo.spring.grpc.pricing.core.entity.BracketEntity;
import it.salspa.demo.spring.grpc.pricing.core.entity.PricingEntity;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface PricingMapper {

    @Mapping(target = "pricingBrackets", source = "pricingBracketsList")
    PricingEntity toEntity(CreatePriceRequest request);

    PriceCodeResponse toCodeDto(PricingEntity pricingEntity);

    @Mapping(target = "pricingBracketsList", source = "pricingBrackets")
    PriceResponse toDto(PricingEntity pricingEntity);

    BracketEntity toEntity(Bracket bracket);

    Bracket toDto(BracketEntity bracketEntity);
}
