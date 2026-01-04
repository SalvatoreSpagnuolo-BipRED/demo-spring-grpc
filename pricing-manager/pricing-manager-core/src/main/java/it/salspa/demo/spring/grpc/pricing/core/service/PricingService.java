package it.salspa.demo.spring.grpc.pricing.core.service;

import it.salspa.demo.spring.grpc.pricing.api.*;
import it.salspa.demo.spring.grpc.pricing.core.entity.BracketEntity;
import it.salspa.demo.spring.grpc.pricing.core.entity.PricingEntity;
import it.salspa.demo.spring.grpc.pricing.core.mapper.PricingMapper;
import it.salspa.demo.spring.grpc.pricing.core.repository.PricingRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRepo pricingRepo;
    private final PricingMapper pricingMapper;

    @Transactional
    public PriceCodeResponse create(CreatePriceRequest request) {
        log.info("Creating price configuration with data: {}", request);

        PricingEntity pricingEntity = pricingMapper.toEntity(request);
        pricingRepo.save(pricingEntity);

        return pricingMapper.toCodeDto(pricingEntity);
    }

    public PriceResponse getPricing(PriceCodeRequest request) {
        log.info("Fetching price configuration with code: {}", request.getCode());

        PricingEntity pricingEntity = pricingRepo.findById(request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Price configuration not found with code: " + request.getCode()));

        return pricingMapper.toDto(pricingEntity);
    }

    public CalculatePriceResponse calculatePrice(CalculatePriceRequest request) {
        log.info("Calculating pricing '{}' price for quantity: {}", request.getPriceCode(), request.getQuantity());

        PricingEntity pricingEntity = pricingRepo.findById(request.getPriceCode())
                .orElseThrow(() -> new IllegalArgumentException("Price configuration not found with code: " + request.getPriceCode()));

        BracketEntity selectedBracket = selectBracket(pricingEntity.getPricingBrackets(), request.getQuantity());

        double finalPrice = BigDecimal.valueOf(selectedBracket.getUnitPrice())
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, RoundingMode.FLOOR)
                .doubleValue();

        log.info("Calculated final price: {} using bracket: {}", finalPrice, selectedBracket);

        return CalculatePriceResponse.newBuilder()
                .setPeriod(pricingEntity.getPeriod())
                .setPricingBracket(pricingMapper.toDto(selectedBracket))
                .setTotalPrice(finalPrice)
                .build();
    }

    private BracketEntity selectBracket(List<BracketEntity> pricingBrackets, double quantity) {
        if (CollectionUtils.isEmpty(pricingBrackets)) {
            throw new IllegalArgumentException("No pricing brackets available.");
        }

        List<BracketEntity> sortedBrackets = pricingBrackets.stream()
                .sorted(Comparator.comparingInt(BracketEntity::getOrder))
                .toList();

        for (BracketEntity bracket : sortedBrackets) {
            if (quantity >= bracket.getMinQuantity() &&
                (bracket.getMaxQuantity() == null || quantity <= bracket.getMaxQuantity())) {
                return bracket;
            }
        }

        throw new IllegalArgumentException("No suitable pricing bracket found for quantity: " + quantity);
    }
}
