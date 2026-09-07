package com.ecocity.esg.adapter.out.persistence.mongodb.repository;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.EnvironmentalLicenseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EnvironmentalLicenseMongoRepository extends MongoRepository<EnvironmentalLicenseDocument, String> {
}
