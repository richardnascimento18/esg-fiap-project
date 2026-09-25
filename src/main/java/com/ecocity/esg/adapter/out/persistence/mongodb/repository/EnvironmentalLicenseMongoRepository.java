package com.ecocity.esg.adapter.out.persistence.mongodb.repository;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.EnvironmentalLicenseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import com.ecocity.esg.domain.model.LicenseStatus;
import java.time.Instant;
import java.util.List;

public interface EnvironmentalLicenseMongoRepository extends MongoRepository<EnvironmentalLicenseDocument, String> {
    List<EnvironmentalLicenseDocument> findByExpirationDateBeforeAndStatusNot(
            Instant deadline, LicenseStatus status, Pageable pageable);
}
