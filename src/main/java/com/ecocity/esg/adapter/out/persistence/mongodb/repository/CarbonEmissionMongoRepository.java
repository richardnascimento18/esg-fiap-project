package com.ecocity.esg.adapter.out.persistence.mongodb.repository;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.CarbonEmissionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CarbonEmissionMongoRepository extends MongoRepository<CarbonEmissionDocument, String> {
}
