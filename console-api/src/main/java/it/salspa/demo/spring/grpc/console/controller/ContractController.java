package it.salspa.demo.spring.grpc.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.salspa.demo.spring.grpc.console.dto.contract.ContractCodeResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.contract.ContractDetailResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.contract.CreateContractRequestDTO;
import it.salspa.demo.spring.grpc.console.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract", description = "Contract management API")
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new contract", description = "Creates a new contract with associated products and returns the contract code and version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contract created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ContractCodeResponseDTO create(
            @Parameter(description = "Contract creation request payload", required = true)
            @RequestBody CreateContractRequestDTO request) {
        return contractService.create(request);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get contract by code", description = "Retrieves detailed contract information by contract code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contract found"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ContractDetailResponseDTO getByCode(
            @Parameter(description = "Contract code", required = true, example = "CTR-001")
            @PathVariable String code) {
        return contractService.getByCode(code);
    }
}
