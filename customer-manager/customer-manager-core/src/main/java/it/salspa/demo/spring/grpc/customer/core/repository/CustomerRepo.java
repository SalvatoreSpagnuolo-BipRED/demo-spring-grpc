package it.salspa.demo.spring.grpc.customer.core.repository;

import it.salspa.demo.spring.grpc.customer.core.entity.CustomerEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepo extends MongoRepository<CustomerEntity, String> {
}
