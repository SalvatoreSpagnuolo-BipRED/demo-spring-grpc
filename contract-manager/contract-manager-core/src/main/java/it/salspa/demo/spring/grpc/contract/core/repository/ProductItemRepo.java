package it.salspa.demo.spring.grpc.contract.core.repository;

import it.salspa.demo.spring.grpc.contract.core.entity.ProductItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductItemRepo extends MongoRepository<ProductItemEntity, String> {
    List<ProductItemEntity> findByContractCode(String contractCode);
    Optional<ProductItemEntity> findByContractCodeAndProductId(String contractCode, String productId);
}
