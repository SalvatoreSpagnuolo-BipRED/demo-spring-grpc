package it.salspa.demo.spring.grpc.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.salspa.demo.spring.grpc.console.dto.contract.ContractCodeResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.contract.ContractDetailResponseDTO;
import it.salspa.demo.spring.grpc.console.dto.contract.CreateContractRequestDTO;
import it.salspa.demo.spring.grpc.console.dto.customer.CreateCustomerRequestDTO;
import it.salspa.demo.spring.grpc.console.dto.customer.CustomerResponseDTO;
import it.salspa.demo.spring.grpc.console.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management API")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new customer", description = "Creates a new customer returns his details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public CustomerResponseDTO create(
            @Parameter(description = "Customer creation request payload", required = true)
            @RequestBody CreateCustomerRequestDTO request) {
        return customerService.create(request);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get customer by code", description = "Retrieves detailed customer information by code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public CustomerResponseDTO getByCode(
            @Parameter(description = "Customer code", required = true, example = "CUS-001")
            @PathVariable String code) {
        return customerService.getByCode(code);
    }
}
