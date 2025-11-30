package it.salspa.demo.spring.grpc.contract.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "it.salspa.demo.spring.grpc.contract.core",
        "it.salspa.demo.spring.grpc.customer.client",
        "it.salspa.demo.spring.grpc.pricing.client"
})
public class ContractServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContractServiceApplication.class, args);
    }
}
