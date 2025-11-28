package it.salspa.demo.spring.grpc.console.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContractDetailResponseDTO(
        String code,
        Integer version,
        String customerId,
        String status,
        List<ProductDTO> products,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
