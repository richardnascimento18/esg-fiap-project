package com.ecocity.esg.adapter.out.persistence.mongodb.repository;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.EnvironmentalLicenseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import com.ecocity.esg.domain.model.LicenseStatus;
import java.time.Instant;
import java.util.List;

public interface EnvironmentalLicenseMongoRepository extends MongoRepository<EnvironmentalLicenseDocument, String> {
    @Query("{'expirationDate': {'$gte': ?0, '$lte': ?1}, 'status': {'$nin': ?2}}")
    List<EnvironmentalLicenseDocument> findRenewalCandidates(
            Instant now, Instant deadline, List<LicenseStatus> statuses, Pageable pageable);
}
