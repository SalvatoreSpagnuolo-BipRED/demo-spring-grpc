package it.salspa.demo.spring.grpc.contract.core;

import it.salspa.demo.spring.grpc.customer.client.CustomerClient;
import it.salspa.demo.spring.grpc.pricing.client.PricingClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ContractManagerApplicationTests {

    @MockBean
    private CustomerClient customerClient;

    @MockBean
    private PricingClient pricingClient;

    @Test
    void contextLoads() {
    }

}
