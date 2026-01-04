package it.salspa.demo.spring.grpc.pricing.core.repository;

import it.salspa.demo.spring.grpc.pricing.core.entity.PricingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingRepo extends MongoRepository<PricingEntity, String> {
}
