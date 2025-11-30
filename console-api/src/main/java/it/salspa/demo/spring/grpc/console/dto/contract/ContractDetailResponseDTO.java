package it.salspa.demo.spring.grpc.console.dto.contract;

import java.time.LocalDateTime;
import java.util.List;

public record ContractDetailResponseDTO(
        String code,
        Integer version,
        ContractCustomerDTO customer,
        String status,
        List<ProductDTO> products,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
