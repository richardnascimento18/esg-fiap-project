package com.ecocity.esg.adapter.out.persistence.mongodb.repository;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.DiversityReportDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DiversityReportMongoRepository extends MongoRepository<DiversityReportDocument, String> {
}
