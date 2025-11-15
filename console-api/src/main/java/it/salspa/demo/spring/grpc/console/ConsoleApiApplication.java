package it.salspa.demo.spring.grpc.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "it.salspa.demo.spring.grpc.console",
        "it.salspa.demo.spring.grpc.contract.client",
        "it.salspa.demo.spring.grpc.customer.client",
        "it.salspa.demo.spring.grpc.pricing.client"
})
public class ConsoleApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsoleApiApplication.class, args);
    }
}
