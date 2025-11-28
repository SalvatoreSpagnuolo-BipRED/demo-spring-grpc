package it.salspa.demo.spring.grpc.contract.core.entity;

import it.salspa.demo.spring.grpc.contract.api.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contracts")
public class ContractEntity {

    @Id
    private String code;

    @Version
    private Integer version;

    @Indexed
    private String customerId;

    private ContractStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private Instant createdAt;

    private Instant updatedAt;

}
