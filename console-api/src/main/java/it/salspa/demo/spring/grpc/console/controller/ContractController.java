package it.salspa.demo.spring.grpc.console.controller;

import it.salspa.demo.spring.grpc.console.service.ContractService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

//    TODO: @GetMapping("/contracts/create")
}
