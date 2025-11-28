package it.salspa.demo.spring.grpc.contract.core.repository;

import it.salspa.demo.spring.grpc.contract.core.entity.ContractEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepo extends MongoRepository<ContractEntity, String> {
    List<ContractEntity> findByCustomerId(String customerId);
}
