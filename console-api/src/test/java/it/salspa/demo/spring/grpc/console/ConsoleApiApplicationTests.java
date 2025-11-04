package it.salspa.demo.spring.grpc.console;

import it.salspa.demo.spring.grpc.contract.client.ContractClient;
import it.salspa.demo.spring.grpc.customer.client.CustomerClient;
import it.salspa.demo.spring.grpc.pricing.client.PricingClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ConsoleApiApplicationTests {

    @MockBean
    private ContractClient contractClient;

    @MockBean
    private CustomerClient customerClient;

    @MockBean
    private PricingClient pricingClient;

    @Test
    void contextLoads() {
    }

}
