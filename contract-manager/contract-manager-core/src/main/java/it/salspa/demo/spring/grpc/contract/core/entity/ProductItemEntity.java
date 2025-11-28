package it.salspa.demo.spring.grpc.contract.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_items")
@CompoundIndex(name = "contract_product_idx", def = "{'contractCode': 1, 'productId': 1}", unique = true)
public class ProductItemEntity {
    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed
    private String contractCode;
    private String productId;
    private String pricingId;
    private Double usageAmount;
}
