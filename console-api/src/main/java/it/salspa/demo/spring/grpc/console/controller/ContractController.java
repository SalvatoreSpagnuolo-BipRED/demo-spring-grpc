package it.salspa.demo.spring.grpc.console.controller;

import it.salspa.demo.spring.grpc.console.dto.ContractResponseDTO;
import it.salspa.demo.spring.grpc.console.service.ContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/contracts/create")
    public ResponseEntity<ContractResponseDTO> create(@RequestParam String name, @RequestParam String details) {
        ContractResponseDTO response = contractService.create(name, details);
        return ResponseEntity.ok(response);
    }
}
