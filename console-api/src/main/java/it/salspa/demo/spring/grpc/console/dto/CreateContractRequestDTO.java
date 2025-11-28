package it.salspa.demo.spring.grpc.console.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CreateContractRequestDTO(
        String customerId,
        List<ProductDTO> products,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
