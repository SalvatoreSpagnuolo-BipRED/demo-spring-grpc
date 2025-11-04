package it.salspa.demo.spring.grpc.customer.core;

import it.salspa.demo.spring.grpc.contract.client.ContractClient;
import it.salspa.demo.spring.grpc.pricing.client.PricingClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class CustomerManagerApplicationTests {

    @MockBean
    private ContractClient contractClient;

    @MockBean
    private PricingClient pricingClient;

    @Test
    void contextLoads() {
    }

}
