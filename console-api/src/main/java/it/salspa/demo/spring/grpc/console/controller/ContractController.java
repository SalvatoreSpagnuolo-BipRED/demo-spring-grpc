package it.salspa.demo.spring.grpc.console.controller;

import it.salspa.demo.spring.grpc.contract.client.ContractClient;
import it.salspa.demo.spring.grpc.contract.api.ContractResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContractController {

    private final ContractClient contractClient;

    public ContractController(ContractClient contractClient) {
        this.contractClient = contractClient;
    }

    @GetMapping("/contracts/create")
    public ResponseEntity<ContractResponse> create(@RequestParam String name, @RequestParam String details) {
        ContractResponse response = contractClient.create(name, details);
        return ResponseEntity.ok(response);
    }
}
