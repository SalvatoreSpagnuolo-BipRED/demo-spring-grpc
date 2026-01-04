package it.salspa.demo.spring.grpc.pricing.core.entity;

import it.salspa.demo.spring.grpc.pricing.api.PricingFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pricings")
public class PricingEntity {

    @Id
    private String code;

    @Version
    private Integer version;

    private PricingFrequency period;

    private List<BracketEntity> pricingBrackets;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
