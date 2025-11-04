package it.salspa.demo.spring.grpc.pricing.core;

import it.salspa.demo.spring.grpc.contract.client.ContractClient;
import it.salspa.demo.spring.grpc.customer.client.CustomerClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class PricingManagerApplicationTests {

    @MockBean
    private ContractClient contractClient;

    @MockBean
    private CustomerClient customerClient;

    @Test
    void contextLoads() {
    }

}
