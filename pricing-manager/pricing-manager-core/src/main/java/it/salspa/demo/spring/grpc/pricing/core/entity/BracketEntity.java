package it.salspa.demo.spring.grpc.pricing.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BracketEntity {

    private Integer order;

    private Double minQuantity;

    private Double maxQuantity;

    private Double unitPrice;
}
