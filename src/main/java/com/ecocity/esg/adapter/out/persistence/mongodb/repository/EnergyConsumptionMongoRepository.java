package com.ecocity.esg.adapter.out.persistence.mongodb.repository;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.EnergyConsumptionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EnergyConsumptionMongoRepository extends MongoRepository<EnergyConsumptionDocument, String> {
}
